package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.auth.Authorizer;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableUserMapping;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.*;

/**
 * @author Vivek
 * @since 23/02/21
 */
@RestController("user-mapping-controller")
@RequestMapping("/diurnal/mapping-user")
public class UserMappingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserMappingController.class);

    @Autowired
    @Qualifier("DiurnalTableUserMapping")
    private DiurnalTableUserMapping diurnalTableUserMapping;

    @Autowired
    private Authorizer authorizer;

    @ApiOperation(value = "create user", hidden = true)
    @PostMapping("/create/user")
    public Integer createUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Creating new user mapping: {} x {} x {} x {}", userMapping.getMobile(), userMapping.getEmail(), userMapping.getUsername(), userMapping.getPremiumUser());
        Integer sqlResult = diurnalTableUserMapping.pushNewEntity(userMapping);
        LOGGER.info("Result of new user creation: {}", sqlResult);
        return sqlResult;
    }

    @PutMapping("/manual/create/user")
    public Integer createUserMappingManually(@RequestParam(defaultValue = "-1", required = false) Long mobile,
                                             @RequestParam String email,
                                             @RequestParam String user,
                                             @RequestParam(defaultValue = "false", required = false) Boolean premiumUser,
                                             @RequestParam String hashCred,
                                             @RequestParam(defaultValue = "INR") UserMappingProto.Currency currency) {
        email = refineEmail(email);
        LOGGER.info("Obtained manual req for new user creation: {} x {} x {} x {} x {} x {}", mobile, email, user, premiumUser, hashCred, currency);
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
            LOGGER.warn("User not found for deletion for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Deleting user mapping: {} x {}", userMapping.getEmail(), userMapping.getUsername());
        Integer sqlResult = diurnalTableUserMapping.deleteEntity(generateUserMappingOnPk(emailHash)); //uses hash email
        LOGGER.info("Result of user deletion: {}", sqlResult);
        return sqlResult;
    }

    @DeleteMapping("/manual/delete/user")
    public Integer deleteUserMappingManually(@RequestParam String email) {
        email = refineEmail(email);
        LOGGER.info("Obtained manual req for user deletion: {}", email);
        return deleteUserMapping(DiurnalUtil.generateUserMapping(email));
    }

    @ApiOperation(value = "update user-name", hidden = true)
    @PostMapping("/update/user/name")
    public Integer updateUserMappingName(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Updating user mapping: {} -> name: {}", userMapping.getEmail(), userMapping.getUsername());
        Integer sqlResult = diurnalTableUserMapping.updateEntity(generateCompleteUserMapping(userMapping, emailHash));
        LOGGER.debug("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @PatchMapping("/manual/update/user/name")
    public Integer updateUserMappingManually(@RequestParam String email,
                                             @RequestParam String updatedUserName) {
        email = refineEmail(email);
        LOGGER.info("Obtained manual req for user updation: {} -> name: {}", email, updatedUserName);
        return updateUserMappingName(generateUserMapping(email, updatedUserName));
    }

    @ApiOperation(value = "update user-cred", hidden = true)
    @PostMapping("/update/user/hash/cred")
    public Integer updateUserMappingCred(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation of hash cred for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Updating user mapping: {} -> cred: {}", userMapping.getEmail(), userMapping.getHashCred());
        Integer sqlResult = diurnalTableUserMapping.updateHashCred(generateCompleteUserMapping(userMapping, emailHash));
        LOGGER.debug("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @PatchMapping("/manual/update/user/hash/cred")
    public Integer updateUserMappingCredManually(@RequestParam String email,
                                                 @RequestParam String hashCred) {
        email = refineEmail(email);
        LOGGER.info("Obtained manual req for user updation: {} -> cred: {}", email, hashCred);
        return updateUserMappingCred(generateUserMapping(DEFAULT_MOBILE, email, DEFAULT_USER_NAME, DEFAULT_PREMIUM_USER_STATUS, hashCred));
    }

    @ApiOperation(value = "update user-mobile", hidden = true)
    @PostMapping("/update/user/mobile")
    public Integer updateUserMappingMobile(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation of mobile for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Updating user mapping: {} -> mobile: {}", userMapping.getEmail(), userMapping.getMobile());
        Integer sqlResult = diurnalTableUserMapping.updateMobile(generateCompleteUserMapping(userMapping, emailHash));
        LOGGER.debug("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @PatchMapping("/manual/update/user/mobile")
    public Integer updateUserMappingMobileManually(@RequestParam String email,
                                                   @RequestParam Long mobile) {
        email = refineEmail(email);
        LOGGER.info("Obtained manual req for user updation: {} -> mobile: {}", email, mobile);
        return updateUserMappingMobile(generateUserMapping(mobile, email, DEFAULT_USER_NAME, DEFAULT_PREMIUM_USER_STATUS, DEFAULT_USER_CRED_HASH));
    }

    @ApiOperation(value = "update user-currency", hidden = true)
    @PostMapping("/update/user/currency")
    public Integer updateUserMappingCurrency(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation of currency for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Updating user mapping: {} -> currency: {}", userMapping.getEmail(), userMapping.getCurrency());
        Integer sqlResult = diurnalTableUserMapping.updateCurrency(generateCompleteUserMapping(userMapping, emailHash));
        LOGGER.debug("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @PatchMapping("/manual/update/user/currency")
    public Integer updateUserMappingCurrencyManually(@RequestParam String email,
                                                     @RequestParam(defaultValue = "INR") UserMappingProto.Currency currency) {
        email = refineEmail(email);
        LOGGER.info("Obtained manual req for user updation: {} -> currency: {}", email, currency);
        return updateUserMappingCurrency(generateUserMapping(email, currency));
    }

    @ApiOperation(value = "update user-cloud save ts", hidden = true)
    @PostMapping("/update/user/timestamp/save/cloud")
    public Integer updateUserMappingLastCloudSaveTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        if (userMapping.getHashEmail() == 0) {
            LOGGER.warn("Emailhash not supplied in the user mapping: {}", userMapping);
            return 0;
        }
        LOGGER.info("Updating user mapping: {} -> last cloud save ts: {}", userMapping.getEmail(), userMapping.getLastCloudSaveTimestamp());
        Integer sqlResult = diurnalTableUserMapping.updateLastCloudSaveTimestamp(userMapping);
        LOGGER.debug("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "update user-save ts", hidden = true)
    @PostMapping("/update/user/timestamp/save/local")
    public Integer updateUserMappingLastSaveTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation of last local save ts for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Updating user mapping: {} -> last local save ts: {}", userMapping.getEmail(), userMapping.getLastSavedTimestamp());
        Integer sqlResult = diurnalTableUserMapping.updateLastSavedTimestamp(generateCompleteUserMapping(userMapping, emailHash));
        LOGGER.debug("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "update user-payment expiry ts", hidden = true)
    @PostMapping("/update/user/timestamp/payment/expiry")
    public Integer updateUserMappingPaymentExpiryTimestamp(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation of payment expiry ts for email [{}]", userMapping.getEmail());
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Updating user mapping: {} -> payment expiry ts: {}", userMapping.getEmail(), userMapping.getPaymentExpiryTimestamp());
        Integer sqlResult = diurnalTableUserMapping.updatePaymentExpiryTimestamp(generateCompleteUserMapping(userMapping, emailHash));
        LOGGER.debug("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "update user-info for name, mobile and currency", hidden = true)
    @PostMapping("/update/user/info")
    public Boolean updateUserInfo(@RequestBody UserMappingProto.UserMapping userMapping) {
        Integer emailHash = retrieveHashEmail(userMapping.getEmail());
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation of user info for email [{}]", userMapping.getEmail());
            return false;
        }
        LOGGER.info("Updating user mapping: {}", userMapping.getEmail());
        UserMappingProto.UserMapping inflatedUserMapping = generateCompleteUserMapping(userMapping, emailHash);
        if (diurnalTableUserMapping.updateEntity(inflatedUserMapping) == ONE
                && diurnalTableUserMapping.updateMobile(inflatedUserMapping) == ONE
                && diurnalTableUserMapping.updateCurrency(inflatedUserMapping) == ONE) {
            LOGGER.info("Successfully updated user info!");
            return true;
        }
        LOGGER.warn("Failed to update complete user info!!");
        return false;
    }

    @ApiOperation(value = "update user-acc creation ts, NOT TO BE USED GENERALLY")
    @PostMapping("/manual/update/user/timestamp/account/creation")
    public Integer updateUserMappingAccountCreationTimestamp(@RequestParam String email,
                                                             @RequestParam Long newAccountCreationTimestamp) {
        Integer emailHash = retrieveHashEmail(refineEmail(email));
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation of acc creation ts for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Updating user mapping: {} -> acc creation ts: {}", email, newAccountCreationTimestamp);
        Integer sqlResult = diurnalTableUserMapping.updateAccountCreationTimestamp(generateCompleteUserMapping(
                DEFAULT_MOBILE, refineEmail(email), DEFAULT_USER_NAME, DEFAULT_PREMIUM_USER_STATUS, DEFAULT_USER_CRED_HASH, emailHash,
                DEFAULT_LAST_CLOUD_SAVE_TS, DEFAULT_LAST_SAVE_TS, DEFAULT_PAYMENT_EXPIRY_TS, newAccountCreationTimestamp, DEFAULT_CURRENCY
        ));
        LOGGER.debug("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @PatchMapping("/manual/update/user/premium")
    public Integer updatePremiumUserMappingManually(@RequestParam String email,
                                                    @RequestParam(defaultValue = "false") Boolean premiumUserStatus) {
        email = refineEmail(email);
        Integer emailHash = retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation of hash cred for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Obtained manual req for user updation: {} -> {}", email, premiumUserStatus);
        UserMappingProto.UserMapping userMapping = generateUserMapping(premiumUserStatus, emailHash);
        Integer sqlResult = diurnalTableUserMapping.updatePremiumUserStatus(userMapping);
        LOGGER.info("Result of premium-user updation: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "retrieve user detail", hidden = true)
    @GetMapping("/retrieve/user")
    public UserMappingProto.UserMapping retrieveUserMapping(@RequestParam Integer emailHash) {
        LOGGER.info("Retrieving user details for [{}]", emailHash);
        UserMappingProto.UserMapping retrievedUserMapping = diurnalTableUserMapping.retrieveSingle(
                generateUserMappingOnPk(emailHash));
        LOGGER.info("Retrieved user detail");
        return retrievedUserMapping;
    }

    @ApiOperation(value = "retrieve all users", hidden = true)
    @GetMapping("/retrieve/all/users")
    public UserMappingProto.UserMappingList retrieveAllUserMappings() {
        LOGGER.info("Retrieving all user mappings");
        UserMappingProto.UserMappingList userMappingList = diurnalTableUserMapping.retrieveAll();
        LOGGER.info("Result of retrieving all user mappings: {} entries", userMappingList.getUserMappingCount());
        return userMappingList;
    }

    @GetMapping("/manual/retrieve/all/users")
    public List<String> retrieveAllUserMappingsManually() {
        LOGGER.info("Obtained manual req for retrieving all user mappings");
        return performBulkOpStr(retrieveAllUserMappings().getUserMappingList(), AbstractMessage::toString);
    }

    @ApiOperation(value = "retrieve hashed cred from db")
    @GetMapping("/retrieve/hash/cred")
    public String retrieveHashCred(@RequestParam Integer emailHash) {
        LOGGER.info("Retrieve cred-hash for: {}", emailHash);
        UserMappingProto.UserMapping userMapping = generateUserMappingOnPk(emailHash);
        String retrievedCred = diurnalTableUserMapping.retrieveHashCred(userMapping);
        LOGGER.info("Result: [{}]", retrievedCred);
        return retrievedCred;
    }

    @ApiOperation(value = "retrieve hashed email from db", notes = "This will be internal to dbi-service only.")
    @GetMapping("/retrieve/hash/email")
    public Integer retrieveHashEmail(@RequestParam String email) {
        email = refineEmail(email);
        LOGGER.info("Retrieve email hash for: {}", email);
        Integer retrievedHashEmail = diurnalTableUserMapping.retrieveHashEmail(generateUserMapping(email));
        LOGGER.info("Result: [{}]", retrievedHashEmail);
        return retrievedHashEmail;
    }

    public Boolean retrievePremiumUserStatus(@RequestParam Integer emailHash) {
        LOGGER.info("Retrieve premium user status for: {}", emailHash);
        UserMappingProto.UserMapping userMapping = generateUserMappingOnPk(emailHash);
        Boolean premiumUserStatus = diurnalTableUserMapping.retrievePremiumUserStatus(userMapping);
        LOGGER.info("Result: [{}]", premiumUserStatus);
        return premiumUserStatus;
    }

    @ApiOperation(value = "retrieve premium user status from db")
    @GetMapping("/retrieve/status/user-premium")
    public Boolean retrievePremiumUserStatusManually(@RequestParam String email) {
        email = refineEmail(email);
        Integer emailHash = retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for checking of premium status for email [{}]", email);
            return null;
        }
        return retrievePremiumUserStatus(emailHash);
    }

    @ApiOperation(value = "check if user exists", hidden = true)
    @GetMapping("/check/user")
    public Boolean checkIfUserExists(@RequestParam UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Checking if user exists for email: [{}]", userMapping.getEmail());
        String email = refineEmail(userMapping.getEmail());
        Integer emailHash = retrieveHashEmail(email);
        boolean checkIfUserExists = !isEmailHashAbsent(emailHash);
        LOGGER.info("Result: {}", checkIfUserExists);
        return checkIfUserExists;
    }

    @GetMapping("/manual/check/user")
    public Boolean checkIfUserExistsManually(@RequestParam String email) {
        email = refineEmail(email);
        LOGGER.info("Checking if user exists for email: {}", email);
        return checkIfUserExists(DiurnalUtil.generateUserMapping(email));
    }

    @GetMapping("/manual/dump/table/csv/")
    public String dumpTableAsCsv() {
        LOGGER.info("Dumping content of table '{}' onto csv now", diurnalTableUserMapping.getTableName());
        String csvFileLocation = diurnalTableUserMapping.dumpTableToCsv();
        LOGGER.info("Csv file location of the dump => [{}]", csvFileLocation);
        return csvFileLocation;
    }

    @PutMapping("/table/create")
    public int createTableIfNotExists() {
        return genericCreateTableIfNotExists(diurnalTableUserMapping);
    }

    @DeleteMapping("/table/drop")
    public Boolean dropTable(@RequestParam(defaultValue = "false") Boolean absolutelyDropTable) {
        return absolutelyDropTable ? genericDropTable(diurnalTableUserMapping) : false;
    }

    @DeleteMapping("/table/truncate")
    public Boolean truncateTable(@RequestParam(defaultValue = "false") Boolean absolutelyTruncateTable) {
        return absolutelyTruncateTable ? genericTruncateTable(diurnalTableUserMapping) : false;
    }
}
