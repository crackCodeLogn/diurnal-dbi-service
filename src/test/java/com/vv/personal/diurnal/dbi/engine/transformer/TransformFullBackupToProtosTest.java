package com.vv.personal.diurnal.dbi.engine.transformer;

import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vivek
 * @since 29/10/21
 */
class TransformFullBackupToProtosTest {

    private TransformFullBackupToProtos transformFullBackupToProtos;

    @Test
    void testGetAllowedFirstDayForCloud() {
        transformFullBackupToProtos = new TransformFullBackupToProtos(Lists.newArrayList(), -1, 365);
        int computedFirstDay = transformFullBackupToProtos.getAllowedFirstDayForCloud(20211029);
        assertThat(computedFirstDay).isEqualTo(20201029);

        transformFullBackupToProtos = new TransformFullBackupToProtos(Lists.newArrayList(), -1, 1);
        computedFirstDay = transformFullBackupToProtos.getAllowedFirstDayForCloud(20211029);
        assertThat(computedFirstDay).isEqualTo(20211028);
    }

    @Test
    void testTrimDownDataToBeSaved() {
        int emailHash = -1, startDate = 20180101;
        transformFullBackupToProtos = new TransformFullBackupToProtos(Lists.newArrayList(), emailHash, 365);
        for (int i = -1; ++i <= 2000; ) {
            transformFullBackupToProtos.getEntryDayListBuilder().addEntryDay(DiurnalUtil.generateEntryDayOnPk(emailHash, transformDate(startDate, i)));
        }

        transformFullBackupToProtos.trimDownDataToBeSaved();
        System.out.println(transformFullBackupToProtos.getEntryDayListBuilder().getEntryDayList().get(0).getDate());
        assertThat(transformFullBackupToProtos.getEntryDayList().getEntryDayList().get(0).getDate()).isEqualTo(20220625);
        assertThat(transformFullBackupToProtos.getEntryDayList().getEntryDayCount()).isEqualTo(365);
    }

    private int transformDate(int startDate, int delta) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(String.valueOf(startDate), dateTimeFormatter);
        return Integer.parseInt(localDate.plusDays(delta).format(dateTimeFormatter));
    }
}