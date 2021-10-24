package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.config.DbiConfig;
import com.vv.personal.diurnal.dbi.config.GenericConfig;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntryDay;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateHash;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.procureStopWatch;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Vivek
 * @since 03/03/21
 */
@ExtendWith(MockitoExtension.class)
public class DataControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataControllerTest.class);

    @InjectMocks
    private final DataController dataController = new DataController();
    @InjectMocks
    private final EntryDayController entryDayController = new EntryDayController();
    @Mock
    private UserMappingController userMappingController;
    @Mock
    private DiurnalTableEntryDay diurnalTableEntryDay;
    @Mock
    private GenericConfig genericConfig;

    public static List<String> readFileFromLocation(String src) {
        List<String> data = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(src))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.trim().isEmpty()) data.add(line.trim());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read file contents from '{}'. ", src, e);
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
        when(diurnalTableEntryDay.pushNewEntity(any(EntryDayProto.EntryDay.class))).thenReturn(1);
        StopWatch stopWatch = procureStopWatch();
        when(genericConfig.procureStopWatch()).thenReturn(stopWatch);
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