package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.dbi.interactor.diurnal.DiurnalTableEntry;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.INT_RESPONSE_WONT_PROCESS;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateEntry;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateEntryOnPk;

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

    @GetMapping("/delete/manual/entry")
    public Integer deleteEntryManually(@RequestParam Long mobile,
                                       @RequestParam Integer date,
                                       @RequestParam Integer serial) {
        LOGGER.info("Obtained manual req for entry deletion: {} x {} x {}", mobile, date, serial);
        return deleteEntry(generateEntryOnPk(mobile, date, serial));
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
        LOGGER.info("Result of retrieving all entries: {} entries", entryList.getEntriesCount());
        return entryList;
    }

    @GetMapping("/retrieve/all/manual/entries")
    public List<String> retrieveAllEntriesManually() {
        LOGGER.info("Obtained manual req for retrieving all entries");
        return retrieveAllEntries().getEntriesList().stream()
                .map(AbstractMessage::toString)
                .collect(Collectors.toList());
    }
}
