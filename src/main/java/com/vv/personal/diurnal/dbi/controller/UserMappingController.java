package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableUserMapping;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_STR;
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

    @ApiOperation(value = "create user", hidden = true)
    @PostMapping("/create/user")
    public Integer createUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Creating new user mapping: {} x {} x {}", userMapping.getMobile(), userMapping.getUsername(), userMapping.getPowerUser());
        Integer sqlResult = diurnalTableUserMapping.pushNewEntity(userMapping);
        LOGGER.info("Result of new user creation: {}", sqlResult);
        return sqlResult;
    }

    @PutMapping("/create/manual/user")
    public Integer createUserMappingManually(@RequestParam Long mobile,
                                             @RequestParam String user,
                                             @RequestParam(defaultValue = "false", required = false) Boolean powerUser,
                                             //@Parameter(schema = @Schema(type = "string", format = "password")) String hashedCred) {
                                             @RequestParam String hashedCred) {
        LOGGER.info("Obtained manual req for new user creation: {} x {} x {} x {}", mobile, user, powerUser, hashedCred);
        return createUserMapping(generateUserMapping(mobile, user, powerUser, hashedCred));
    }

    @ApiOperation(value = "delete user", hidden = true)
    @PostMapping("/delete/user")
    public Integer deleteUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Deleting user mapping: {} x {}", userMapping.getMobile(), userMapping.getUsername());
        Integer sqlResult = diurnalTableUserMapping.deleteEntity(userMapping);
        LOGGER.info("Result of user deletion: {}", sqlResult);
        return sqlResult;
    }

    @DeleteMapping("/delete/manual/user")
    public Integer deleteUserMappingManually(@RequestParam Long mobile) {
        LOGGER.info("Obtained manual req for user deletion: {}", mobile);
        return deleteUserMapping(generateUserMappingOnPk(mobile));
    }

    @ApiOperation(value = "update user-name", hidden = true)
    @PostMapping("/update/user/name")
    public Integer updateUserMappingName(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Updating user mapping: {} -> name: {}", userMapping.getMobile(), userMapping.getUsername());
        Integer sqlResult = diurnalTableUserMapping.updateEntity(userMapping);
        LOGGER.info("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @PatchMapping("/update/manual/user/name")
    public Integer updateUserMappingManually(@RequestParam Long mobile,
                                             @RequestParam String updatedUserName) {
        LOGGER.info("Obtained manual req for user updation: {} -> name: {}", mobile, updatedUserName);
        return updateUserMappingName(generateUserMapping(mobile, updatedUserName));
    }

    @ApiOperation(value = "update user-cred", hidden = true)
    @PostMapping("/update/user/cred")
    public Integer updateUserMappingCred(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Updating user mapping: {} -> cred: {}", userMapping.getMobile(), userMapping.getCred());
        Integer sqlResult = diurnalTableUserMapping.updateCred(userMapping);
        LOGGER.info("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @PatchMapping("/update/manual/user/cred")
    public Integer updateUserMappingCredManually(@RequestParam Long mobile,
                                                 @RequestParam String hashedCred) {
        LOGGER.info("Obtained manual req for user updation: {} -> cred: {}", mobile, hashedCred);
        return updateUserMappingCred(generateUserMapping(mobile, EMPTY_STR, false, hashedCred));
    }

    @PatchMapping("/update/manual/user-power")
    public Integer updatePowerUserMappingManually(@RequestParam Long mobile,
                                                  @RequestParam Boolean powerUserStatus) {
        LOGGER.info("Obtained manual req for user updation: {} -> {}", mobile, powerUserStatus);
        UserMappingProto.UserMapping userMapping = generateUserMapping(mobile, EMPTY_STR, powerUserStatus, EMPTY_STR);
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

    @GetMapping("/retrieve/all/manual/users")
    public List<String> retrieveAllUserMappingsManually() {
        LOGGER.info("Obtained manual req for retrieving all user mappings");
        return performBulkOpStr(retrieveAllUserMappings().getUserMappingList(), AbstractMessage::toString);
    }

    @ApiOperation(value = "retrieve hashed cred from db", hidden = true)
    @GetMapping("/retrieve/cred")
    public ResponsePrimitiveProto.ResponsePrimitive retrieveCredential(@RequestParam UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Retrieve cred for: {}", userMapping.getMobile());
        String retrieveCred = diurnalTableUserMapping.retrieveCred(userMapping);
        LOGGER.info("Result: [{}]", retrieveCred);
        return generateResponsePrimitiveString(retrieveCred);
    }

    @GetMapping("/retrieve/manual/cred")
    public String verifyCredentialManually(@RequestParam Long mobile) {
        return retrieveCredential(generateUserMappingOnPk(mobile)).getResponse();
    }

    @ApiOperation(value = "check if user exists", hidden = true)
    @GetMapping("/check/user")
    public Boolean checkIfUserExists(@RequestParam UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Checking if user exists for mobile: {}", userMapping.getMobile());
        boolean checkIfUserExists = diurnalTableUserMapping.checkEntity(userMapping);
        LOGGER.info("Result: {}", checkIfUserExists);
        return checkIfUserExists;
    }

    @GetMapping("/check/manual/user")
    public Boolean checkIfUserExistsManually(@RequestParam Long mobile) {
        LOGGER.info("Checking if user exists for mobile: {}", mobile);
        return checkIfUserExists(generateUserMappingOnPk(mobile));
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
