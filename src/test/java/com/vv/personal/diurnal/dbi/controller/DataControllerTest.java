package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.config.BeanStore;
import com.vv.personal.diurnal.dbi.config.DbiLimitPeriodDaysConfig;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntryDay;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateHash;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.procureStopWatch;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * @author Vivek
 * @since 03/03/21
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class DataControllerTest {
    @InjectMocks
    final DataController dataController = new DataController();
    @InjectMocks
    final EntryDayController entryDayController = new EntryDayController();
    @Mock
    UserMappingController userMappingController;
    @Mock
    DiurnalTableEntryDay diurnalTableEntryDay;
    @Mock
    BeanStore beanStore;
    @Mock
    DbiLimitPeriodDaysConfig dbiLimitPeriodDaysConfig;

    public static List<String> readFileFromLocation(String src) {
        List<String> data = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(src))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.trim().isEmpty()) data.add(line.trim());
            }
        } catch (IOException e) {
            log.error("Failed to read file contents from '{}'. ", src, e);
        }
        //LOGGER.info("Data read in => \n{}", data);
        return data;
    }

    @BeforeEach
    public void preHaste() {
        dataController.setEntryDayController(entryDayController);
    }

    @Test
    void testPushWholeBackup() {
        List<String> testData = readFileFromLocation("src/test/resources/sample.backup.txt");
        System.out.println(testData);
        long mobile = 1234567890L;
        String email = "something@somewhere.com";
        Integer emailHash = generateHash(email);

        when(userMappingController.retrieveHashEmail(email)).thenReturn(emailHash);
        when(userMappingController.retrievePremiumUserStatus(emailHash)).thenReturn(true);
        when(userMappingController.updateUserMappingLastCloudSaveTimestamp(any(UserMappingProto.UserMapping.class))).thenReturn(1);
        when(diurnalTableEntryDay.pushNewEntities(anyList())).thenReturn(3);
        when(dbiLimitPeriodDaysConfig.cloud()).thenReturn(365);
        StopWatch stopWatch = procureStopWatch();
        when(beanStore.procureStopWatch()).thenReturn(stopWatch);

        dataController.setExemptedEmails(Sets.newHashSet());
        stopWatch.start();
        ResponsePrimitiveProto.ResponsePrimitive backupPushResult = dataController.pushWholeBackup(
                DiurnalUtil.generateDataTransit(mobile, email, 20210304, UserMappingProto.Currency.INR,
                        StringUtils.join(testData, "\n")));
        assertThat(backupPushResult.getBoolResponse()).isTrue();

        stopWatch.reset();
        stopWatch.start();
        when(userMappingController.retrievePremiumUserStatus(emailHash)).thenReturn(false);
        backupPushResult = dataController.pushWholeBackup(
                DiurnalUtil.generateDataTransit(mobile, email, 20210304, UserMappingProto.Currency.INR,
                        StringUtils.join(testData, "\n")));
        assertThat(backupPushResult.getBoolResponse()).isFalse();
    }
}