package com.vv.personal.diurnal.dbi.component.activity;

import com.vv.personal.diurnal.dbi.component.ShutdownManager;
import com.vv.personal.diurnal.dbi.config.GenericConfig;
import com.vv.personal.diurnal.dbi.util.TimerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Vivek
 * @since 08/04/21
 */
@Component
public class InactiveTimerComponent implements WebMvcConfigurer, HandlerInterceptor {
    private final Logger LOGGER = LoggerFactory.getLogger(InactiveTimerComponent.class);
    public Timer inactiveTimer = TimerUtil.generateNewTimer();
    @Autowired
    private ShutdownManager shutdownManager;
    @Autowired
    private GenericConfig genericConfig;

    //need to keep this synchronized in order to avoid all web req creating their own timer
    @Override
    public synchronized boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.info("Cancelling inactive timer [{}] and restarting it thereafter", inactiveTimer);
        restartInactiveTimer();
        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @PostConstruct
    public void postHaste() {
        restartInactiveTimer();
    }

    private void restartInactiveTimer() {
        inactiveTimer.cancel();
        //inactiveTimer.purge();
        inactiveTimer = TimerUtil.generateNewTimer();
        TimerUtil.scheduleTimer(inactiveTimer, procureInactiveTimerTask(), genericConfig.getDbiInactiveTimeoutSeconds());
    }

    private TimerTask procureInactiveTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                LOGGER.warn("*** Attention, shutting down now ***");
                shutdownManager.initiateShutdown(0);
            }
        };
    }

}
