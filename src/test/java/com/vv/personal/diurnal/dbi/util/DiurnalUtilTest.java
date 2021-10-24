package com.vv.personal.diurnal.dbi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.processStringForSqlPush;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.refineDbStringForOriginal;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vivek
 * @since 06/03/21
 */
@ExtendWith(MockitoExtension.class)
class DiurnalUtilTest {

    @Test
    void testProcessStringForSqlPushAndBackRefinement() {
        String input = "{\"mobile\": \"123456789\",\"date\": 20210306,\"amount\": 121.12,\"description\": \"Sampler s1 't1'\"}";
        String result = processStringForSqlPush(input);
        System.out.println(result);
        assertThat(result).hasToString("{`@%mobile`@%: `@%123456789`@%,`@%date`@%: 20210306,`@%amount`@%: 121.12,`@%description`@%: `@%Sampler s1 ''t1''`@%}");

        result = refineDbStringForOriginal(result);
        System.out.println(result);
        assertThat(result).hasToString(input);
    }

    @Test
    void testConvertEntryDayDateToDisplayFormat() {
        String convertedDisplayDate = DiurnalUtil.convertEntryDayDateToDisplayFormat(20201211);
        System.out.println(convertedDisplayDate);
        assertThat(convertedDisplayDate).hasToString("11-12-2020");
    }
}