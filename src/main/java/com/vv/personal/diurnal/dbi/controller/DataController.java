package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.DataTransitProto;
import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.config.GenericConfig;
import com.vv.personal.diurnal.dbi.engine.transformer.TransformFullBackupToProtos;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vv.personal.diurnal.dbi.constants.Constants.NEW_LINE;
import static com.vv.personal.diurnal.dbi.constants.Constants.RESPOND_FALSE_BOOL;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.*;

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
    private EntryDayController entryDayController;
    @Autowired
    private TitleMappingController titleMappingController;
    @Autowired
    private UserMappingController userMappingController;
    @Autowired
    private GenericConfig genericConfig;

    @ApiOperation(value = "Read whole backup file and generate data for DB", hidden = true)
    @PostMapping("/push/backup/whole")
    public ResponsePrimitiveProto.ResponsePrimitive pushWholeBackup(@RequestBody DataTransitProto.DataTransit dataTransit) {
        LOGGER.info("Rx-ed data in dataTransit to backup to DB: {} bytes", dataTransit.getBackupData().getBytes().length);
        StopWatch stopWatch = genericConfig.procureStopWatch();
        Integer emailHash = userMappingController.retrieveHashEmail(dataTransit.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User doesn't exist for email: {}", dataTransit.getEmail());
            stopWatch.stop();
            LOGGER.info("Operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
            return RESPOND_FALSE_BOOL;
        }
        boolean opResult = false;
        TransformFullBackupToProtos transformFullBackupToProtos = new TransformFullBackupToProtos(
                Arrays.asList(StringUtils.split(dataTransit.getBackupData(), NEW_LINE)),
                emailHash);
        if (transformFullBackupToProtos.transformWithoutSuppliedDate()) {
            List<Integer> bulkEntryDayOpResult = entryDayController.deleteAndCreateEntryDays(transformFullBackupToProtos.getEntryDayList());
            if (bulkEntryDayOpResult.stream().allMatch(integer -> integer == 1)) opResult = true;
        }
        stopWatch.stop();
        LOGGER.info("Operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        return generateResponsePrimitiveBool(opResult);
    }

    @ApiOperation(value = "retrieve hash cred for user", hidden = true)
    @PostMapping("/retrieve/user/hash/cred")
    public ResponsePrimitiveProto.ResponsePrimitive retrieveUserHashCredFromDb(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Received req to extract hash for user: {}", userMapping.getEmail());
        String hashCred = userMappingController.retrieveHashCred(userMapping.getEmail());
        return generateResponsePrimitiveString(hashCred);
    }

    @GetMapping("/manual/retrieve/user/hash/cred")
    public String retrieveUserHashCredFromDbManually(@RequestParam String email) {
        email = refineEmail(email);
        return retrieveUserHashCredFromDb(DiurnalUtil.generateUserMapping(email)).getResponse();
    }


    public DataController setEntryController(EntryController entryController) {
        this.entryController = entryController;
        return this;
    }

    public DataController setEntryDayController(EntryDayController entryDayController) {
        this.entryDayController = entryDayController;
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
