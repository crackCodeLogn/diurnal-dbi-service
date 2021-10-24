package com.vv.personal.diurnal.dbi.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Vivek
 * @since 05/04/21
 */
@RunWith(JUnit4.class)
public class TimingUtilTest {

    @Test
    public void testExtractCurrentUtcTimestamp() {
        System.out.println(TimingUtil.extractCurrentUtcTimestamp());
    }
}