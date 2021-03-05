package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.*;
import com.vv.personal.diurnal.dbi.config.GenericConfig;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntry;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableTitleMapping;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.procureStopWatch;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Vivek
 * @since 03/03/21
 */
@RunWith(MockitoJUnitRunner.class)
public class DataControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataControllerTest.class);

    @InjectMocks
    private final DataController dataController = new DataController();
    @InjectMocks
    private final TitleMappingController titleMappingController = new TitleMappingController();
    @InjectMocks
    private final EntryController entryController = new EntryController();
    @Mock
    private UserMappingController userMappingController;
    @Mock
    private DiurnalTableTitleMapping diurnalTableTitleMapping;
    @Mock
    private DiurnalTableEntry diurnalTableEntry;
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

    @Before
    public void preHaste() {
        dataController.setTitleMappingController(titleMappingController);
        dataController.setEntryController(entryController);
    }

    @Test
    public void testPushWholeBackup() {
        List<String> testData = readFileFromLocation("src/test/resources/sample.backup.txt");
        System.out.println(testData);
        long mobile = 1234567890L;

        when(userMappingController.checkIfUserExists(any(UserMappingProto.UserMapping.class))).thenReturn(true);
        when(diurnalTableTitleMapping.deleteEntity(any(TitleMappingProto.TitleMapping.class))).thenReturn(0);
        when(diurnalTableTitleMapping.pushNewEntity(any(TitleMappingProto.TitleMapping.class))).thenReturn(1);
        when(diurnalTableEntry.deleteEntity(any(EntryProto.Entry.class))).thenReturn(0);
        when(diurnalTableEntry.pushNewEntity(any(EntryProto.Entry.class))).thenReturn(1);
        StopWatch stopWatch = procureStopWatch();
        when(genericConfig.procureStopWatch()).thenReturn(stopWatch);
        stopWatch.start();
        ResponsePrimitiveProto.ResponsePrimitive backupPushResult = dataController.pushWholeBackup(DiurnalUtil.generateDataTransit(mobile, 20210304, DataTransitProto.Currency.INR,
                StringUtils.join(testData, "\n")));

        assertTrue(backupPushResult.getBoolResponse());
    }

}
