package com.vv.personal.diurnal.dbi.util;

import com.vv.personal.diurnal.artifactory.generated.EntryProto;
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
        EntryProto.Entry entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, EntryProto.Currency.INR, 121.12, "Sampler s1");
        String json = convertEntryToJson(entry);
        System.out.println(json);
        assertEquals("{\n" +
                "  \"mobile\": \"123456789\",\n" +
                "  \"date\": 20210306,\n" +
                "  \"amount\": 121.12,\n" +
                "  \"description\": \"Sampler s1\"\n" +
                "}", json);
    }

    @Test
    public void testConvertEntryToCompactedJson() {
        EntryProto.Entry entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, EntryProto.Currency.INR, 121.12, "Sampler s1");
        String json = convertEntryToCompactedJson(entry);
        System.out.println(json);
        assertEquals("{\"mobile\": \"123456789\",\"date\": 20210306,\"amount\": 121.12,\"description\": \"Sampler s1\"}", json);

        entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, EntryProto.Currency.INR, 121.12, "Sampler s1\n second line this is !");
        json = convertEntryToCompactedJson(entry);
        System.out.println(json);
        assertEquals("{\"mobile\": \"123456789\",\"date\": 20210306,\"amount\": 121.12,\"description\": \"Sampler s1\\n second line this is !\"}", json);

        entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, EntryProto.Currency.INR, 121.12, "Sampler s1\n" +
                " second line this is !");
        json = convertEntryToCompactedJson(entry);
        System.out.println(json);
        assertEquals("{\"mobile\": \"123456789\",\"date\": 20210306,\"amount\": 121.12,\"description\": \"Sampler s1\\n second line this is !\"}", json);
    }

}