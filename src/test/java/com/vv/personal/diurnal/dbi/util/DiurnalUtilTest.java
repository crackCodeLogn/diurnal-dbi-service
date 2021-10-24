package com.vv.personal.diurnal.dbi.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.processStringForSqlPush;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.refineDbStringForOriginal;
import static org.junit.Assert.assertEquals;

/**
 * @author Vivek
 * @since 06/03/21
 */
@RunWith(JUnit4.class)
public class DiurnalUtilTest {

    @Test
    public void testProcessStringForSqlPushAndBackRefinement() {
        String input = "{\"mobile\": \"123456789\",\"date\": 20210306,\"amount\": 121.12,\"description\": \"Sampler s1 't1'\"}";
        String result = processStringForSqlPush(input);
        System.out.println(result);
        assertEquals("{`@%mobile`@%: `@%123456789`@%,`@%date`@%: 20210306,`@%amount`@%: 121.12,`@%description`@%: `@%Sampler s1 ''t1''`@%}", result);

        result = refineDbStringForOriginal(result);
        System.out.println(result);
        assertEquals(input, result);
    }

    @Test
    public void testConvertEntryDayDateToDisplayFormat() {
        String convertedDisplayDate = DiurnalUtil.convertEntryDayDateToDisplayFormat(20201211);
        System.out.println(convertedDisplayDate);
        assertEquals("11-12-2020", convertedDisplayDate);
    }
}