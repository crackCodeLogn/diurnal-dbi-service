package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.DataTransitProto;
import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.config.BeanStore;
import com.vv.personal.diurnal.dbi.config.DbiLimitPeriodDaysConfig;
import com.vv.personal.diurnal.dbi.engine.transformer.TransformBackupToString;
import com.vv.personal.diurnal.dbi.engine.transformer.TransformFullBackupToProtos;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import com.vv.personal.diurnal.dbi.util.TimingUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.*;

/**
 * @author Vivek
 * @since 27/02/21
 * <p>
 * This controller's end-points are the one to be used by external client - app - to push data to DB
 */
@Slf4j
@RestController("data-controller")
@RequestMapping("/diurnal/data")
public class DataController {

    @Autowired
    private EntryDayController entryDayController;
    @Autowired
    private UserMappingController userMappingController;
    @Autowired
    private BeanStore beanStore;
    @Autowired
    private DbiLimitPeriodDaysConfig dbiLimitPeriodDaysConfig;

    @ApiOperation(value = "Sign up new user", hidden = true)
    @PostMapping("/signup")
    public ResponsePrimitiveProto.ResponsePrimitive signUpUser(@RequestBody UserMappingProto.UserMapping userMapping) {
        log.info("Rx-ed user to sign up -> [{}]", userMapping.getEmail());
        StopWatch stopWatch = beanStore.procureStopWatch();
        try {
            boolean signUpResult = userMappingController.createUserMapping(userMapping) == ONE;
            log.info("Sign up result for [{}] => {}", userMapping.getEmail(), signUpResult);
            return signUpResult ? RESPOND_TRUE_BOOL : RESPOND_FALSE_BOOL;
        } finally {
            stopWatch.stop();
            log.info("SignUp Operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
    }

    @ApiOperation(value = "Check if sign up new user already exists", hidden = true)
    @PostMapping("/signup/check/email")
    public ResponsePrimitiveProto.ResponsePrimitive checkSignUpUserEmail(@RequestBody DataTransitProto.DataTransit dataTransit) {
        log.info("Checking if user with email [{}] exists in DB", dataTransit.getEmail());
        StopWatch stopWatch = beanStore.procureStopWatch();
        try {
            Integer emailHash = userMappingController.retrieveHashEmail(dataTransit.getEmail());
            if (isEmailHashAbsent(emailHash)) {
                log.info("User doesn't exist for email: {}", dataTransit.getEmail());
                return RESPOND_FALSE_BOOL;
            }
            return RESPOND_TRUE_BOOL;
        } finally {
            stopWatch.stop();
            log.info("Checking user already exists operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
    }

    @ApiOperation(value = "Read whole backup file and generate data for DB", hidden = true)
    @PostMapping("/push/backup/whole")
    public ResponsePrimitiveProto.ResponsePrimitive pushWholeBackup(@RequestBody DataTransitProto.DataTransit dataTransit) {
        log.info("Rx-ed data in dataTransit to backup to DB: {} bytes, for email [{}]", dataTransit.getBackupData().getBytes().length,
                dataTransit.getEmail());
        StopWatch stopWatch = beanStore.procureStopWatch();
        try {
            Integer emailHash = userMappingController.retrieveHashEmail(dataTransit.getEmail());
            if (isEmailHashAbsent(emailHash)) {
                log.warn("User doesn't exist for email: {}", dataTransit.getEmail());
                return RESPOND_FALSE_BOOL;
            }
            if (!userMappingController.retrievePremiumUserStatus(emailHash)) {
                log.warn("User for email [{}] doesn't have premium-user privileges, cannot proceed with cloud backup!", dataTransit.getEmail());
                return RESPOND_FALSE_BOOL;
            }
            TransformFullBackupToProtos transformFullBackupToProtos = new TransformFullBackupToProtos(
                    Arrays.asList(StringUtils.split(dataTransit.getBackupData(), NEW_LINE)),
                    emailHash,
                    dbiLimitPeriodDaysConfig.getCloud());
            if (transformFullBackupToProtos.transformWithoutSuppliedDate()) {
                transformFullBackupToProtos.trimDownDataToBeSaved();
                if (entryDayController.deleteAndCreateEntryDays(transformFullBackupToProtos)) {
                    UserMappingProto.UserMapping userMapping = UserMappingProto.UserMapping.newBuilder()
                            .setEmail(dataTransit.getEmail())
                            .setHashEmail(emailHash)
                            .setLastCloudSaveTimestamp(TimingUtil.extractCurrentUtcTimestamp())
                            .build();
                    return generateResponsePrimitiveBool(userMappingController.updateUserMappingLastCloudSaveTimestamp(userMapping) == ONE);
                }
            } else {
                log.warn("Incomplete / incorrect save to cloud done!!");
            }
        } finally {
            stopWatch.stop();
            log.info("Pushing backup to cloud operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
        return RESPOND_FALSE_BOOL;
    }

    @ApiOperation(value = "Read DB and convert to string to respond to app", hidden = true)
    @PostMapping("/retrieve/backup/whole")
    public ResponsePrimitiveProto.ResponsePrimitive retrieveWholeBackup(@RequestBody DataTransitProto.DataTransit dataTransit) {
        log.info("Rx-ed request in dataTransit to retrieve backup from DB: {} bytes, for email [{}]", dataTransit.getBackupData().getBytes().length, dataTransit.getEmail());
        StopWatch stopWatch = beanStore.procureStopWatch();
        try {
            Integer emailHash = userMappingController.retrieveHashEmail(dataTransit.getEmail());
            if (isEmailHashAbsent(emailHash)) {
                log.warn("User doesn't exist for email: {}", dataTransit.getEmail());
                return RESPOND_EMPTY_BODY;
            }
            if (!userMappingController.retrievePremiumUserStatus(emailHash)) {
                log.warn("User for email [{}] doesn't have premium-user privileges, cannot proceed with cloud retrieval!", dataTransit.getEmail());
                return RESPOND_EMPTY_BODY;
            }
            EntryDayProto.EntryDayList enquiredEntryDayList = entryDayController.retrieveAllEntryDaysOfEmailHash(generateUserMappingOnPk(emailHash));
            if (enquiredEntryDayList.getEntryDayCount() > 0) {
                TransformBackupToString transformBackupToString = new TransformBackupToString(enquiredEntryDayList);
                return transformBackupToString.transform();
            }
        } finally {
            stopWatch.stop();
            log.info("Retrieval of cloud backup operation took: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
        return RESPOND_EMPTY_BODY;
    }

    @ApiOperation(value = "push last saved timestamp", hidden = true)
    @PostMapping("/push/timestamp/save")
    public ResponsePrimitiveProto.ResponsePrimitive pushLastSavedTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        log.info("Received push for last save ts for [{}]", userMapping.getEmail());
        return generateResponsePrimitiveInt(userMappingController.updateUserMappingLastSaveTimestamp(userMapping));
    }

    @ApiOperation(value = "push user info update for name, mobile and currency", hidden = true)
    @PostMapping("/push/user/info")
    public ResponsePrimitiveProto.ResponsePrimitive pushUserInfo(@RequestBody UserMappingProto.UserMapping userMapping) {
        log.info("Received new user info for [{}] -> {}, {}, {}", userMapping.getEmail(), userMapping.getUsername(), userMapping.getMobile(), userMapping.getCurrency());
        return generateResponsePrimitiveBool(userMappingController.updateUserInfo(userMapping));
    }

    @ApiOperation(value = "retrieve user detail", hidden = true)
    @PostMapping("/retrieve/user")
    public UserMappingProto.UserMapping retrieveUserDetailsFromDb(@RequestBody DataTransitProto.DataTransit dataTransit) {
        log.info("Received req to extract details for user: {}", dataTransit.getEmail());
        Integer emailHash = userMappingController.retrieveHashEmail(dataTransit.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User doesn't exist for email: {}", dataTransit.getEmail());
            return EMPTY_USER_MAPPING;
        }
        UserMappingProto.UserMapping retrievedUserMapping = userMappingController.retrieveUserMapping(emailHash);
        if (retrievedUserMapping.getPaymentExpiryTimestamp() != DEFAULT_PAYMENT_EXPIRY_TS && TimingUtil.hasTimestampExpired(retrievedUserMapping.getPaymentExpiryTimestamp())) {
            // If payment has expired, if any, shift user to non-premium state and update DB with that state, and pass the updated state back from DB
            UserMappingProto.UserMapping updatedUserMapping = UserMappingProto.UserMapping.newBuilder()
                    .mergeFrom(retrievedUserMapping)
                    .setPaymentExpiryTimestamp(DEFAULT_PAYMENT_EXPIRY_TS)
                    .setPremiumUser(false)
                    .build();
            userMappingController.updatePremiumUserMapping(updatedUserMapping);
            retrievedUserMapping = userMappingController.retrieveUserMapping(emailHash);
        } else if (!retrievedUserMapping.getPremiumUser() && !TimingUtil.hasTimestampExpired(retrievedUserMapping.getPaymentExpiryTimestamp())) {
            // If user is not marked as premium but the payment is yet to be expired
            UserMappingProto.UserMapping updatedUserMapping = UserMappingProto.UserMapping.newBuilder()
                    .mergeFrom(retrievedUserMapping)
                    .setPremiumUser(true)
                    .build();
            userMappingController.updatePremiumUserMapping(updatedUserMapping);
            retrievedUserMapping = userMappingController.retrieveUserMapping(emailHash);
        }
        log.info("Replying with user mapping: {} x {}", retrievedUserMapping.getEmail(), retrievedUserMapping.getUsername());
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
}