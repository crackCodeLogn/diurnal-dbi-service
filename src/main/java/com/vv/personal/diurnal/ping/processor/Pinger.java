package com.vv.personal.diurnal.ping.processor;

import com.vv.personal.diurnal.ping.feign.HealthFeign;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Vivek
 * @since 05/02/21
 */
@Slf4j
public class Pinger {
    private final ExecutorService pingChecker;
    private final int pingTimeoutSeconds;
    private final int pingRetryCount;
    private final int pingRetryTimeoutSeconds;

    public Pinger(int pingTimeoutSeconds, int pingRetryCount, int pingRetryTimeoutSeconds) {
        this.pingTimeoutSeconds = pingTimeoutSeconds;
        this.pingRetryCount = pingRetryCount;
        this.pingRetryTimeoutSeconds = pingRetryTimeoutSeconds;

        this.pingChecker = Executors.newSingleThreadExecutor();
        log.info("Initialized Pinger with {} thread and {} s ping timeout", 1, pingTimeoutSeconds);
    }

    public boolean allEndPointsActive(HealthFeign... healthFeigns) {
        //check for end-points of rendering service and mongo-service
        int retry = 0;
        while (++retry <= pingRetryCount) {
            log.info("Attempting allEndPointsActive test sequence: {}", retry);
            AtomicBoolean allPingsPass = new AtomicBoolean(true);
            for (HealthFeign healthFeign : healthFeigns)
                if (!pingResult(createPingTask(healthFeign))) {
                    allPingsPass.set(false);
                    break;
                }
            if (allPingsPass.get()) return true;
            try {
                Thread.sleep(pingRetryTimeoutSeconds * 1000L);
            } catch (InterruptedException e) {
                log.error("Pinger interrupted whilst sleeping. ", e);
            }
        }
        return false;
    }

    private Callable<String> createPingTask(HealthFeign healthFeign) {
        log.info("Creating ping task for {}", healthFeign);
        return healthFeign::ping;
    }

    private boolean pingResult(Callable<String> pingTask) {
        Future<String> pingResultFuture = pingChecker.submit(pingTask);
        try {
            String pingResult = pingResultFuture.get(pingTimeoutSeconds, TimeUnit.SECONDS);
            log.info("Obtained '{}' as ping result for {}", pingResult, pingResult);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Timed out waiting on ping, task: {}", pingTask);
        }
        return false;
    }

    public void destroyExecutor() {
        log.info("Shutting down pinging executor");
        if (!pingChecker.isShutdown())
            pingChecker.shutdown();
    }
}