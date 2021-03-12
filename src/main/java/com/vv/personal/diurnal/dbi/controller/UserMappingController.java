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
        LOGGER.info("Creating new user mapping: {} x {} x {} x {}", userMapping.getMobile(), userMapping.getEmail(), userMapping.getUsername(), userMapping.getPowerUser());
        Integer sqlResult = diurnalTableUserMapping.pushNewEntity(userMapping);
        LOGGER.info("Result of new user creation: {}", sqlResult);
        return sqlResult;
    }

    @PutMapping("/manual/create/user")
    public Integer createUserMappingManually(@RequestParam(defaultValue = "-1", required = false) Long mobile,
                                             @RequestParam String email,
                                             @RequestParam String user,
                                             @RequestParam(defaultValue = "false", required = false) Boolean powerUser,
                                             @RequestParam String hashCred) {
        email = refineEmail(email);
        LOGGER.info("Obtained manual req for new user creation: {} x {} x {} x {} x {}", mobile, email, user, powerUser, hashCred);
        return createUserMapping(generateCompleteUserMapping(mobile, email, user, powerUser, hashCred,
                generateHash(email)));
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
        LOGGER.info("Result of user updation: {}", sqlResult);
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
        LOGGER.info("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @PatchMapping("/manual/update/user/hash/cred")
    public Integer updateUserMappingCredManually(@RequestParam String email,
                                                 @RequestParam String hashCred) {
        email = refineEmail(email);
        LOGGER.info("Obtained manual req for user updation: {} -> cred: {}", email, hashCred);
        return updateUserMappingCred(generateUserMapping(NA_LONG, email, EMPTY_STR, false, hashCred));
    }

    @PatchMapping("/manual/update/user-power")
    public Integer updatePowerUserMappingManually(@RequestParam String email,
                                                  @RequestParam Boolean powerUserStatus) {
        email = refineEmail(email);
        Integer emailHash = retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for updation of hash cred for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Obtained manual req for user updation: {} -> {}", email, powerUserStatus);
        UserMappingProto.UserMapping userMapping = generateCompleteUserMapping(NA_LONG, email, EMPTY_STR, powerUserStatus, EMPTY_STR, emailHash);
        Integer sqlResult = diurnalTableUserMapping.updatePowerUserStatus(userMapping);
        LOGGER.info("Result of power-user updation: {}", sqlResult);
        return sqlResult;
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

    @ApiOperation(value = "retrieve power user status from db")
    @GetMapping("/retrieve/status/user-power")
    public Boolean retrievePowerUserStatus(@RequestParam Integer emailHash) {
        LOGGER.info("Retrieve power user status for: {}", emailHash);
        UserMappingProto.UserMapping userMapping = generateUserMappingOnPk(emailHash);
        Boolean powerUserStatus = diurnalTableUserMapping.retrievePowerUserStatus(userMapping);
        LOGGER.info("Result: [{}]", powerUserStatus);
        return powerUserStatus;
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
