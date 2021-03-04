package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.DataTransitProto;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.dbi.config.GenericConfig;
import com.vv.personal.diurnal.dbi.engine.transformer.TransformFullBackupToProtos;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vv.personal.diurnal.dbi.constants.Constants.INT_RESPONSE_WONT_PROCESS;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateUserMappingOnPk;

/**
 * @author Vivek
 * @since 27/02/21
 * <p>
 * This controller's end-points are the one to be used by external client - app - to push data to DB
 */
@RestController("data-controller")
@RequestMapping("/diurnal/data")
public class DataController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private EntryController entryController;
    @Autowired
    private TitleMappingController titleMappingController;
    @Autowired
    private UserMappingController userMappingController;
    @Autowired
    private GenericConfig genericConfig;

    @ApiOperation(value = "push new entry", hidden = true)
    @PostMapping("/push/entry")
    public Integer pushEntry(@RequestBody EntryProto.Entry entry) {
        LOGGER.info("Rx-ed new entry to push to DB: {} x {} x {}", entry.getMobile(), entry.getDate(), entry.getSerial());
        if (!userMappingController.checkIfUserExists(generateUserMappingOnPk(entry.getMobile()))) {
            LOGGER.warn("User doesn't exist for mobile: {}", entry.getMobile());
            return INT_RESPONSE_WONT_PROCESS;
        }
        Integer result = entryController.createEntry(entry);
        LOGGER.info("Result of creating new entry: {}", result);
        return result;
    }

    @ApiOperation(value = "Read whole backup file and generate data for DB", hidden = true)
    @PostMapping("/push/backup/whole")
    public Boolean pushWholeBackup(@RequestBody DataTransitProto.DataTransit dataTransit) {
        LOGGER.info("Rx-ed data in dataTransit to backup to DB: {} bytes", dataTransit.getBackupData().getBytes().length);
        StopWatch stopWatch = genericConfig.procureStopWatch();
        if (!userMappingController.checkIfUserExists(generateUserMappingOnPk(dataTransit.getMobile()))) {
            LOGGER.warn("User doesn't exist for mobile: {}", dataTransit.getMobile());
            stopWatch.stop();
            LOGGER.info("Operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
            return false;
        }
        boolean opResult = false;
        TransformFullBackupToProtos transformFullBackupToProtos = new TransformFullBackupToProtos(
                Arrays.asList(StringUtils.split(dataTransit.getBackupData(), "\n")),
                dataTransit.getMobile());
        if (transformFullBackupToProtos.transformWithoutSuppliedDate()) {
            List<Integer> bulkTitleOpResult = titleMappingController.deleteAndCreateTitles(transformFullBackupToProtos.getTitleMapping());
            List<Integer> bulkEntryOpResult = entryController.deleteAndCreateEntries(transformFullBackupToProtos.getEntryList());
            if (bulkTitleOpResult.stream().allMatch(integer -> integer == 1) &&
                    bulkEntryOpResult.stream().allMatch(integer -> integer == 1)) opResult = true;
        }
        stopWatch.stop();
        LOGGER.info("Operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        return opResult;
    }

    public DataController setEntryController(EntryController entryController) {
        this.entryController = entryController;
        return this;
    }

    public DataController setTitleMappingController(TitleMappingController titleMappingController) {
        this.titleMappingController = titleMappingController;
        return this;
    }

    public DataController setUserMappingController(UserMappingController userMappingController) {
        this.userMappingController = userMappingController;
        return this;
    }

    public DataController setGenericConfig(GenericConfig genericConfig) {
        this.genericConfig = genericConfig;
        return this;
    }
}
