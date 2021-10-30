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
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

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
@RestController("entry-day-controller")
@RequestMapping("/diurnal/entry-day")
public class EntryDayController {

    @Autowired
    @Qualifier("DiurnalTableEntryDay")
    private DiurnalTableEntryDay diurnalTableEntryDay;
    @Autowired
    private UserMappingController userMappingController;
    @Autowired
    private GitHubEntryDayFeignClientImpl gitHubEntryDayFeignClient;
    @Autowired
    private BeanStore beanStore;

    @ApiOperation(value = "create entry day", hidden = true)
    @PostMapping("/create/entry-day")
    public Integer createEntryDay(@RequestBody EntryDayProto.EntryDay entryDay) {
        Integer sqlResult = diurnalTableEntryDay.pushNewEntity(entryDay.getHashEmail(), entryDay.getDate(), entryDay.getTitle(), entryDay.getEntriesAsString());
        if (log.isDebugEnabled()) log.debug("Result of new entry-day creation: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "create bulk entry-days", hidden = true)
    @PostMapping("/create/entry-days")
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

    @ApiOperation(value = "delete entry-day", hidden = true)
    @PostMapping("/delete/entry-day")
    public Integer deleteEntryDay(@RequestBody EntryDayProto.EntryDay entryDay) {
        log.info("Deleting entry-day: {} x {}", entryDay.getHashEmail(), entryDay.getDate());
        Integer sqlResult = diurnalTableEntryDay.deleteEntity(entryDay.getHashEmail(), entryDay.getDate());
        log.info("Result of entry-day deletion: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "delete bulk entry-days", hidden = true)
    @PostMapping("/delete/entry-days")
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

    @ApiOperation(value = "delete all entry-days of an user", hidden = true)
    @PostMapping("/delete/entry-days/user")
    public Integer bulkDeleteEntryDaysOfUser(@RequestBody UserMappingProto.UserMapping userMapping) {
        log.info("Bulk deleting entry-days of user with hash: {}", userMapping.getHashEmail());
        Integer bulkEntriesDeletionResult = diurnalTableEntryDay.bulkDeleteEntryDaysOfUser(userMapping.getHashEmail());
        log.info("Bulk deletion of '{}' entry-days done for user with hash: {}", bulkEntriesDeletionResult, userMapping.getHashEmail());
        return bulkEntriesDeletionResult;
    }

    @ApiOperation(value = "manually delete all entry-days of an user")
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

    @ApiOperation(value = "retrieve all entry-days", hidden = true)
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

    @ApiOperation(value = "retrieve all entry-days of an email-hash", hidden = true)
    @GetMapping("/retrieve/all/entry-days/email-hash")
    public EntryDayProto.EntryDayList retrieveAllEntryDaysOfEmailHash(@RequestParam UserMappingProto.UserMapping userMapping) {
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

    @ApiOperation(value = "check if entry-day exists", hidden = true)
    @GetMapping("/check/entry-day")
    public Boolean checkIfEntryDayExists(@RequestParam EntryDayProto.EntryDay entryDay) {
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

    @PutMapping("/backup/github/csv")
    public boolean backupUserMappingDataToGitHubInCsv(@RequestParam(name = "delimiter", defaultValue = ",") String delimiter) {
        StopWatch stopWatch = beanStore.procureStopWatch();
        StringBuilder dataLines = new StringBuilder();
        diurnalTableEntryDay.retrieveAllEntities().forEach(entryDay ->
                dataLines.append(StringUtils.joinWith(delimiter,
                                String.valueOf(entryDay.getEntryDayId().getEmailHash()), entryDay.getEntryDayId().getDate(), entryDay.getTitle(), entryDay.getEntriesAsString()))
                        .append(NEW_LINE)
        );
        boolean compute = gitHubEntryDayFeignClient.backupAndUploadToGitHub(dataLines.toString());
        stopWatch.stop();
        log.info("Took {} ms to complete entry_day table backup from entry-day controller. Result: {}", stopWatch.getTime(TimeUnit.MILLISECONDS), compute);
        return compute;
    }
}