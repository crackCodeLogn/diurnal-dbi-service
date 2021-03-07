package com.vv.personal.diurnal.dbi.engine.transformer;

import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.artifactory.generated.TitleMappingProto;
import com.vv.personal.diurnal.dbi.engine.transformer.parser.ParseEntry;
import com.vv.personal.diurnal.dbi.engine.transformer.parser.ParseTitle;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import com.vv.personal.diurnal.dbi.util.JsonConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;
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
    private final EntryDayProto.EntryDayList.Builder entryDayListBuilder = EntryDayProto.EntryDayList.newBuilder();
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
            String currentTitle = DEFAULT_TITLE, lastTitle = "-1";
            Integer currentDate = NA_INT, lastDate = NA_INT;
            List<EntryProto.Entry> entries = new ArrayList<>(fullBackupText.size());

            for (String data : fullBackupText) {
                if (data.isEmpty()) continue;

                LINE_TYPE line_type = deriveLineType(data);
                if (line_type == LINE_TYPE.TITLE) {
                    ParseTitle title = new ParseTitle(data);
                    title.parse();
                    date = title.getDate();
                    currentDate = date;
                    if (!TITLES_TO_EXEMPT.contains(title.getTitle())) {
                        currentTitle = processStringForSqlPush(title.getTitle());
                        titleMappingListBuilder.addTitleMapping(generateTitleMapping(mobileNumber, date, currentTitle));
                        if (titleMappingListBuilder.getTitleMappingCount() == 1) {
                            lastTitle = currentTitle;
                            lastDate = date;
                        }
                    } else {
                        currentTitle = DEFAULT_TITLE;
                        LOGGER.info("Skipping insertion in db for no titles: {}", data);
                    }
                    serial = ZERO;
                    if (!entries.isEmpty() || titleMappingListBuilder.getTitleMappingCount() > 1) {
                        EntryDayProto.EntryDay entryDay = computeEntryDay(entries, lastTitle, lastDate);
                        entryDayListBuilder.addEntryDay(entryDay);
                        lastTitle = currentTitle;
                        lastDate = currentDate;
                    }

                } else if (line_type == LINE_TYPE.ENTRY) {
                    ParseEntry entry = new ParseEntry(data);
                    entry.parse();
                    entries.add(generateLightEntry(date, serial, entry.getSign(), entry.getCurrency(), entry.getAmount(), entry.getDescription()));
                    serial++;
                }
            }
            if (!entries.isEmpty()) { //last one
                EntryDayProto.EntryDay entryDay = computeEntryDay(entries, currentTitle, currentDate);
                entryDayListBuilder.addEntryDay(entryDay);
            }
            LOGGER.info("Completed transformation of backup data to DB compatible data. Generated {} titles and {} entry-days",
                    titleMappingListBuilder.getTitleMappingCount(), entryDayListBuilder.getEntryDayCount());
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to completely transform data from backup file. Will not be saving to database. ", e);
        } finally {
            stopWatch.stop();
            LOGGER.info("Took {} ms in backup transformation op", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
        return false;
    }

    private EntryDayProto.EntryDay computeEntryDay(List<EntryProto.Entry> entries, String title, Integer date) {
        EntryDayProto.EntryDay.Builder entryDayBuilder = EntryDayProto.EntryDay.newBuilder();
        entryDayBuilder.setMobile(mobileNumber);
        entryDayBuilder.setDate(date);
        entryDayBuilder.setTitle(title);
        entryDayBuilder.setEntriesAsString(
                StringUtils.join(entries.stream()
                                .map(JsonConverterUtil::convertEntryToCompactedJson)
                                .map(DiurnalUtil::processStringForSqlPush)
                                .collect(Collectors.toList()),
                        "<%~@^>"));
        entries.clear();
        return entryDayBuilder.build();
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

    public EntryDayProto.EntryDayList getEntryDayList() {
        return entryDayListBuilder.build();
    }

    private enum LINE_TYPE {
        TITLE, ENTRY
    }

}
