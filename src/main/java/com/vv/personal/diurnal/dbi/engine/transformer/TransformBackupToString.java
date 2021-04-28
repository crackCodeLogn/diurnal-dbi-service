package com.vv.personal.diurnal.dbi.engine.transformer;

import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
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

    public ResponsePrimitiveProto.ResponsePrimitive transform() {
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

    public ResponsePrimitiveProto.ResponsePrimitive secondStageTransform(Queue<EntryDayRepr> entryDayReprQueue) {
        ResponsePrimitiveProto.ResponsePrimitive.Builder response = ResponsePrimitiveProto.ResponsePrimitive.newBuilder();

        while (!entryDayReprQueue.isEmpty()) {
            EntryDayRepr entryDayRepr = entryDayReprQueue.poll();
            Queue<EntryProto.Entry> entries = entryDayRepr.getEntries();
            if (entries.isEmpty()) continue;

            response.addResponses(String.format("%s %s ::\n",
                    DiurnalUtil.convertEntryDayDateToDisplayFormat(entryDayRepr.getEntryDay().getDate()),
                    entryDayRepr.getEntryDay().getTitle()));

            while (!entries.isEmpty()) {
                EntryProto.Entry entry = entries.poll();
                String acquiredSign = EMPTY_STR;
                boolean nonComment = true;
                switch (entry.getSign()) {
                    case COMMENT:
                        response.addResponses(String.format("//%s\n", entry.getDescription().trim()));
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
                    response.addResponses(String.format("%s %s %.2f : %s\n", acquiredSign, entry.getCurrency(), entry.getAmount(), entry.getDescription().trim()));
                    //forcefully neglecting conversion from entry.getCurrency's STR format to the sign symbol for now, as on app side, currency would be overridden from settings itself
                }
            }
            response.addResponses(NEW_LINE);
        }
        return response.build();
    }

    private EntryDayRepr generateEntryDayRepr(EntryDayProto.EntryDay entryDay, EntryProto.EntryList entries) {
        EntryDayRepr entryDayRepr = new EntryDayRepr(entryDay);
        entryDayRepr.addEntries(entries);
        return entryDayRepr;
    }

}
