package com.vv.personal.diurnal.dbi.engine.transformer;

import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.dbi.engine.transformer.parser.ParseEntry;
import com.vv.personal.diurnal.dbi.engine.transformer.parser.ParseTitle;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import com.vv.personal.diurnal.dbi.util.JsonConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.ENTRIES_SQL_DATA_SEPARATOR;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateLiteEntry;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.procureStopWatch;

/**
 * @author Vivek
 * @since 03/03/21
 */
@Slf4j
public class TransformFullBackupToProtos {
    private final List<String> fullBackupText;
    private final Integer emailHash;
    private final EntryDayProto.EntryDayList.Builder entryDayListBuilder = EntryDayProto.EntryDayList.newBuilder();

    public TransformFullBackupToProtos(List<String> fullBackupText, Integer emailHash) {
        this.fullBackupText = fullBackupText;
        this.emailHash = emailHash;
    }

    public boolean transformWithoutSuppliedDate() {
        StopWatch stopWatch = procureStopWatch();
        stopWatch.start();
        try {
            String currentTitle;
            Integer currentDate;
            Queue<EntryProto.Entry> entries = new LinkedList<>();
            int i = 0, serial = 0;
            for (i = 0; fullBackupText.get(i).trim().isEmpty(); i++) ; //cycling fwd on empty lines if any
            if (i >= fullBackupText.size()) {
                log.warn("Strange backup file acquired - no good lines present.");
                return false;
            }
            LINE_TYPE line_type = deriveLineType(fullBackupText.get(i));
            if (line_type == LINE_TYPE.ENTRY) {
                log.warn("Not a good backup file. First good line cannot be an entry!");
                return false;
            }
            ParseTitle title = new ParseTitle(fullBackupText.get(i)); //first sentient line to be a title
            title.parse();
            currentTitle = title.getRefinedTitle();
            currentDate = title.getDate();
            i++;

            int exemptTitles = 0;
            if (title.isTitleToExempt()) exemptTitles++;
            for (; i < fullBackupText.size(); i++) {
                String data = fullBackupText.get(i);
                if (data.isEmpty()) continue;

                line_type = deriveLineType(data);
                if (line_type == LINE_TYPE.ENTRY) {
                    ParseEntry entry = new ParseEntry(data);
                    entry.parse();
                    entries.offer(generateLiteEntry(serial, entry.getSign(), entry.getCurrency(), entry.getAmount(), entry.getDescription()));
                    serial++;

                } else if (line_type == LINE_TYPE.TITLE) {
                    serial = 0;
                    entryDayListBuilder.addEntryDay(computeEntryDay(entries, currentTitle, currentDate));
                    entries.clear();

                    title = new ParseTitle(data);
                    title.parse();
                    currentTitle = title.getRefinedTitle();
                    currentDate = title.getDate();
                    if (title.isTitleToExempt()) exemptTitles++;
                }
            }
            if (!entries.isEmpty()) { //last one
                EntryDayProto.EntryDay entryDay = computeEntryDay(entries, currentTitle, currentDate);
                entryDayListBuilder.addEntryDay(entryDay);
            }
            log.info("Completed transformation of backup data to DB compatible data. Generated {} titles and {} entry-days",
                    entryDayListBuilder.getEntryDayCount() - exemptTitles, entryDayListBuilder.getEntryDayCount());
            return true;
        } catch (Exception e) {
            log.error("Failed to completely transform data from backup file. Will not be saving to database. ", e);
        } finally {
            stopWatch.stop();
            log.info("Took {} ms in backup transformation op", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
        return false;
    }

    private EntryDayProto.EntryDay computeEntryDay(Queue<EntryProto.Entry> entries, String title, Integer date) {
        EntryDayProto.EntryDay.Builder entryDayBuilder = EntryDayProto.EntryDay.newBuilder();
        entryDayBuilder.setHashEmail(emailHash);
        entryDayBuilder.setDate(date);
        entryDayBuilder.setTitle(title);
        entryDayBuilder.setEntriesAsString(
                StringUtils.join(entries.stream()
                                .map(JsonConverterUtil::convertEntryToCompactedJson)
                                .map(DiurnalUtil::processStringForSqlPush)
                                .collect(Collectors.toList()),
                        ENTRIES_SQL_DATA_SEPARATOR));
        entries.clear();
        return entryDayBuilder.build();
    }

    private LINE_TYPE deriveLineType(String line) {
        if (line.contains("::")) return LINE_TYPE.TITLE;
        if (line.contains(":") || line.startsWith("//")) return LINE_TYPE.ENTRY;
        return null;
    }

    public EntryDayProto.EntryDayList getEntryDayList() {
        return entryDayListBuilder.build();
    }

    public Integer getEmailHash() {
        return emailHash;
    }

    private enum LINE_TYPE {
        TITLE, ENTRY
    }
}