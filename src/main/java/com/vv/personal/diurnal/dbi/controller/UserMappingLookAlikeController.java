package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.dbi.model.UserMappingLookAlikeEntity;
import com.vv.personal.diurnal.dbi.repository.UserMappingLookAlikeRepository;
import com.vv.personal.diurnal.dbi.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Vivek
 * @since 28/10/21
 */
@Slf4j
@RestController("user-mapping-2-controller")
@RequestMapping("/diurnal/mapping-user-2")
public class UserMappingLookAlikeController {

    @Autowired
    private UserMappingLookAlikeRepository userMappingLookAlikeRepository;

    @PutMapping("/upload/csv")
    int uploadCsv(@RequestParam("csv-location") String csvLocation) {
        AtomicInteger counter = new AtomicInteger(0);
        List<UserMappingLookAlikeEntity> userMappingLookAlikeEntities = FileUtil.readFileFromLocation(csvLocation).stream()
                .map(data -> {
                    if (counter.get() == 0) data = data.substring(1);
                    counter.incrementAndGet();
                    //log.info(data);
                    String[] vals = data.split(",");

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
                    return new UserMappingLookAlikeEntity()
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
        log.info("Extracted {} entities from '{}'", userMappingLookAlikeEntities.size(), csvLocation);
        int saved = userMappingLookAlikeRepository.saveAllAndFlush(userMappingLookAlikeEntities).size();
        log.info("Saved {} into db", saved);
        return saved;
    }
}