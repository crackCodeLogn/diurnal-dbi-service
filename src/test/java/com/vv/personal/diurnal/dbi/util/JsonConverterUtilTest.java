package com.vv.personal.diurnal.dbi.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.vv.personal.diurnal.dbi.util.JsonConverterUtil.convertEntryToCompactedJson;
import static com.vv.personal.diurnal.dbi.util.JsonConverterUtil.convertEntryToJson;
import static org.junit.Assert.assertEquals;

/**
 * @author Vivek
 * @since 06/03/21
 */
@RunWith(JUnit4.class)
public class JsonConverterUtilTest {

    @Test
    public void testConvertEntryToJson() {
        EntryProto.Entry entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, UserMappingProto.Currency.INR, 121.12, "Sampler s1");
        String json = convertEntryToJson(entry);
        System.out.println(json);
        assertEquals("{\n" +
                "  \"mobile\": \"123456789\",\n" +
                "  \"date\": 20210306,\n" +
                "  \"currency\": \"INR\",\n" +
                "  \"amount\": 121.12,\n" +
                "  \"description\": \"Sampler s1\"\n" +
                "}", json);
    }

    @Test
    public void testConvertEntryToCompactedJson() {
        EntryProto.Entry entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, UserMappingProto.Currency.INR, 121.12, "Sampler s1");
        String json = convertEntryToCompactedJson(entry);
        System.out.println(json);
        assertEquals("{\"mobile\": \"123456789\",\"date\": 20210306,\"currency\": \"INR\",\"amount\": 121.12,\"description\": \"Sampler s1\"}", json);

        entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, UserMappingProto.Currency.INR, 121.12, "Sampler s1\n second line this is !");
        json = convertEntryToCompactedJson(entry);
        System.out.println(json);
        assertEquals("{\"mobile\": \"123456789\",\"date\": 20210306,\"currency\": \"INR\",\"amount\": 121.12,\"description\": \"Sampler s1\\n second line this is !\"}", json);

        entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, UserMappingProto.Currency.INR, 121.12, "Sampler s1\n" +
                " second line this is !");
        json = convertEntryToCompactedJson(entry);
        System.out.println(json);
        assertEquals("{\"mobile\": \"123456789\",\"date\": 20210306,\"currency\": \"INR\",\"amount\": 121.12,\"description\": \"Sampler s1\\n second line this is !\"}", json);
    }

    @Test
    public void testRaw() {
        EntryProto.Entry entry = EntryProto.Entry.newBuilder().setSerial(0).setDescription("ijafijfiajf").build();
        EntryProto.Entry entry2 = EntryProto.Entry.newBuilder().setSerial(1).setDescription("fojofjof").build();
        EntryProto.Entry entry3 = EntryProto.Entry.newBuilder().setSerial(2).setDescription("dfkakfo").build();
        EntryProto.EntryList entryList = EntryProto.EntryList.newBuilder()
                .addEntry(entry)
                .addEntry(entry2)
                .addEntry(entry3)
                .build();
        System.out.println(entry2);
        System.out.println(entryList);

        try {
            System.out.println(JsonFormat.printer().print(entryList));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

}