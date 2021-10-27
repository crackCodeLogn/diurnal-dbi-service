package com.vv.personal.diurnal.dbi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vivek
 * @since 27/10/21
 */
@ExtendWith(MockitoExtension.class)
class UserMappingControllerTest {

    @InjectMocks
    UserMappingController userMappingController;

    @Test
    void testGetTrialEndPeriod() {
        int daysToExtend = 30;
        long currentTimeMillis = System.currentTimeMillis();

        Instant trialEndPeriodTimeInstant = userMappingController.getTrialEndPeriod(daysToExtend);
        assertThat((trialEndPeriodTimeInstant.toEpochMilli() - currentTimeMillis) / 1000)
                .isGreaterThanOrEqualTo(24 * 60 * 60 * daysToExtend)
                .isLessThan(24 * 60 * 60 * daysToExtend + 1); //allowing 1 second delta for upper bound check
        System.out.printf("Current timestamp: %d\nTrial   timestamp: %d\n", currentTimeMillis, trialEndPeriodTimeInstant.toEpochMilli());
        System.out.println(trialEndPeriodTimeInstant);
    }
}