package com.vv.personal.diurnal.dbi.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Vivek
 * @since 07/03/21
 */
@Slf4j
public class TimerUtil {

    private TimerUtil() {
    }

    public static void scheduleTimer(Timer timer, TimerTask timerTask, long seconds) {
        timer.schedule(timerTask, seconds * 1000);
        log.info("Scheduled timer task for: {} for {} seconds", timer, seconds);
    }

    public static Timer generateNewTimer() {
        Timer timer = new Timer();
        log.info("Generated new timer: {}", timer);
        return timer;
    }
}