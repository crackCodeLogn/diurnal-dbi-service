package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.DataTransitProto;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.vv.personal.diurnal.dbi.constants.Constants.INT_RESPONSE_WONT_PROCESS;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateUserMappingOnPk;

/**
 * @author Vivek
 * @since 27/02/21
 * <p>
 * This controller's end-points are the one to be used by external client - app - to push data to DB
 */
@RestController("data-controller")
@RequestMapping("/diurnal/data")
public class DataController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private EntryController entryController;
    @Autowired
    private TitleMappingController titleMappingController;
    @Autowired
    private UserMappingController userMappingController;

    @ApiOperation(value = "push new entry", hidden = true)
    @PostMapping("/push/entry")
    public Integer pushEntry(@RequestBody EntryProto.Entry entry) {
        LOGGER.info("Rx-ed new entry to push to DB: {} x {} x {}", entry.getMobile(), entry.getDate(), entry.getSerial());
        if (!userMappingController.checkIfUserExists(generateUserMappingOnPk(entry.getMobile()))) {
            LOGGER.warn("User doesn't exist for mobile: {}", entry.getMobile());
            return INT_RESPONSE_WONT_PROCESS;
        }
        Integer result = entryController.createEntry(entry);
        LOGGER.info("Result of creating new entry: {}", result);
        return result;
    }

    @ApiOperation(value = "Read whole backup file and generate data for DB", hidden = true)
    @PostMapping("/push/backup/whole")
    public Boolean pushWholeBackup(@RequestBody DataTransitProto.DataTransit dataTransit) {
        LOGGER.info("Rx-ed data in dataTransit to backup to DB: {} bytes", dataTransit.getBackupData().getBytes());
        if (!userMappingController.checkIfUserExists(generateUserMappingOnPk(dataTransit.getMobile()))) {
            LOGGER.warn("User doesn't exist for mobile: {}", dataTransit.getMobile());
            return false;
        }


        return false;
    }

}
