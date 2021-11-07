package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.auth.Authorizer;
import com.vv.personal.diurnal.dbi.client.impl.GitHubUserMappingFeignClientImpl;
import com.vv.personal.diurnal.dbi.config.BeanStore;
import com.vv.personal.diurnal.dbi.config.DbiLimitPeriodDaysConfig;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableUserMapping;
import com.vv.personal.diurnal.dbi.model.UserMappingEntity;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import com.vv.personal.diurnal.dbi.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.*;

/**
 * @author Vivek
 * @since 23/02/21
 */
@Slf4j
@Secured("user")
@RestController("user-mapping-controller")
@RequestMapping("/diurnal/mapping-user")
public class UserMappingController {
    @Inject
    DiurnalTableUserMapping diurnalTableUserMapping;
    @Inject
    Authorizer authorizer;
    @Inject
    DbiLimitPeriodDaysConfig dbiLimitPeriodDaysConfig;
    @Inject
    GitHubUserMappingFeignClientImpl gitHubUserMappingFeignClient;
    @Inject
    BeanStore beanStore;

    @PostMapping(value = "/create/user", consumes = APPLICATION_X_PROTOBUF)
    public Integer createUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        log.info("Creating new user mapping: {} x {} x {} x {}", userMapping.getMobile(), userMapping.getEmail(), userMapping.getUsername(), userMapping.getPremiumUser());
        Instant currentInstant = Instant.now();
        Instant trialPremiumPaymentExpiryInstant = getTrialEndPeriod(dbiLimitPeriodDaysConfig.trialPremium());
        final UserMappingEntity userMappingEntity = new UserMappingEntity()
                .setMobile(userMapping.getMobile())
                .setEmail(refineEmail(userMapping.getEmail()))
                .setUser(userMapping.getUsername())
                .setPremiumUser(true)
                .setCredHash(userMapping.getHashCred())
                .setEmailHash(generateHash(userMapping.getEmail()))
                .setLastCloudSaveTimestamp(DEFAULT_INSTANT_DATETIME)
                .setLastSaveTimestamp(DEFAULT_INSTANT_DATETIME)
                .setAccountCreationTimestamp(currentInstant)
                .setPaymentExpiryTimestamp(trialPremiumPaymentExpiryInstant)
                .setCurrency(userMapping.getCurrency().name());
        return diurnalTableUserMapping.pushNewEntity(userMappingEntity);
    }

    @PutMapping("/manual/create/user")
    public Integer createUserMappingManually(@RequestParam(defaultValue = "-1", required = false) Long mobile,
                                             @RequestParam String email,
                                             @RequestParam String user,
                                             @RequestParam(defaultValue = "false", required = false) Boolean premiumUser,
                                             @RequestParam String hashCred,
                                             @RequestParam(defaultValue = "INR") UserMappingProto.Currency currency) {
        log.info("Obtained manual req for new user creation: {} x {} x {} x {} x {} x {}", mobile, email, user, premiumUser, hashCred, currency);
        return createUserMapping(generateCompleteUserMapping(mobile, email, user, premiumUser, hashCred, DEFAULT_EMAIL_HASH,
                DEFAULT_LAST_CLOUD_SAVE_TS, DEFAULT_LAST_SAVE_TS, DEFAULT_PAYMENT_EXPIRY_TS, DEFAULT_ACCOUNT_CREATION_TS, currency));
    }

    @GetMapping("/manual/generate/hash/cred")
    public String generateHashCredManually(@RequestParam String cred) {
        return authorizer.encode(cred);
    }

    @PostMapping(value = "/delete/user", consumes = APPLICATION_X_PROTOBUF)
    public Integer deleteUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for deletion for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Deleting user mapping: {} x {}", userMapping.getEmail(), userMapping.getUsername());
        return diurnalTableUserMapping.deleteEntity(emailHash);
    }

    @DeleteMapping("/manual/delete/user")
    public Integer deleteUserMappingManually(@RequestParam String email) {
        log.info("Obtained manual req for user deletion: {}", email);
        return deleteUserMapping(DiurnalUtil.generateUserMapping(email));
    }

    @PostMapping(value = "/update/user/name", consumes = APPLICATION_X_PROTOBUF)
    public Integer updateUserMappingName(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Updating user mapping: {} -> name: {}", userMapping.getEmail(), userMapping.getUsername());
        return diurnalTableUserMapping.updateUsername(emailHash, userMapping.getUsername());
    }

    @PatchMapping("/manual/update/user/name")
    public Integer updateUserMappingManually(@RequestParam String email,
                                             @RequestParam String updatedUserName) {
        email = refineEmail(email);
        log.info("Obtained manual req for user updation: {} -> name: {}", email, updatedUserName);
        return updateUserMappingName(generateUserMapping(email, updatedUserName));
    }

    @PostMapping(value = "/update/user/hash/cred", consumes = APPLICATION_X_PROTOBUF)
    public Integer updateUserMappingCred(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of hash cred for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Updating user mapping: {} -> cred: {}", userMapping.getEmail(), userMapping.getHashCred());
        return diurnalTableUserMapping.updateHashCred(emailHash, userMapping.getHashCred());
    }

    @PatchMapping("/manual/update/user/hash/cred")
    public Integer updateUserMappingCredManually(@RequestParam String email,
                                                 @RequestParam String hashCred) {
        email = refineEmail(email);
        log.info("Obtained manual req for user updation: {} -> cred: {}", email, hashCred);
        return updateUserMappingCred(generateUserMapping(DEFAULT_MOBILE, email, DEFAULT_USER_NAME, DEFAULT_PREMIUM_USER_STATUS, hashCred));
    }

    @PostMapping(value = "/update/user/mobile", consumes = APPLICATION_X_PROTOBUF)
    public Integer updateUserMappingMobile(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of mobile for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Updating user mapping: {} -> mobile: {}", userMapping.getEmail(), userMapping.getMobile());
        return diurnalTableUserMapping.updateMobile(emailHash, userMapping.getMobile());
    }

    @PatchMapping("/manual/update/user/mobile")
    public Integer updateUserMappingMobileManually(@RequestParam String email,
                                                   @RequestParam Long mobile) {
        email = refineEmail(email);
        log.info("Obtained manual req for user updation: {} -> mobile: {}", email, mobile);
        return updateUserMappingMobile(generateUserMapping(mobile, email, DEFAULT_USER_NAME, DEFAULT_PREMIUM_USER_STATUS, DEFAULT_USER_CRED_HASH));
    }

    @PostMapping(value = "/update/user/currency", consumes = APPLICATION_X_PROTOBUF)
    public Integer updateUserMappingCurrency(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of currency for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Updating user mapping: {} -> currency: {}", userMapping.getEmail(), userMapping.getCurrency());
        return diurnalTableUserMapping.updateCurrency(emailHash, userMapping.getCurrency());
    }

    @PatchMapping("/manual/update/user/currency")
    public Integer updateUserMappingCurrencyManually(@RequestParam String email,
                                                     @RequestParam(defaultValue = "INR") UserMappingProto.Currency currency) {
        email = refineEmail(email);
        log.info("Obtained manual req for user updation: {} -> currency: {}", email, currency);
        return updateUserMappingCurrency(generateUserMapping(email, currency));
    }

    @PostMapping(value = "/update/user/timestamp/save/cloud", consumes = APPLICATION_X_PROTOBUF)
    public Integer updateUserMappingLastCloudSaveTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        if (userMapping.getHashEmail() == 0) {
            log.warn("Emailhash not supplied in the user mapping: {}", userMapping);
            return 0;
        }
        log.info("Updating user mapping: {} -> last cloud save ts: {}", userMapping.getEmail(), userMapping.getLastCloudSaveTimestamp());
        return diurnalTableUserMapping.updateLastCloudSaveTimestamp(userMapping.getHashEmail(), userMapping.getLastCloudSaveTimestamp());
    }

    @PostMapping(value = "/update/user/timestamp/save/local", consumes = APPLICATION_X_PROTOBUF)
    public Integer updateUserMappingLastSaveTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of last local save ts for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Updating user mapping: {} -> last local save ts: {}", userMapping.getEmail(), userMapping.getLastSavedTimestamp());
        return diurnalTableUserMapping.updateLastSavedTimestamp(emailHash, userMapping.getLastSavedTimestamp());
    }

    @PostMapping(value = "/update/user/timestamp/payment/expiry", consumes = APPLICATION_X_PROTOBUF)
    public Integer updateUserMappingPaymentExpiryTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of payment expiry ts for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Updating user mapping: {} -> payment expiry ts: {}", userMapping.getEmail(), userMapping.getPaymentExpiryTimestamp());
        return diurnalTableUserMapping.updatePaymentExpiryTimestamp(emailHash, userMapping.getPaymentExpiryTimestamp());
    }

    @PostMapping("/manual/update/user/timestamp/payment/expiry")
    public Integer updateUserMappingPaymentExpiryTimestampManually(@RequestParam String email,
                                                                   @RequestParam Long paymentExpiryTimestamp) {
        UserMappingProto.UserMapping userMapping = UserMappingProto.UserMapping.newBuilder()
                .setEmail(email)
                .setPaymentExpiryTimestamp(paymentExpiryTimestamp)
                .build();
        return updateUserMappingPaymentExpiryTimestamp(userMapping);
    }

    @PostMapping(value = "/update/user/info", consumes = APPLICATION_X_PROTOBUF)
    public Boolean updateUserInfo(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of user info for email [{}]", userMapping.getEmail());
            return false;
        }
        log.info("Updating user mapping: {}", userMapping.getEmail());
        if (diurnalTableUserMapping.updateUsername(emailHash, userMapping.getUsername()) == ONE
                && diurnalTableUserMapping.updateMobile(emailHash, userMapping.getMobile()) == ONE
                && diurnalTableUserMapping.updateCurrency(emailHash, userMapping.getCurrency()) == ONE) {
            log.info("Successfully updated user info!");
            return true;
        }
        log.warn("Failed to update complete user info!!");
        return false;
    }

    @PatchMapping(value = "/update/user/premium", consumes = APPLICATION_X_PROTOBUF)
    public Integer updatePremiumUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        String email = refineEmail(userMapping.getEmail());
        Integer emailHash = retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of hash cred for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Obtained manual req for user updation: {} -> {}", email, userMapping.getPremiumUser());
        if (diurnalTableUserMapping.updatePaymentExpiryTimestamp(emailHash, userMapping.getPremiumUser() ? userMapping.getPaymentExpiryTimestamp() : DEFAULT_PAYMENT_EXPIRY_TS) == ONE
                && diurnalTableUserMapping.updatePremiumUserStatus(emailHash, userMapping.getPremiumUser()) == ONE) {
            log.info("Premium user updation complete for {}", emailHash);
            return ONE;
        }
        log.warn("Premium user updation failed for {}", emailHash);
        return NA_INT;
    }

    @PatchMapping("/manual/update/user/premium")
    public void updatePremiumUserMappingManually(@RequestParam String email,
                                                 @RequestParam(defaultValue = "0") Long paymentExpiryTimestamp,
                                                 @RequestParam(defaultValue = "false") Boolean premiumUserStatus) {
        UserMappingProto.UserMapping userMapping = generateUserMapping(DEFAULT_MOBILE, email, DEFAULT_USER_NAME, premiumUserStatus, DEFAULT_USER_CRED_HASH);
        UserMappingProto.UserMapping userMapping1 = UserMappingProto.UserMapping.newBuilder()
                .mergeFrom(userMapping)
                .setPaymentExpiryTimestamp(paymentExpiryTimestamp)
                .build();
        int result = updatePremiumUserMapping(userMapping1);
        log.info("Manual premium user update done => {}", result);
    }

    @GetMapping(value = "/retrieve/user", produces = APPLICATION_X_PROTOBUF)
    public UserMappingProto.UserMapping retrieveUserMapping(@RequestParam Integer emailHash) {
        log.info("Retrieving user details for [{}]", emailHash);
        UserMappingProto.UserMapping retrievedUserMapping = diurnalTableUserMapping.retrieveSingle(emailHash);
        log.info("Retrieved user detail for {}", emailHash);
        return retrievedUserMapping;
    }

    @GetMapping(value = "/retrieve/all/users", produces = APPLICATION_X_PROTOBUF)
    public UserMappingProto.UserMappingList retrieveAllUserMappings() {
        log.info("Retrieving all user mappings");
        UserMappingProto.UserMappingList userMappingList = diurnalTableUserMapping.retrieveAll();
        log.info("Result of retrieving all user mappings: {} entries", userMappingList.getUserMappingCount());
        return userMappingList;
    }

    @GetMapping("/manual/retrieve/all/users")
    public List<String> retrieveAllUserMappingsManually() {
        log.info("Obtained manual req for retrieving all user mappings");
        return performBulkOpStr(retrieveAllUserMappings().getUserMappingList(), AbstractMessage::toString);
    }

    @GetMapping("/retrieve/hash/cred")
    public String retrieveHashCred(@RequestParam Integer emailHash) {
        log.info("Retrieve cred-hash for: {}", emailHash);
        String retrievedCred = diurnalTableUserMapping.retrieveHashCred(emailHash);
        log.info("Result: [{}]", retrievedCred);
        return retrievedCred;
    }

    @GetMapping("/retrieve/hash/email")
    public Integer retrieveHashEmail(@RequestParam String email) {
        email = refineEmail(email);
        log.info("Retrieve email hash for: {}", email);
        return diurnalTableUserMapping.retrieveHashEmail(email);
    }

    public Boolean retrievePremiumUserStatus(@RequestParam Integer emailHash) {
        log.info("Retrieve premium user status for: {}", emailHash);
        return diurnalTableUserMapping.retrievePremiumUserStatus(emailHash);
    }

    @GetMapping("/retrieve/status/user-premium")
    public Boolean retrievePremiumUserStatusManually(@RequestParam String email) {
        email = refineEmail(email);
        Integer emailHash = retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for checking of premium status for email [{}]", email);
            return null;
        }
        return retrievePremiumUserStatus(emailHash);
    }

    @GetMapping(value = "/check/user", consumes = APPLICATION_X_PROTOBUF)
    public Boolean checkIfUserExists(@RequestBody UserMappingProto.UserMapping userMapping) {
        log.info("Checking if user exists for email: [{}]", userMapping.getEmail());
        String email = refineEmail(userMapping.getEmail());
        boolean checkIfUserExists = diurnalTableUserMapping.checkIfEntityExists(email);
        log.info("Result: {}", checkIfUserExists);
        return checkIfUserExists;
    }

    @GetMapping("/manual/check/user")
    public Boolean checkIfUserExistsManually(@RequestParam String email) {
        email = refineEmail(email);
        log.info("Checking if user exists for email: {}", email);
        return checkIfUserExists(DiurnalUtil.generateUserMapping(email));
    }

    @PutMapping("/upload/csv")
    public int uploadCsv(@RequestParam("csv-location") String csvLocation,
                         @RequestParam(value = "delimiter", defaultValue = ",") String delimiter) {
        AtomicInteger counter = new AtomicInteger(0);
        List<UserMappingEntity> userMappingEntities = FileUtil.readFileFromLocation(csvLocation).stream()
                .map(data -> {
                    if (counter.get() == 0) data = data.substring(1);
                    counter.incrementAndGet();
                    String[] vals = data.split(delimiter);

                    long mobile = Long.parseLong(vals[0].trim());
                    String email = vals[1];
                    String user = vals[2];
                    boolean premium = "t".equals(vals[3]);
                    String credHash = vals[4];
                    int emailHash = Integer.parseInt(vals[5].trim());
                    Instant cloudTs = Instant.parse(vals[6]);
                    Instant saveTs = Instant.parse(vals[7]);
                    Instant pymtTs = Instant.parse(vals[8]);
                    Instant crtTs = Instant.parse(vals[9]);
                    String curr = vals[10];
                    return new UserMappingEntity()
                            .setMobile(mobile)
                            .setEmail(email)
                            .setUser(user)
                            .setPremiumUser(premium)
                            .setCredHash(credHash)
                            .setEmailHash(emailHash)
                            .setLastCloudSaveTimestamp(cloudTs)
                            .setLastSaveTimestamp(saveTs)
                            .setPaymentExpiryTimestamp(pymtTs)
                            .setAccountCreationTimestamp(crtTs)
                            .setCurrency(curr)
                            ;
                }).collect(Collectors.toList());
        log.info("Extracted {} entities from '{}'", userMappingEntities.size(), csvLocation);
        int saved = diurnalTableUserMapping.pushNewEntities(userMappingEntities);
        log.info("Saved {} into db", saved);
        return saved;
    }

    @PutMapping("/backup/github/csv")
    public boolean backupUserMappingDataToGitHubInCsv(@RequestParam(name = "delimiter", defaultValue = ",") String delimiter) {
        StopWatch stopWatch = beanStore.procureStopWatch();
        String dataLines = diurnalTableUserMapping.processDataToCsv(delimiter);
        boolean compute = gitHubUserMappingFeignClient.backupAndUploadToGitHub(dataLines);
        stopWatch.stop();
        log.info("Took {} ms to complete user_mapping table backup from user-mapping controller. Result: {}", stopWatch.getTime(TimeUnit.MILLISECONDS), compute);
        return compute;
    }

    Instant getTrialEndPeriod(int trialDays) {
        return Instant.now().plus(trialDays, ChronoUnit.DAYS);
    }
}