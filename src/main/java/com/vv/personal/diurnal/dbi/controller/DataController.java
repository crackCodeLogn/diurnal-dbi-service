package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.DataTransitProto;
import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.config.GenericConfig;
import com.vv.personal.diurnal.dbi.engine.transformer.TransformFullBackupToProtos;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import com.vv.personal.diurnal.dbi.util.TimingUtil;
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

import static com.vv.personal.diurnal.dbi.constants.Constants.*;
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
    private EntryDayController entryDayController;
    @Autowired
    private UserMappingController userMappingController;
    @Autowired
    private GenericConfig genericConfig;

    @ApiOperation(value = "Sign up new user", hidden = true)
    @PostMapping("/signup")
    public ResponsePrimitiveProto.ResponsePrimitive signUpUser(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Rx-ed user to sign up -> [{}]", userMapping.getEmail());
        StopWatch stopWatch = genericConfig.procureStopWatch();
        try {
            boolean signUpResult = userMappingController.createUserMapping(userMapping) == ONE;
            LOGGER.info("Sign up result for [{}] => {}", userMapping.getEmail(), signUpResult);
            return signUpResult ? RESPOND_TRUE_BOOL : RESPOND_FALSE_BOOL;
        } finally {
            stopWatch.stop();
            LOGGER.info("SignUp Operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
    }

    @ApiOperation(value = "Check if sign up new user already exists", hidden = true)
    @PostMapping("/signup/check/email")
    public ResponsePrimitiveProto.ResponsePrimitive checkSignUpUserEmail(@RequestBody DataTransitProto.DataTransit dataTransit) {
        LOGGER.info("Checking if user with email [{}] exists in DB", dataTransit.getEmail());
        StopWatch stopWatch = genericConfig.procureStopWatch();
        try {
            Integer emailHash = userMappingController.retrieveHashEmail(dataTransit.getEmail());
            if (isEmailHashAbsent(emailHash)) {
                LOGGER.info("User doesn't exist for email: {}", dataTransit.getEmail());
                return RESPOND_FALSE_BOOL;
            }
            return RESPOND_TRUE_BOOL;
        } finally {
            stopWatch.stop();
            LOGGER.info("Checking user already exists operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
    }

    @ApiOperation(value = "Read whole backup file and generate data for DB", hidden = true)
    @PostMapping("/push/backup/whole")
    public ResponsePrimitiveProto.ResponsePrimitive pushWholeBackup(@RequestBody DataTransitProto.DataTransit dataTransit) {
        LOGGER.info("Rx-ed data in dataTransit to backup to DB: {} bytes, for email [{}]", dataTransit.getBackupData().getBytes().length,
                dataTransit.getEmail());
        StopWatch stopWatch = genericConfig.procureStopWatch();
        try {
            Integer emailHash = userMappingController.retrieveHashEmail(dataTransit.getEmail());
            if (isEmailHashAbsent(emailHash)) {
                LOGGER.warn("User doesn't exist for email: {}", dataTransit.getEmail());
                return RESPOND_FALSE_BOOL;
            }
            if (!userMappingController.retrievePremiumUserStatus(emailHash)) {
                LOGGER.warn("User for email [{}] doesn't have premium-user privileges, cannot proceed with cloud backup!", dataTransit.getEmail());
                return RESPOND_FALSE_BOOL;
            }
            TransformFullBackupToProtos transformFullBackupToProtos = new TransformFullBackupToProtos(
                    Arrays.asList(StringUtils.split(dataTransit.getBackupData(), NEW_LINE)),
                    emailHash);
            if (transformFullBackupToProtos.transformWithoutSuppliedDate()) {
                List<Integer> bulkEntryDayOpResult = entryDayController.deleteAndCreateEntryDays(transformFullBackupToProtos);
                if (bulkEntryDayOpResult.stream().allMatch(integer -> integer == 1)) {
                    UserMappingProto.UserMapping userMapping = UserMappingProto.UserMapping.newBuilder()
                            .setEmail(dataTransit.getEmail())
                            .setHashEmail(emailHash)
                            .setLastCloudSaveTimestamp(TimingUtil.extractCurrentUtcTimestamp())
                            .build();
                    return generateResponsePrimitiveBool(userMappingController.updateUserMappingLastCloudSaveTimestamp(userMapping) == ONE);
                }
            } else {
                LOGGER.warn("Incomplete / incorrect save to cloud done!!");
            }
        } finally {
            stopWatch.stop();
            LOGGER.info("Pushing backup to cloud operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
        return RESPOND_FALSE_BOOL;
    }

    @ApiOperation(value = "push last saved timestamp", hidden = true)
    @PostMapping("/push/timestamp/save")
    public ResponsePrimitiveProto.ResponsePrimitive pushLastSavedTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Received push for last save ts for [{}]", userMapping.getEmail());
        return generateResponsePrimitiveInt(userMappingController.updateUserMappingLastSaveTimestamp(userMapping));
    }

    @ApiOperation(value = "push user info update for name, mobile and currency", hidden = true)
    @PostMapping("/push/user/info")
    public ResponsePrimitiveProto.ResponsePrimitive pushUserInfo(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Received new user info for [{}] -> {}, {}, {}", userMapping.getEmail(), userMapping.getUsername(), userMapping.getMobile(), userMapping.getCurrency());
        return generateResponsePrimitiveBool(userMappingController.updateUserInfo(userMapping));
    }

    @ApiOperation(value = "retrieve user detail", hidden = true)
    @PostMapping("/retrieve/user")
    public UserMappingProto.UserMapping retrieveUserDetailsFromDb(@RequestBody DataTransitProto.DataTransit dataTransit) {
        LOGGER.info("Received req to extract details for user: {}", dataTransit.getEmail());
        Integer emailHash = userMappingController.retrieveHashEmail(dataTransit.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User doesn't exist for email: {}", dataTransit.getEmail());
            return EMPTY_USER_MAPPING;
        }
        UserMappingProto.UserMapping retrievedUserMapping = userMappingController.retrieveUserMapping(emailHash);
        LOGGER.info("Replying with user mapping: {} x {}", retrievedUserMapping.getEmail(), retrievedUserMapping.getUsername());
        return retrievedUserMapping;
    }

    @GetMapping("/manual/retrieve/user/hash/cred")
    public String retrieveUserHashCredFromDbManually(@RequestParam String email) {
        email = refineEmail(email);
        return retrieveUserDetailsFromDb(DiurnalUtil.generateDataTransit(email)).getHashCred();
    }

    public DataController setEntryDayController(EntryDayController entryDayController) {
        this.entryDayController = entryDayController;
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
