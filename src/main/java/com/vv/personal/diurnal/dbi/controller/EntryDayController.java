package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.client.impl.GitHubEntryDayFeignClientImpl;
import com.vv.personal.diurnal.dbi.config.BeanStore;
import com.vv.personal.diurnal.dbi.engine.transformer.TransformFullBackupToProtos;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntryDay;
import com.vv.personal.diurnal.dbi.model.EntryDayEntity;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import com.vv.personal.diurnal.dbi.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.*;

/**
 * @author Vivek
 * @since 06/03/21
 */
@Slf4j
@Secured("user")
@RestController("entry-day-controller")
@RequestMapping("/diurnal/entry-day")
public class EntryDayController {
    @Inject
    DiurnalTableEntryDay diurnalTableEntryDay;
    @Inject
    UserMappingController userMappingController;
    @Inject
    GitHubEntryDayFeignClientImpl gitHubEntryDayFeignClient;
    @Inject
    BeanStore beanStore;

    @PostMapping(value = "/create/entry-day", produces = APPLICATION_X_PROTOBUF, consumes = APPLICATION_X_PROTOBUF)
    public Integer createEntryDay(@RequestBody EntryDayProto.EntryDay entryDay) {
        Integer sqlResult = diurnalTableEntryDay.pushNewEntity(entryDay.getHashEmail(), entryDay.getDate(), entryDay.getTitle(), entryDay.getEntriesAsString());
        if (log.isDebugEnabled()) log.debug("Result of new entry-day creation: {}", sqlResult);
        return sqlResult;
    }

    @PostMapping(value = "/create/entry-days", consumes = APPLICATION_X_PROTOBUF)
    public int bulkCreateEntryDays(@RequestBody EntryDayProto.EntryDayList entryDayList) {
        log.info("Bulk creating {} entry-days", entryDayList.getEntryDayCount());
        List<EntryDayEntity> bulkEntriesMapped = generateBulkEntryDaysFromProto(entryDayList);
        int bulkEntriesCreationResult = diurnalTableEntryDay.pushNewEntities(bulkEntriesMapped);
        log.info("Result of '{}' bulk entry-days creation: {}", entryDayList.getEntryDayCount(), bulkEntriesCreationResult);
        return bulkEntriesCreationResult;
    }

    private List<EntryDayEntity> generateBulkEntryDaysFromProto(EntryDayProto.EntryDayList entryDayList) {
        return entryDayList.getEntryDayList().stream().map(entryDay ->
                DiurnalTableEntryDay.generateEntryDayEntity(entryDay.getHashEmail(), entryDay.getDate(), entryDay.getTitle(), entryDay.getEntriesAsString())
        ).collect(Collectors.toList());
    }

    @PutMapping("/manual/create/entry-day")
    public Integer createEntryDayManually(@RequestParam String email,
                                          @RequestParam Integer date,
                                          @RequestParam(defaultValue = EMPTY_STR, required = false) String title,
                                          @RequestParam String entriesAsString) {
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for entry-day insertion for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Obtained manual req for new entry-day creation: {} x {}", email, date);
        return createEntryDay(generateCompleteEntryDay(emailHash, date, title, entriesAsString));
    }

    @PostMapping(value = "/delete/entry-day", consumes = APPLICATION_X_PROTOBUF)
    public Integer deleteEntryDay(@RequestBody EntryDayProto.EntryDay entryDay) {
        log.info("Deleting entry-day: {} x {}", entryDay.getHashEmail(), entryDay.getDate());
        Integer sqlResult = diurnalTableEntryDay.deleteEntity(entryDay.getHashEmail(), entryDay.getDate());
        log.info("Result of entry-day deletion: {}", sqlResult);
        return sqlResult;
    }

    @PostMapping(value = "/delete/entry-days", consumes = APPLICATION_X_PROTOBUF)
    public List<Integer> bulkDeleteEntryDays(@RequestBody EntryDayProto.EntryDayList entryDayList) {
        log.info("Bulk deleting {} entry-days", entryDayList.getEntryDayCount());
        List<Integer> bulkEntriesDeletionResult = performBulkOpInt(entryDayList.getEntryDayList(), this::deleteEntryDay);
        log.info("Result of bulk entry-days deletion: {}", bulkEntriesDeletionResult);
        return bulkEntriesDeletionResult;
    }

    @DeleteMapping("/manual/delete/entry-day")
    public Integer deleteEntryDayManually(@RequestParam String email,
                                          @RequestParam Integer date) {
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for entry-day deletion for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        log.info("Obtained manual req for entry-day deletion: {} x {}", email, date);
        return deleteEntryDay(generateEntryDayOnPk(emailHash, date));
    }

    @PostMapping(value = "/delete/entry-days/user", consumes = APPLICATION_X_PROTOBUF)
    public Integer bulkDeleteEntryDaysOfUser(@RequestBody UserMappingProto.UserMapping userMapping) {
        log.info("Bulk deleting entry-days of user with hash: {}", userMapping.getHashEmail());
        Integer bulkEntriesDeletionResult = diurnalTableEntryDay.bulkDeleteEntryDaysOfUser(userMapping.getHashEmail());
        log.info("Bulk deletion of '{}' entry-days done for user with hash: {}", bulkEntriesDeletionResult, userMapping.getHashEmail());
        return bulkEntriesDeletionResult;
    }

    @DeleteMapping("/manual/delete/entry-days/user")
    public Integer bulkDeleteEntryDaysOfUserManually(@RequestParam String email) {
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User doesn't exist for email: {}", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        return bulkDeleteEntryDaysOfUser(generateUserMappingOnPk(emailHash));
    }

    public boolean deleteAndCreateEntryDays(TransformFullBackupToProtos transformFullBackupToProtos) {
        if (transformFullBackupToProtos.getEntryDayList().getEntryDayCount() == 0) return true; //TODO - handle this UC
        log.info("Received request to perform delete-create op on {} entry-days", transformFullBackupToProtos.getEntryDayList().getEntryDayCount());
        bulkDeleteEntryDaysOfUser(DiurnalUtil.generateUserMappingOnPk(transformFullBackupToProtos.getEmailHash()));

        int expectedNewRows = transformFullBackupToProtos.getEntryDayList().getEntryDayCount();
        int bulkOpResult = bulkCreateEntryDays(transformFullBackupToProtos.getEntryDayList());
        if (bulkOpResult != expectedNewRows) {
            log.warn("Bulk create had some issues while creating certain entry-days. Check log for further details");
            return false;
        }
        log.info("Bulk creation op of entry-days completed.");
        return true;
    }

    @GetMapping("/retrieve/all/entry-days")
    public EntryDayProto.EntryDayList retrieveAllEntryDays() {
        log.info("Retrieving all entry-days");
        EntryDayProto.EntryDayList entryDayList = diurnalTableEntryDay.retrieveAll();
        log.info("Result of retrieving all: {} entry-days", entryDayList.getEntryDayCount());
        return entryDayList;
    }

    @GetMapping("/manual/retrieve/all/entry-days")
    public List<String> retrieveAllEntryDaysManually() {
        log.info("Obtained manual req for retrieving all entry-days");
        return performBulkOpStr(retrieveAllEntryDays().getEntryDayList(), AbstractMessage::toString);
    }

    @GetMapping(value = "/retrieve/all/entry-days/email-hash", produces = APPLICATION_X_PROTOBUF, consumes = APPLICATION_X_PROTOBUF)
    public EntryDayProto.EntryDayList retrieveAllEntryDaysOfEmailHash(@RequestBody UserMappingProto.UserMapping userMapping) {
        log.info("Retrieving all entry-days of email-hash => {}", userMapping.getHashEmail());
        EntryDayProto.EntryDayList entryDayList = diurnalTableEntryDay.retrieveSome(userMapping.getHashEmail());
        log.info("Result of retrieving all: {} entry-days of email hash {}", entryDayList.getEntryDayCount(), userMapping.getHashEmail());
        return entryDayList;
    }

    @GetMapping("/manual/retrieve/all/entry-days/email-hash")
    public List<String> retrieveAllEntryDaysOfEmailHashManually(@RequestParam String email) {
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for entry-days retrieval for email [{}]", email);
            return new ArrayList<>();
        }
        return performBulkOpStr(retrieveAllEntryDaysOfEmailHash(generateUserMappingOnPk(emailHash)).getEntryDayList(), AbstractMessage::toString);
    }

    @GetMapping(value = "/check/entry-day", consumes = APPLICATION_X_PROTOBUF)
    public Boolean checkIfEntryDayExists(@RequestBody EntryDayProto.EntryDay entryDay) {
        log.info("Checking if entry-day exists for: {} x {}", entryDay.getHashEmail(), entryDay.getDate());
        boolean checkIfEntryDayExists = diurnalTableEntryDay.checkEntity(entryDay.getHashEmail(), entryDay.getDate());
        log.info("Result: {}", checkIfEntryDayExists);
        return checkIfEntryDayExists;
    }

    @GetMapping("/manual/check/entry-day")
    public Boolean checkIfEntryDayExistsManually(@RequestParam String email,
                                                 @RequestParam Integer date) {
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            log.warn("User not found for entry-day updation for email [{}]", email);
            return false;
        }
        log.info("Checking if entry-day exists for: {} x {}", email, date);
        return checkIfEntryDayExists(generateEntryDayOnPk(emailHash, date));
    }

    @PutMapping("/upload/csv")
    public int uploadCsv(@RequestParam("csv-location") String csvLocation,
                         @RequestParam(value = "delimiter", defaultValue = "\\|") String delimiter) {
        List<EntryDayEntity> entryDayEntities = FileUtil.readFileFromLocation(csvLocation).stream()
                .map(data -> {
                    String[] vals = data.split(delimiter);

                    String emailHashAndDate = vals[0].trim();
                    int emailHash = Integer.parseInt(vals[1]);
                    int date = Integer.parseInt(vals[2]);
                    String title = vals[3];
                    String entries = vals[4];
                    return new EntryDayEntity()
                            .setEmailHashAndDate(emailHashAndDate)
                            .setEmailHash(emailHash)
                            .setDate(date)
                            .setTitle(title)
                            .setEntriesAsString(entries)
                            ;
                }).collect(Collectors.toList());
        log.info("Extracted {} entities from '{}'", entryDayEntities.size(), csvLocation);
        int saved = diurnalTableEntryDay.pushNewEntities(entryDayEntities);
        log.info("Saved {} into db", saved);
        return saved;
    }

    @PutMapping("/backup/github/csv")
    public boolean backupEntryDayDataToGitHubInCsv(@RequestParam(name = "delimiter", defaultValue = "\\|") String delimiter) {
        StopWatch stopWatch = beanStore.procureStopWatch();
        String dataLines = diurnalTableEntryDay.processAllRowsToCsv(delimiter);
        boolean compute = gitHubEntryDayFeignClient.backupAndUploadToGitHub(dataLines);
        stopWatch.stop();
        log.info("Took {} ms to complete entry_day table backup from entry-day controller. Result: {}", stopWatch.getTime(TimeUnit.MILLISECONDS), compute);
        return compute;
    }
}