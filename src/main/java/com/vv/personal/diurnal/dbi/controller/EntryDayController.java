package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.engine.transformer.TransformFullBackupToProtos;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntryDay;
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
 * @since 06/03/21
 */
@RestController("entry-day-controller")
@RequestMapping("/diurnal/entry-day")
public class EntryDayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntryDayController.class);

    @Autowired
    @Qualifier("DiurnalTableEntryDay")
    private DiurnalTableEntryDay diurnalTableEntryDay;

    @Autowired
    private UserMappingController userMappingController;

    @ApiOperation(value = "create entry day", hidden = true)
    @PostMapping("/create/entry-day")
    public Integer createEntryDay(@RequestBody EntryDayProto.EntryDay entryDay) {
        Integer sqlResult = diurnalTableEntryDay.pushNewEntity(entryDay);
        LOGGER.info("Result of new entry-day creation: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "create bulk entry-days", hidden = true)
    @PostMapping("/create/entry-days")
    public List<Integer> bulkCreateEntryDays(@RequestBody EntryDayProto.EntryDayList entryDayList) {
        LOGGER.info("Bulk creating {} entry-days", entryDayList.getEntryDayCount());
        List<Integer> bulkEntriesCreationResult = performBulkOpInt(entryDayList.getEntryDayList(), this::createEntryDay);
        LOGGER.info("Result of bulk entry-days creation: {}", bulkEntriesCreationResult);
        return bulkEntriesCreationResult;
    }

    @PutMapping("/manual/create/entry-day")
    public Integer createEntryDayManually(@RequestParam String email,
                                          @RequestParam Integer date,
                                          @RequestParam(defaultValue = EMPTY_STR, required = false) String title,
                                          @RequestParam String entriesAsString) {
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for entry-day insertion for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Obtained manual req for new entry-day creation: {} x {}", email, date);
        return createEntryDay(generateCompleteEntryDay(emailHash, date, title, entriesAsString));
    }

    @ApiOperation(value = "delete entry-day", hidden = true)
    @PostMapping("/delete/entry-day")
    public Integer deleteEntryDay(@RequestBody EntryDayProto.EntryDay entryDay) {
        LOGGER.info("Deleting entry-day: {} x {}", entryDay.getHashEmail(), entryDay.getDate());
        Integer sqlResult = diurnalTableEntryDay.deleteEntity(entryDay);
        LOGGER.info("Result of entry-day deletion: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "delete bulk entry-days", hidden = true)
    @PostMapping("/delete/entry-days")
    public List<Integer> bulkDeleteEntryDays(@RequestBody EntryDayProto.EntryDayList entryDayList) {
        LOGGER.info("Bulk deleting {} entry-days", entryDayList.getEntryDayCount());
        List<Integer> bulkEntriesDeletionResult = performBulkOpInt(entryDayList.getEntryDayList(), this::deleteEntryDay);
        LOGGER.info("Result of bulk entry-days deletion: {}", bulkEntriesDeletionResult);
        return bulkEntriesDeletionResult;
    }

    @DeleteMapping("/manual/delete/entry-day")
    public Integer deleteEntryDayManually(@RequestParam String email,
                                          @RequestParam Integer date) {
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for entry-day deletion for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Obtained manual req for entry-day deletion: {} x {}", email, date);
        return deleteEntryDay(generateEntryDayOnPk(emailHash, date));
    }

    @ApiOperation(value = "delete all entry-days of an user", hidden = true)
    @PostMapping("/delete/entry-days/user")
    public Integer bulkDeleteEntryDaysOfUser(@RequestBody UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Bulk deleting entry-days of user with hash: {}", userMapping.getHashEmail());
        Integer bulkEntriesDeletionResult = diurnalTableEntryDay.bulkDeleteEntryDaysOfUser(userMapping);
        LOGGER.info("Result of bulk entry-days deletion: {} for user with hash: {}", bulkEntriesDeletionResult, userMapping.getHashEmail());
        return bulkEntriesDeletionResult;
    }

    @ApiOperation(value = "manually delete all entry-days of an user")
    @PostMapping("/manual/delete/entry-days/user")
    public Integer bulkDeleteEntryDaysOfUserManually(@RequestParam String email) {
        email = refineEmail(email);
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User doesn't exist for email: {}", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        return bulkDeleteEntryDaysOfUser(generateUserMappingOnPk(emailHash));
    }

    public List<Integer> deleteAndCreateEntryDays(TransformFullBackupToProtos transformFullBackupToProtos) {
        if (transformFullBackupToProtos.getEntryDayList().getEntryDayCount() == 0) return EMPTY_LIST_INT;
        LOGGER.info("Received request to perform delete-create op on {} entry-days", transformFullBackupToProtos.getEntryDayList().getEntryDayCount());
        bulkDeleteEntryDaysOfUser(DiurnalUtil.generateUserMappingOnPk(transformFullBackupToProtos.getEmailHash()));

        List<Integer> bulkOpResult = bulkCreateEntryDays(transformFullBackupToProtos.getEntryDayList());
        if (bulkOpResult.stream().anyMatch(integer -> integer == 0)) {
            LOGGER.warn("Bulk create had some issues while creating certain entry-days. Check log for further details");
        }
        LOGGER.info("Bulk creation op of entry-days completed.");
        return bulkOpResult;
    }

    @ApiOperation(value = "update entry-day", hidden = true)
    @PostMapping("/update/entry-day")
    public Integer updateEntryDay(@RequestBody EntryDayProto.EntryDay entryDay) {
        LOGGER.warn("Updating entry-day is not supported atm. Delete and re-insert if required.");
        return INT_RESPONSE_WONT_PROCESS;
    }

    @PatchMapping("/manual/update/entry-day")
    public Integer updateEntryDayManually(@RequestParam String email,
                                          @RequestParam Integer date) {
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for entry-day updation for email [{}]", email);
            return INT_RESPONSE_WONT_PROCESS;
        }
        LOGGER.info("Obtained manual req for entry-day updation: {} x {}", email, date);
        return updateEntryDay(generateEntryDayOnPk(emailHash, date));
    }

    @ApiOperation(value = "retrieve all entry-days", hidden = true)
    @GetMapping("/retrieve/all/entry-days")
    public EntryDayProto.EntryDayList retrieveAllEntryDays() {
        LOGGER.info("Retrieving all entry-days");
        EntryDayProto.EntryDayList entryDayList = diurnalTableEntryDay.retrieveAll();
        LOGGER.info("Result of retrieving all: {} entry-days", entryDayList.getEntryDayCount());
        return entryDayList;
    }

    @GetMapping("/manual/retrieve/all/entry-days")
    public List<String> retrieveAllEntryDaysManually() {
        LOGGER.info("Obtained manual req for retrieving all entry-days");
        return performBulkOpStr(retrieveAllEntryDays().getEntryDayList(), AbstractMessage::toString);
    }

    @ApiOperation(value = "check if entry-day exists", hidden = true)
    @GetMapping("/check/entry-day")
    public Boolean checkIfEntryDayExists(@RequestParam EntryDayProto.EntryDay entryDay) {
        LOGGER.info("Checking if entry-day exists for: {} x {}", entryDay.getHashEmail(), entryDay.getDate());
        boolean checkIfEntryDayExists = diurnalTableEntryDay.checkEntity(entryDay);
        LOGGER.info("Result: {}", checkIfEntryDayExists);
        return checkIfEntryDayExists;
    }

    @GetMapping("/manual/check/entry-day")
    public Boolean checkIfEntryDayExistsManually(@RequestParam String email,
                                                 @RequestParam Integer date) {
        Integer emailHash = userMappingController.retrieveHashEmail(email);
        if (isEmailHashAbsent(emailHash)) {
            LOGGER.warn("User not found for entry-day updation for email [{}]", email);
            return false;
        }
        LOGGER.info("Checking if entry-day exists for: {} x {}", email, date);
        return checkIfEntryDayExists(generateEntryDayOnPk(emailHash, date));
    }

    @GetMapping("/manual/dump/table/csv/")
    public String dumpTableAsCsv() {
        LOGGER.info("Dumping content of table '{}' onto csv now", diurnalTableEntryDay.getTableName());
        String csvFileLocation = diurnalTableEntryDay.dumpTableToCsv();
        LOGGER.info("Csv file location of the dump => [{}]", csvFileLocation);
        return csvFileLocation;
    }

    @PutMapping("/table/create")
    public int createTableIfNotExists() {
        return genericCreateTableIfNotExists(diurnalTableEntryDay);
    }

    @DeleteMapping("/table/drop")
    public Boolean dropTable(@RequestParam(defaultValue = "false") Boolean absolutelyDropTable) {
        return absolutelyDropTable ? genericDropTable(diurnalTableEntryDay) : false;
    }

    @DeleteMapping("/table/truncate")
    public Boolean truncateTable(@RequestParam(defaultValue = "false") Boolean absolutelyTruncateTable) {
        return absolutelyTruncateTable ? genericTruncateTable(diurnalTableEntryDay) : false;
    }
}
