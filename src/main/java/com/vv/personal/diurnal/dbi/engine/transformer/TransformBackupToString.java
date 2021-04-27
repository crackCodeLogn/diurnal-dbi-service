package com.vv.personal.diurnal.dbi.engine.transformer;

import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.dbi.engine.transformer.packet.EntryDayRepr;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import com.vv.personal.diurnal.dbi.util.JsonConverterUtil;

import java.util.LinkedList;
import java.util.Queue;

import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_STR;
import static com.vv.personal.diurnal.dbi.constants.Constants.NEW_LINE;

/**
 * @author Vivek
 * @since 24/04/21
 */
public class TransformBackupToString {

    private final EntryDayProto.EntryDayList entryDayList;

    public TransformBackupToString(EntryDayProto.EntryDayList entryDayList) {
        this.entryDayList = entryDayList;
    }

    public String transform() {
        Queue<EntryDayRepr> entryDayReprQueue = firstStageTransform();
        return secondStageTransform(entryDayReprQueue);
    }

    public Queue<EntryDayRepr> firstStageTransform() {
        Queue<EntryDayRepr> entryDayReprList = new LinkedList<>();
        entryDayList.getEntryDayList().forEach(srcEntryDay -> {
            if (!srcEntryDay.getEntriesAsString().isEmpty()) {
                EntryDayProto.EntryDay.Builder entryDay = EntryDayProto.EntryDay.newBuilder();
                entryDay.setDate(srcEntryDay.getDate());
                entryDay.setTitle(srcEntryDay.getTitle());

                EntryProto.EntryList entryList = JsonConverterUtil.convertSqlEntriesToEntryProtoList(srcEntryDay.getEntriesAsString());

                entryDayReprList.offer(generateEntryDayRepr(entryDay.build(), entryList));
            }
        });
        return entryDayReprList;
    }

    public String secondStageTransform(Queue<EntryDayRepr> entryDayReprQueue) {
        StringBuilder response = new StringBuilder();
        while (!entryDayReprQueue.isEmpty()) {
            EntryDayRepr entryDayRepr = entryDayReprQueue.poll();
            Queue<EntryProto.Entry> entries = entryDayRepr.getEntries();
            if (entries.isEmpty()) continue;

            response.append(String.format("%s %s ::",
                    DiurnalUtil.convertEntryDayDateToDisplayFormat(entryDayRepr.getEntryDay().getDate()),
                    entryDayRepr.getEntryDay().getTitle()))
                    .append(NEW_LINE);

            while (!entries.isEmpty()) {
                EntryProto.Entry entry = entries.poll();
                String acquiredSign = EMPTY_STR;
                boolean nonComment = true;
                switch (entry.getSign()) {
                    case COMMENT:
                        response.append(String.format("//%s", entry.getDescription().trim()));
                        nonComment = false;
                        break;
                    case POSITIVE:
                        acquiredSign = "+";
                        break;
                    case NEGATIVE:
                        acquiredSign = "-";
                        break;
                }
                if (nonComment) {
                    response.append(String.format("%s %s %.2f : %s", acquiredSign, entry.getCurrency(), entry.getAmount(), entry.getDescription().trim()));
                    //forcefully neglecting conversion from entry.getCurrency's STR format to the sign symbol for now, as on app side, currency would be overridden from settings itself
                }
                response.append(NEW_LINE);
            }
            response.append(NEW_LINE);
        }
        return response.toString();
    }

    private EntryDayRepr generateEntryDayRepr(EntryDayProto.EntryDay entryDay, EntryProto.EntryList entries) {
        EntryDayRepr entryDayRepr = new EntryDayRepr(entryDay);
        entryDayRepr.addEntries(entries);
        return entryDayRepr;
    }

}
