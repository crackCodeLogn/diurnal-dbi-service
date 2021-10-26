package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.auth.Authorizer;
import com.vv.personal.diurnal.dbi.config.DbiConfig;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableUserMapping;
import com.vv.personal.diurnal.dbi.model.UserMappingEntity;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.*;

/**
 * @author Vivek
 * @since 23/02/21
 */
@Slf4j
@RestController("user-mapping-controller")
@RequestMapping("/diurnal/mapping-user")
public class UserMappingController {

    @Autowired
    @Qualifier("DiurnalTableUserMapping")
    private DiurnalTableUserMapping diurnalTableUserMapping;
    @Autowired
    private Authorizer authorizer;
    @Autowired
    private DbiConfig dbiConfig;

    @ApiOperation(value = "create user", hidden = true)
    @PostMapping("/create/user")
    public Integer createUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        log.info("Creating new user mapping: {} x {} x {} x {}", userMapping.getMobile(), userMapping.getEmail(), userMapping.getUsername(), userMapping.getPremiumUser());
        ZonedDateTime currentZoned = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(dbiConfig.getComputeTimezone(), ZoneId.SHORT_IDS));
        ZonedDateTime trialPremiumPaymentExpiryZoned = currentZoned.plusDays(dbiConfig.getTrialPeriodDays());
        final UserMappingEntity userMappingEntity = new UserMappingEntity()
                .setMobile(userMapping.getMobile())
                .setEmail(refineEmail(userMapping.getEmail()))
                .setUser(userMapping.getUsername())
                .setPremiumUser(true)
                .setCredHash(userMapping.getHashCred())
                .setEmailHash(generateHash(userMapping.getEmail()))
                .setLastCloudSaveTimestamp(DEFAULT_INSTANT_DATETIME)
                .setLastSaveTimestamp(DEFAULT_INSTANT_DATETIME)
                .setAccountCreationTimestamp(currentZoned.toInstant())
                .setPaymentExpiryTimestamp(trialPremiumPaymentExpiryZoned.toInstant())
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

    @ApiOperation(value = "delete user", hidden = true)
    @PostMapping("/delete/user")
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

    @ApiOperation(value = "update user-name", hidden = true)
    @PostMapping("/update/user/name")
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

    @ApiOperation(value = "update user-cred", hidden = true)
    @PostMapping("/update/user/hash/cred")
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

    @ApiOperation(value = "update user-mobile", hidden = true)
    @PostMapping("/update/user/mobile")
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

    @ApiOperation(value = "update user-currency", hidden = true)
    @PostMapping("/update/user/currency")
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

    @ApiOperation(value = "update user-cloud save ts", hidden = true)
    @PostMapping("/update/user/timestamp/save/cloud")
    public Integer updateUserMappingLastCloudSaveTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        if (userMapping.getHashEmail() == 0) {
            log.warn("Emailhash not supplied in the user mapping: {}", userMapping);
            return 0;
        }
        log.info("Updating user mapping: {} -> last cloud save ts: {}", userMapping.getEmail(), userMapping.getLastCloudSaveTimestamp());
        return diurnalTableUserMapping.updateLastCloudSaveTimestamp(userMapping.getHashEmail(), userMapping.getLastCloudSaveTimestamp());
    }

    @ApiOperation(value = "update user-save ts", hidden = true)
    @PostMapping("/update/user/timestamp/save/local")
    public Integer updateUserMappingLastSaveTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of last local save ts for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Updating user mapping: {} -> last local save ts: {}", userMapping.getEmail(), userMapping.getLastSavedTimestamp());
        return diurnalTableUserMapping.updateLastSavedTimestamp(emailHash, userMapping.getLastSavedTimestamp());
    }

    @ApiOperation(value = "update user-payment expiry ts", hidden = true)
    @PostMapping("/update/user/timestamp/payment/expiry")
    public Integer updateUserMappingPaymentExpiryTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of payment expiry ts for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Updating user mapping: {} -> payment expiry ts: {}", userMapping.getEmail(), userMapping.getPaymentExpiryTimestamp());
        return diurnalTableUserMapping.updatePaymentExpiryTimestamp(emailHash, userMapping.getPaymentExpiryTimestamp());
    }

    @ApiOperation(value = "update user-payment expiry ts")
    @PostMapping("/manual/update/user/timestamp/payment/expiry")
    public Integer updateUserMappingPaymentExpiryTimestampManually(@RequestParam String email,
                                                                   @RequestParam Long paymentExpiryTimestamp) {
        UserMappingProto.UserMapping userMapping = UserMappingProto.UserMapping.newBuilder()
                .setEmail(email)
                .setPaymentExpiryTimestamp(paymentExpiryTimestamp)
                .build();
        return updateUserMappingPaymentExpiryTimestamp(userMapping);
    }

    @ApiOperation(value = "update user-info for name, mobile and currency", hidden = true)
    @PostMapping("/update/user/info")
    public Boolean updateUserInfo(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of user info for email [{}]", userMapping.getEmail());
            return false;
        }
        log.info("Updating user mapping: {}", userMapping.getEmail());
        UserMappingProto.UserMapping inflatedUserMapping = generateCompleteUserMapping(userMapping, emailHash);
        if (diurnalTableUserMapping.updateUsername(emailHash, userMapping.getUsername()) == ONE
                && diurnalTableUserMapping.updateMobile(emailHash, userMapping.getMobile()) == ONE
                && diurnalTableUserMapping.updateCurrency(emailHash, userMapping.getCurrency()) == ONE) {
            log.info("Successfully updated user info!");
            return true;
        }
        log.warn("Failed to update complete user info!!");
        return false;
    }

    @ApiOperation(value = "user premium updation", hidden = true)
    @PatchMapping("/update/user/premium")
    public Integer updatePremiumUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        String email = refineEmail(userMapping.getEmail());
        Integer emailHash = retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for updation of hash cred for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Obtained manual req for user updation: {} -> {}", email, userMapping.getPremiumUser());
        Integer sqlResult = diurnalTableUserMapping.updatePremiumUserStatus(emailHash, userMapping.getPremiumUser());
        log.info("Result of premium-user updation: {}", sqlResult);
        return sqlResult;
    }

    @PatchMapping("/manual/update/user/premium")
    public void updatePremiumUserMappingManually(@RequestParam String email,
                                                 @RequestParam(defaultValue = "false") Boolean premiumUserStatus) {
        UserMappingProto.UserMapping userMapping = generateUserMapping(DEFAULT_MOBILE, email, DEFAULT_USER_NAME, premiumUserStatus, DEFAULT_USER_CRED_HASH);
        int result = updatePremiumUserMapping(userMapping);
        log.info("Manual premium user update done => {}", result);
    }

    @ApiOperation(value = "retrieve user detail", hidden = true)
    @GetMapping("/retrieve/user")
    public UserMappingProto.UserMapping retrieveUserMapping(@RequestParam Integer emailHash) {
        log.info("Retrieving user details for [{}]", emailHash);
        UserMappingProto.UserMapping retrievedUserMapping = diurnalTableUserMapping.retrieveSingle(emailHash);
        log.info("Retrieved user detail for {}", emailHash);
        return retrievedUserMapping;
    }

    @ApiOperation(value = "retrieve all users", hidden = true)
    @GetMapping("/retrieve/all/users")
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

    @ApiOperation(value = "retrieve hashed cred from db")
    @GetMapping("/retrieve/hash/cred")
    public String retrieveHashCred(@RequestParam Integer emailHash) {
        log.info("Retrieve cred-hash for: {}", emailHash);
        String retrievedCred = diurnalTableUserMapping.retrieveHashCred(emailHash);
        log.info("Result: [{}]", retrievedCred);
        return retrievedCred;
    }

    @ApiOperation(value = "retrieve hashed email from db", notes = "This will be internal to dbi-service only.")
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

    @ApiOperation(value = "retrieve premium user status from db")
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

    @ApiOperation(value = "check if user exists", hidden = true)
    @GetMapping("/check/user")
    public Boolean checkIfUserExists(@RequestParam UserMappingProto.UserMapping userMapping) {
        log.info("Checking if user exists for email: [{}]", userMapping.getEmail());
        String email = refineEmail(userMapping.getEmail());
        Integer emailHash = retrieveHashEmail(email);
        boolean checkIfUserExists = !isEmailHashAbsent(emailHash);
        log.info("Result: {}", checkIfUserExists);
        return checkIfUserExists;
    }

    @GetMapping("/manual/check/user")
    public Boolean checkIfUserExistsManually(@RequestParam String email) {
        email = refineEmail(email);
        log.info("Checking if user exists for email: {}", email);
        return checkIfUserExists(DiurnalUtil.generateUserMapping(email));
    }
}