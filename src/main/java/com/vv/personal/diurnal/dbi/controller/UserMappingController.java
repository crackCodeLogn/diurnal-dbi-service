package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.interactor.diurnal.DiurnalTableUserMapping;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        LOGGER.info("Creating new user mapping: {} x {}", userMapping.getMobile(), userMapping.getUsername());
        Integer sqlResult = diurnalTableUserMapping.pushNewEntity(userMapping);
        LOGGER.info("Result of new user creation: {}", sqlResult);
        return sqlResult;
    }

    @GetMapping("/create/manual/user")
    public Integer createUserMappingManually(@RequestParam Long mobile,
                                             @RequestParam String user) {
        LOGGER.info("Obtained manual req for new user creation: {} x {}", mobile, user);
        return createUserMapping(UserMappingProto.UserMapping.newBuilder()
                .setMobile(mobile)
                .setUsername(user)
                .build());
    }

    @ApiOperation(value = "delete user", hidden = true)
    @PostMapping("/delete/user")
    public Integer deleteUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Deleting user mapping: {} x {}", userMapping.getMobile(), userMapping.getUsername());
        Integer sqlResult = diurnalTableUserMapping.deleteEntity(userMapping);
        LOGGER.info("Result of user deletion: {}", sqlResult);
        return sqlResult;
    }

    @GetMapping("/delete/manual/user")
    public Integer deleteUserMappingManually(@RequestParam Long mobile) {
        LOGGER.info("Obtained manual req for user deletion: {}", mobile);
        return deleteUserMapping(UserMappingProto.UserMapping.newBuilder()
                .setMobile(mobile)
                .build());
    }

    @ApiOperation(value = "update user", hidden = true)
    @PostMapping("/update/user")
    public Integer updateUserMapping(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Updating user mapping: {} -> {}", userMapping.getMobile(), userMapping.getUsername());
        Integer sqlResult = diurnalTableUserMapping.updateEntity(userMapping);
        LOGGER.info("Result of user updation: {}", sqlResult);
        return sqlResult;
    }

    @GetMapping("/update/manual/user")
    public Integer updateUserMappingManually(@RequestParam Long mobile,
                                             @RequestParam String updatedUserName) {
        LOGGER.info("Obtained manual req for user updation: {} -> {}", mobile, updatedUserName);
        return updateUserMapping(UserMappingProto.UserMapping.newBuilder()
                .setMobile(mobile)
                .setUsername(updatedUserName)
                .build());
    }

    @ApiOperation(value = "retrieve all users", hidden = true)
    @GetMapping("/retrieve/all/users")
    public UserMappingProto.UserMappingList retrieveAllUserMappings() {
        LOGGER.info("Retrieving all user mappings");
        UserMappingProto.UserMappingList userMappingList = diurnalTableUserMapping.retrieveAll();
        LOGGER.info("Result of retrieving all user mappings: {} entries", userMappingList.getUserMappingsCount());
        return userMappingList;
    }

    @GetMapping("/retrieve/all/manual/users")
    public List<String> retrieveAllUserMappingsManually() {
        LOGGER.info("Obtained manual req for retrieving all user mappings");
        return retrieveAllUserMappings().getUserMappingsList().stream()
                .map(AbstractMessage::toString)
                .collect(Collectors.toList());
    }
}
