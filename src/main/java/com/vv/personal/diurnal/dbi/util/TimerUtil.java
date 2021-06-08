package com.vv.personal.diurnal.dbi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Vivek
 * @since 07/03/21
 */
public class TimerUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(TimerUtil.class);

    public static void scheduleTimer(Timer timer, TimerTask timerTask, long seconds) {
        timer.schedule(timerTask, seconds * 1000);
        LOGGER.info("Scheduled timer task for: {} for {} seconds", timer, seconds);
    }

    public static Timer generateNewTimer() {
        Timer timer = new Timer();
        LOGGER.info("Generated new timer: {}", timer);
        return timer;
    }

}
