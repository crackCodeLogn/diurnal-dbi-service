package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntryDay;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_LIST_INT;
import static com.vv.personal.diurnal.dbi.constants.Constants.INT_RESPONSE_WONT_PROCESS;
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

    @ApiOperation(value = "create entry day", hidden = true)
    @PostMapping("/create/entry-day")
    public Integer createEntryDay(@RequestBody EntryDayProto.EntryDay entryDay) {
        LOGGER.info("Creating new entry-day: {} x {}", entryDay.getMobile(), entryDay.getDate());
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

    @GetMapping("/create/manual/entry-day")
    public Integer createEntryDayManually(@RequestParam Long mobile,
                                          @RequestParam Integer date,
                                          @RequestParam String entriesAsString) {
        LOGGER.info("Obtained manual req for new entry-day creation: {} x {}", mobile, date);
        return createEntryDay(generateEntryDay(mobile, date, entriesAsString));
    }

    @ApiOperation(value = "delete entry-day", hidden = true)
    @PostMapping("/delete/entry-day")
    public Integer deleteEntryDay(@RequestBody EntryDayProto.EntryDay entryDay) {
        LOGGER.info("Deleting entry-day: {} x {}", entryDay.getMobile(), entryDay.getDate());
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

    @GetMapping("/delete/manual/entry-day")
    public Integer deleteEntryDayManually(@RequestParam Long mobile,
                                          @RequestParam Integer date) {
        LOGGER.info("Obtained manual req for entry-day deletion: {} x {}", mobile, date);
        return deleteEntryDay(generateEntryDayOnPk(mobile, date));
    }

    @ApiOperation(value = "delete then create entry-days", hidden = true)
    @PostMapping("/delete-create/entry-days")
    public List<Integer> deleteAndCreateEntryDays(@RequestBody EntryDayProto.EntryDayList entryDayList) {
        if (entryDayList.getEntryDayList().isEmpty()) return EMPTY_LIST_INT;
        LOGGER.info("Received request to perform delete-create op on {} entry-days", entryDayList.getEntryDayCount());
        bulkDeleteEntryDays(entryDayList);

        List<Integer> bulkOpResult = bulkCreateEntryDays(entryDayList);
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

    @GetMapping("/update/manual/entry-day")
    public Integer updateEntryDayManually(@RequestParam Long mobile,
                                          @RequestParam Integer date) {
        LOGGER.info("Obtained manual req for entry-day updation: {} x {}", mobile, date);
        return updateEntryDay(generateEntryDayOnPk(mobile, date));
    }

    @ApiOperation(value = "retrieve all entry-days", hidden = true)
    @GetMapping("/retrieve/all/entry-days")
    public EntryDayProto.EntryDayList retrieveAllEntryDays() {
        LOGGER.info("Retrieving all entry-days");
        EntryDayProto.EntryDayList entryDayList = diurnalTableEntryDay.retrieveAll();
        LOGGER.info("Result of retrieving all: {} entry-days", entryDayList.getEntryDayCount());
        return entryDayList;
    }

    @GetMapping("/retrieve/all/manual/entry-days")
    public List<String> retrieveAllEntryDaysManually() {
        LOGGER.info("Obtained manual req for retrieving all entry-days");
        return performBulkOpStr(retrieveAllEntryDays().getEntryDayList(), AbstractMessage::toString);
    }

    @ApiOperation(value = "check if entry-day exists", hidden = true)
    @GetMapping("/check/entry-day")
    public Boolean checkIfEntryDayExists(@RequestParam EntryDayProto.EntryDay entryDay) {
        LOGGER.info("Checking if entry-day exists for: {} x {}", entryDay.getMobile(), entryDay.getDate());
        boolean checkIfEntryDayExists = diurnalTableEntryDay.checkEntity(entryDay);
        LOGGER.info("Result: {}", checkIfEntryDayExists);
        return checkIfEntryDayExists;
    }

    @GetMapping("/check/manual/entry-entry")
    public Boolean checkIfEntryDayExistsManually(@RequestParam Long mobile,
                                                 @RequestParam Integer date) {
        LOGGER.info("Checking if entry-day exists for: {} x {}", mobile, date);
        return checkIfEntryDayExists(generateEntryDayOnPk(mobile, date));
    }

}
