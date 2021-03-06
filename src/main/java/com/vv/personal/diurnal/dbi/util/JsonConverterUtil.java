package com.vv.personal.diurnal.dbi.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Vivek
 * @since 06/03/21
 */
public class JsonConverterUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonConverterUtil.class);

    private static final Gson GSON = new Gson();
    private static final GsonBuilder ENTRY_GSON_BUILDER = new GsonBuilder();

    private static final Gson ENTRY_GSON = ENTRY_GSON_BUILDER.registerTypeAdapter(EntryProto.Entry.class, new TypeAdapter<EntryProto.Entry>() {
        @Override
        public void write(JsonWriter jsonWriter, EntryProto.Entry entry) throws IOException {
            jsonWriter.jsonValue(JsonFormat.printer().print(entry));
        }

        @Override
        public EntryProto.Entry read(JsonReader jsonReader) {
            return null; //empty on purpose;
        }
    }).create();

    public static String convertEntryToJson(EntryProto.Entry entry) {
        return ENTRY_GSON.toJson(entry);
    }

    public static String convertEntryToCompactedJson(EntryProto.Entry entry) {
        return convertEntryToJson(entry)
                .replaceAll(",\n", ",")
                .replaceAll(",\\s+\"", ",\"")
                .replaceAll("\\{\\s+\"", "\\{\"")
                .replaceAll("\"\\s+}", "\"}")
                .trim();
    }

    public static EntryProto.Entry convertToEntryProto(String json) {
        EntryProto.Entry.Builder builder = EntryProto.Entry.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Failed to convert {} to entry proto. ", json, e);
        }
        return builder.build();
    }

    public static <T> String convertToJson(T object) {
        return GSON.toJson(object);
    }

}
