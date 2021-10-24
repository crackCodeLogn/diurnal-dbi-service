package com.vv.personal.diurnal.dbi.util;

import org.junit.jupiter.api.Test;

/**
 * @author Vivek
 * @since 05/04/21
 */
class TimingUtilTest {

    @Test
    void testExtractCurrentUtcTimestamp() {
        System.out.println(TimingUtil.extractCurrentUtcTimestamp());
    }
}