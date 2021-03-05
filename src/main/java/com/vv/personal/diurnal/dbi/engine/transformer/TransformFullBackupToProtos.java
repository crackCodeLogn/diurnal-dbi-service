package com.vv.personal.diurnal.dbi.engine.transformer;

import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.artifactory.generated.TitleMappingProto;
import com.vv.personal.diurnal.dbi.engine.transformer.parser.ParseEntry;
import com.vv.personal.diurnal.dbi.engine.transformer.parser.ParseTitle;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vv.personal.diurnal.dbi.constants.Constants.NA_INT;
import static com.vv.personal.diurnal.dbi.constants.Constants.ZERO;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.*;

/**
 * @author Vivek
 * @since 03/03/21
 */
public class TransformFullBackupToProtos {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformFullBackupToProtos.class);

    private final List<String> fullBackupText;
    private final Long mobileNumber;
    private final EntryProto.EntryList.Builder entryListBuilder = EntryProto.EntryList.newBuilder();
    private final TitleMappingProto.TitleMappingList.Builder titleMappingListBuilder = TitleMappingProto.TitleMappingList.newBuilder();

    public TransformFullBackupToProtos(List<String> fullBackupText, Long mobileNumber) {
        this.fullBackupText = fullBackupText;
        this.mobileNumber = mobileNumber;
    }

    public boolean transformWithoutSuppliedDate() {
        int date = NA_INT, serial = ZERO;
        StopWatch stopWatch = procureStopWatch();
        stopWatch.start();
        try {
            for (String data : fullBackupText) {
                if (data.isEmpty()) continue;

                LINE_TYPE line_type = deriveLineType(data);
                if (line_type == LINE_TYPE.TITLE) {
                    ParseTitle title = new ParseTitle(data);
                    title.parse();
                    date = title.getDate();
                    if (!title.getTitle().equals("-TITLE-")) {
                        titleMappingListBuilder.addTitleMapping(generateTitleMapping(mobileNumber, date, title.getTitle()));
                    } else {
                        LOGGER.info("Skipping insertion in db for no titles: {}", data);
                    }
                    serial = ZERO;
                } else if (line_type == LINE_TYPE.ENTRY) {
                    ParseEntry entry = new ParseEntry(data);
                    entry.parse();
                    entryListBuilder.addEntry(generateEntry(mobileNumber, date, serial, entry.getSign(), entry.getCurrency(), entry.getAmount(), entry.getDescription()));
                    serial++;
                }
            }
            LOGGER.info("Completed transformation of backup data to DB compatible data. Generated {} titles and {} entries",
                    titleMappingListBuilder.getTitleMappingCount(), entryListBuilder.getEntryCount());
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to completely transform data from backup file. Will not be saving to database. ", e);
        } finally {
            stopWatch.stop();
            LOGGER.info("Took {} ms in backup transformation op", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
        return false;
    }

    private LINE_TYPE deriveLineType(String line) {
        if (line.contains("::")) return LINE_TYPE.TITLE;
        if (line.contains(":") || line.startsWith("//")) return LINE_TYPE.ENTRY;
        return null;
    }

    public EntryProto.EntryList getEntryList() {
        return entryListBuilder.build();
    }

    public TitleMappingProto.TitleMappingList getTitleMapping() {
        return titleMappingListBuilder.build();
    }

    private enum LINE_TYPE {
        TITLE, ENTRY
    }

}
