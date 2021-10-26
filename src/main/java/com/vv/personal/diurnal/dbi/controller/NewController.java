package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.config.DbiConfig;
import com.vv.personal.diurnal.dbi.model.UserMappingEntity;
import com.vv.personal.diurnal.dbi.repository.UserMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.vv.personal.diurnal.dbi.constants.Constants.DEFAULT_INSTANT_DATETIME;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateHash;

/**
 * @author Vivek
 * @since 24/10/21
 */
@Slf4j
@RestController("/sampler")
public class NewController {

    @Autowired
    UserMappingRepository userMappingRepository;
    @Autowired
    private DbiConfig dbiConfig;

    @GetMapping("/manual/create/user")
    public void createUserMappingManually(@RequestParam(defaultValue = "-1", required = false) Long mobile,
                                          @RequestParam String email,
                                          @RequestParam String user,
                                          @RequestParam(defaultValue = "false", required = false) Boolean premiumUser,
                                          @RequestParam String hashCred,
                                          @RequestParam(defaultValue = "INR") UserMappingProto.Currency currency) {
        log.info("Obtained manual req for new user creation: {} x {} x {} x {} x {} x {}", mobile, email, user, premiumUser, hashCred, currency);
        ZonedDateTime currentZoned = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("IST", ZoneId.SHORT_IDS));
        UserMappingEntity userMapping = new UserMappingEntity()
                .setMobile(mobile)
                .setEmail(email)
                .setUser(user)
                .setPremiumUser(premiumUser)
                .setCredHash(hashCred)
                .setEmailHash(generateHash(email))
                .setLastCloudSaveTimestamp(DEFAULT_INSTANT_DATETIME)
                .setLastSaveTimestamp(DEFAULT_INSTANT_DATETIME)
                .setPaymentExpiryTimestamp(currentZoned.plusDays(dbiConfig.getTrialPeriodDays()).toInstant())
                .setAccountCreationTimestamp(currentZoned.toInstant())
                .setCurrency(currency.name());
        UserMappingEntity savedEntity = userMappingRepository.save(userMapping);
        log.info("Generated and saved: {}", savedEntity);
    }

    @GetMapping("/manual/users/")
    public String getAllUsers() {
        log.info("Obtained manual req for reading all users");
        List<UserMappingEntity> userMappingEntities = userMappingRepository.findAll();
        log.info("Extracted: {}", userMappingEntities);
        return userMappingEntities.toString();
    }

    @GetMapping("/manual/delete/user/{email}")
    public String deleteUser(@RequestParam("email") String emailToDelete) {
        log.info("Obtained manual req for deleting email: {}", emailToDelete);
        Integer emailHash = generateHash(emailToDelete);
        userMappingRepository.deleteById(emailHash);
        return getAllUsers();
    }

    Long getTrialEndPeriod(Instant instant) {
        return instant
                .plus(dbiConfig.getTrialPeriodDays(), ChronoUnit.DAYS)
                .toEpochMilli();
    }
}