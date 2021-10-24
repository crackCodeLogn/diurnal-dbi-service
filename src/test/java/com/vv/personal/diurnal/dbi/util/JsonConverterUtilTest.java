package com.vv.personal.diurnal.dbi.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.vv.personal.diurnal.dbi.util.JsonConverterUtil.convertEntryToCompactedJson;
import static com.vv.personal.diurnal.dbi.util.JsonConverterUtil.convertEntryToJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vivek
 * @since 06/03/21
 */
@ExtendWith(MockitoExtension.class)
class JsonConverterUtilTest {

    @Test
    void testConvertEntryToJson() {
        EntryProto.Entry entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, UserMappingProto.Currency.INR, 121.12, "Sampler s1");
        String json = convertEntryToJson(entry);
        System.out.println(json);
        assertThat(json).hasToString("{\n" +
                "  \"mobile\": \"123456789\",\n" +
                "  \"date\": 20210306,\n" +
                "  \"currency\": \"INR\",\n" +
                "  \"amount\": 121.12,\n" +
                "  \"description\": \"Sampler s1\"\n" +
                "}");
    }

    @Test
    void testConvertEntryToCompactedJson() {
        EntryProto.Entry entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, UserMappingProto.Currency.INR, 121.12, "Sampler s1");
        String json = convertEntryToCompactedJson(entry);
        System.out.println(json);
        assertThat(json).hasToString("{\"mobile\": \"123456789\",\"date\": 20210306,\"currency\": \"INR\",\"amount\": 121.12,\"description\": \"Sampler s1\"}");

        entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, UserMappingProto.Currency.INR, 121.12, "Sampler s1\n second line this is !");
        json = convertEntryToCompactedJson(entry);
        System.out.println(json);
        assertThat(json).hasToString("{\"mobile\": \"123456789\",\"date\": 20210306,\"currency\": \"INR\",\"amount\": 121.12,\"description\": \"Sampler s1\\n second line this is !\"}");

        entry = DiurnalUtil.generateEntry(123456789L, 20210306, 0, EntryProto.Sign.NEGATIVE, UserMappingProto.Currency.INR, 121.12, "Sampler s1\n" +
                " second line this is !");
        json = convertEntryToCompactedJson(entry);
        System.out.println(json);
        assertThat(json).hasToString("{\"mobile\": \"123456789\",\"date\": 20210306,\"currency\": \"INR\",\"amount\": 121.12,\"description\": \"Sampler s1\\n second line this is !\"}");
    }

    @Test
    void testRaw() {
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

    @Test
    void testConvertSqlEntryToProtoEntry() {
        String sqlEntry = "{`@%currency`@%: `@%INR`@%,`@%amount`@%: 100.0,`@%description`@%: `@%lunch`@%}";
        EntryProto.Entry convertedEntry = JsonConverterUtil.convertSqlEntryToProtoEntry(sqlEntry);
        System.out.println(convertedEntry);
    }

    @Test
    void testConvertSqlEntriesToEntryProtoList() {
        String sqlEntries = "{`@%currency`@%: `@%INR`@%,`@%amount`@%: 100.0,`@%description`@%: `@%lunch`@%}%~@{`@%currency`@%: `@%INR`@%,`@%amount`@%: 110.0,`@%description`@%: `@%surf excel matic 500gm`@%,`@%serial`@%: 1}%~@{`@%currency`@%: `@%INR`@%,`@%amount`@%: 30.0,`@%description`@%: `@%2 x bhel`@%,`@%serial`@%: 2}%~@{`@%currency`@%: `@%INR`@%,`@%amount`@%: 159.0,`@%description`@%: `@%pillow`@%,`@%serial`@%: 3}%~@{`@%currency`@%: `@%INR`@%,`@%amount`@%: 155.0,`@%description`@%: `@%cornflakes original`@%,`@%serial`@%: 4}%~@{`@%currency`@%: `@%INR`@%,`@%amount`@%: 19.0,`@%description`@%: `@%channi`@%,`@%serial`@%: 5}%~@{`@%currency`@%: `@%INR`@%,`@%amount`@%: 20.0,`@%description`@%: `@%2 x perk (Rs 5) + 1 munch (Rs 10)`@%,`@%serial`@%: 6}";
        EntryProto.EntryList convertedEntryList = JsonConverterUtil.convertSqlEntriesToEntryProtoList(sqlEntries);
        System.out.println(convertedEntryList);
        assertThat(convertedEntryList.getEntryCount()).isEqualTo(7);
        assertThat(convertedEntryList.getEntry(5).getDescription()).hasToString("channi");
    }

    @Test
    void test() {
        System.out.println("amazon@apl".matches("[a-z0-9\\-]+@[a-z0-9]+"));
        System.out.println("money-warden@apl".matches("[a-z0-9-]+@[a-z0-9]+"));
        System.out.println("a@apl".matches("[a-z0-9]{3}+@[a-z0-9]+"));

        System.out.println(UUID.randomUUID());
    }
}