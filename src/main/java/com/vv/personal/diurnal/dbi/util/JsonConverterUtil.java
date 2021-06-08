package com.vv.personal.diurnal.dbi.util;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_STR;
import static com.vv.personal.diurnal.dbi.constants.Constants.ENTRIES_SQL_DATA_SEPARATOR;

/**
 * @author Vivek
 * @since 06/03/21
 */
public class JsonConverterUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonConverterUtil.class);

    private static final Gson GSON = new Gson();

    public static String convertEntryToJson(EntryProto.Entry entry) {
        try {
            return JsonFormat.printer().print(entry);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Error during to json conv: ", e);
        }
        return EMPTY_STR;
    }

    public static String convertEntryToCompactedJson(EntryProto.Entry entry) {
        return convertEntryToJson(entry)
                .replaceAll(",\n", ",")
                .replaceAll("\n", "")
                .replaceAll(",\\s+\"", ",\"")
                .replaceAll("\\{\\s+\"", "\\{\"")
                .replaceAll("\"\\s+}", "\"}")
                .trim();
    }

    public static EntryProto.Entry convertSqlEntryToProtoEntry(String sqlEntry) {
        String json = DiurnalUtil.refineDbStringForOriginal(sqlEntry);
        EntryProto.Entry.Builder builder = EntryProto.Entry.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Failed to convert {} to entry proto. ", json, e);
        }
        return builder.build();
    }

    public static EntryProto.EntryList convertSqlEntriesToEntryProtoList(String sqlEntriesData) {
        EntryProto.EntryList.Builder entryListBuilder = EntryProto.EntryList.newBuilder();
        entryListBuilder.addAllEntry(
                Arrays.stream(sqlEntriesData.split(ENTRIES_SQL_DATA_SEPARATOR)) //not sure why StringUtils.split is messing up on this.
                        .map(JsonConverterUtil::convertSqlEntryToProtoEntry)
                        .collect(Collectors.toList()));
        return entryListBuilder.build();
    }

    public static <T> String convertToJson(T object) {
        return GSON.toJson(object);
    }

}
