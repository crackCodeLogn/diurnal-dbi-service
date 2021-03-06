package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntry;
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
 * @since 24/02/21
 */
@RestController("entry-controller")
@RequestMapping("/diurnal/entry")
public class EntryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntryController.class);

    @Autowired
    @Qualifier("DiurnalTableEntry")
    private DiurnalTableEntry diurnalTableEntry;

    @ApiOperation(value = "create entry", hidden = true)
    @PostMapping("/create/entry")
    public Integer createEntry(@RequestBody EntryProto.Entry entry) {
        LOGGER.info("Creating new entry: {} x {} x {}", entry.getMobile(), entry.getDate(), entry.getSerial());
        Integer sqlResult = diurnalTableEntry.pushNewEntity(entry);
        LOGGER.info("Result of new entry creation: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "create bulk entries", hidden = true)
    @PostMapping("/create/entries")
    public List<Integer> bulkCreateEntries(@RequestBody EntryProto.EntryList entryList) {
        LOGGER.info("Bulk creating {} entries", entryList.getEntryCount());
        List<Integer> bulkEntriesCreationResult = performBulkOpInt(entryList.getEntryList(), this::createEntry);
        LOGGER.info("Result of bulk entry creation: {}", bulkEntriesCreationResult);
        return bulkEntriesCreationResult;
    }

    @GetMapping("/create/manual/entry")
    public Integer createEntryManually(@RequestParam Long mobile,
                                       @RequestParam Integer date,
                                       @RequestParam Integer serial,
                                       @RequestParam(defaultValue = "NEGATIVE") EntryProto.Sign sign,
                                       @RequestParam(defaultValue = "INR") EntryProto.Currency currency,
                                       @RequestParam Double amount,
                                       @RequestParam String description) {
        LOGGER.info("Obtained manual req for new entry creation: {} x {} x {}", mobile, date, serial);
        return createEntry(generateEntry(mobile, date, serial, sign, currency, amount, description));
    }

    @ApiOperation(value = "delete entry", hidden = true)
    @PostMapping("/delete/entry")
    public Integer deleteEntry(@RequestBody EntryProto.Entry entry) {
        LOGGER.info("Deleting entry: {} x {} x {}", entry.getMobile(), entry.getDate(), entry.getSerial());
        Integer sqlResult = diurnalTableEntry.deleteEntity(entry);
        LOGGER.info("Result of entry deletion: {}", sqlResult);
        return sqlResult;
    }

    @ApiOperation(value = "delete bulk entries", hidden = true)
    @PostMapping("/delete/entries")
    public List<Integer> bulkDeleteEntries(@RequestBody EntryProto.EntryList entryList) {
        LOGGER.info("Bulk deleting {} entries", entryList.getEntryCount());
        List<Integer> bulkEntriesDeletionResult = performBulkOpInt(entryList.getEntryList(), this::deleteEntry);
        LOGGER.info("Result of bulk entry deletion: {}", bulkEntriesDeletionResult);
        return bulkEntriesDeletionResult;
    }

    @GetMapping("/delete/manual/entry")
    public Integer deleteEntryManually(@RequestParam Long mobile,
                                       @RequestParam Integer date,
                                       @RequestParam Integer serial) {
        LOGGER.info("Obtained manual req for entry deletion: {} x {} x {}", mobile, date, serial);
        return deleteEntry(generateEntryOnPk(mobile, date, serial));
    }

    @ApiOperation(value = "delete then create entries", hidden = true)
    @PostMapping("/delete-create/entries")
    public List<Integer> deleteAndCreateEntries(@RequestBody EntryProto.EntryList entryList) {
        if (entryList.getEntryList().isEmpty()) return EMPTY_LIST_INT;
        LOGGER.info("Received request to perform delete-create op on {} entries", entryList.getEntryCount());
        bulkDeleteEntries(entryList);

        List<Integer> bulkOpResult = bulkCreateEntries(entryList);
        if (bulkOpResult.stream().anyMatch(integer -> integer == 0)) {
            LOGGER.warn("Bulk create had some issues while creating certain entries. Check log for further details");
        }
        LOGGER.info("Bulk creation op of entries completed.");
        return bulkOpResult;
    }

    @ApiOperation(value = "update entry", hidden = true)
    @PostMapping("/update/entry")
    public Integer updateEntry(@RequestBody EntryProto.Entry entry) {
        LOGGER.warn("Updating entry is not supported atm. Delete and re-insert if required.");
        return INT_RESPONSE_WONT_PROCESS;
    }

    @GetMapping("/update/manual/entry")
    public Integer updateEntryManually(@RequestParam Long mobile,
                                       @RequestParam Integer date,
                                       @RequestParam Integer serial) {
        LOGGER.info("Obtained manual req for entry updation: {} x {} x {}", mobile, date, serial);
        return updateEntry(generateEntryOnPk(mobile, date, serial));
    }

    @ApiOperation(value = "retrieve all entries", hidden = true)
    @GetMapping("/retrieve/all/entries")
    public EntryProto.EntryList retrieveAllEntries() {
        LOGGER.info("Retrieving all entries");
        EntryProto.EntryList entryList = diurnalTableEntry.retrieveAll();
        LOGGER.info("Result of retrieving all entries: {} entries", entryList.getEntryCount());
        return entryList;
    }

    @GetMapping("/retrieve/all/manual/entries")
    public List<String> retrieveAllEntriesManually() {
        LOGGER.info("Obtained manual req for retrieving all entries");
        return performBulkOpStr(retrieveAllEntries().getEntryList(), AbstractMessage::toString);
    }

    @ApiOperation(value = "check if entry exists", hidden = true)
    @GetMapping("/check/entry")
    public Boolean checkIfEntryExists(@RequestParam EntryProto.Entry entry) {
        LOGGER.info("Checking if entry exists for: {} x {} x {}", entry.getMobile(), entry.getDate(), entry.getSerial());
        boolean checkIfEntryExists = diurnalTableEntry.checkEntity(entry);
        LOGGER.info("Result: {}", checkIfEntryExists);
        return checkIfEntryExists;
    }

    @GetMapping("/check/manual/entry")
    public Boolean checkIfEntryExistsManually(@RequestParam Long mobile,
                                              @RequestParam Integer date,
                                              @RequestParam Integer serial) {
        LOGGER.info("Checking if entry exists for: {} x {} x {}", mobile, date, serial);
        return checkIfEntryExists(generateEntryOnPk(mobile, date, serial));
    }

    @DeleteMapping("/drop/table")
    public Boolean dropTable(@RequestParam(defaultValue = "false") Boolean absolutelyDropTable) {
        return absolutelyDropTable ? genericDropTable(diurnalTableEntry) : false;
    }

    @DeleteMapping("/truncate/table")
    public Boolean truncateTable(@RequestParam(defaultValue = "false") Boolean absolutelyTruncateTable) {
        return absolutelyTruncateTable ? genericTruncateTable(diurnalTableEntry) : false;
    }

}
