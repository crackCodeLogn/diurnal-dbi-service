package com.vv.personal.diurnal.dbi.util;

import java.time.Instant;

/**
 * @author Vivek
 * @since 05/04/21
 */
public class TimingUtil {

    private TimingUtil() {
    }

    public static long extractCurrentUtcTimestamp() {
        return Instant.now().toEpochMilli();
    }

    public static boolean hasTimestampExpired(Long inputTimestamp) {
        return System.currentTimeMillis() > inputTimestamp;
    }
}