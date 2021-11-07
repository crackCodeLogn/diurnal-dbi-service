package com.vv.personal.diurnal.dbi.activity;

/*import com.vv.personal.diurnal.dbi.component.ShutdownManager;
import com.vv.personal.diurnal.dbi.config.DbiInactiveTimeoutConfig;
import com.vv.personal.diurnal.dbi.config.DbiShutdownExitCodeConfig;
import com.vv.personal.diurnal.dbi.util.TimerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Timer;
import java.util.TimerTask;*/

/**
 * @author Vivek
 * @since 08/04/21
 */
/*
@Slf4j
@Deprecated
@Component
public class InactiveTimerComponent implements WebMvcConfigurer, HandlerInterceptor {
    private Timer inactiveTimer = TimerUtil.generateNewTimer();
    @Autowired
    private ShutdownManager shutdownManager;
    @Autowired
    private DbiInactiveTimeoutConfig inactiveTimeoutConfig;
    @Autowired
    private DbiShutdownExitCodeConfig dbiShutdownExitCodeConfig;

    //need to keep this synchronized in order to avoid all web req creating their own timer
    @Override
    public synchronized boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        restartInactiveTimer();
        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @PostConstruct
    public void postHaste() {
        log.info("Is DBI InactiveTimeout enabled: {}", inactiveTimeoutConfig.isEnabled());
        restartInactiveTimer();
    }

    private void restartInactiveTimer() {
        if (inactiveTimeoutConfig.isEnabled()) {
            log.info("Cancelling inactive timer [{}] and restarting it thereafter", inactiveTimer);
            inactiveTimer.cancel();
            //inactiveTimer.purge();
            inactiveTimer = TimerUtil.generateNewTimer();
            TimerUtil.scheduleTimer(inactiveTimer, procureInactiveTimerTask(inactiveTimer), inactiveTimeoutConfig.getSeconds());
        }
    }

    private TimerTask procureInactiveTimerTask(Timer inactiveTimer) {
        return new TimerTask() {
            @Override
            public void run() {
                log.warn("*** Attention, shutting down now ***");
                shutdownManager.initiateShutdown(dbiShutdownExitCodeConfig.getCode());

                log.info("Shutting down inactive-timer task");
                inactiveTimer.cancel();
                inactiveTimer.purge();
            }
        };
    }
}*/
