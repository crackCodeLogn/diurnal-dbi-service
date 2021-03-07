package com.vv.personal.diurnal.dbi.util;

import com.vv.personal.diurnal.artifactory.generated.*;
import com.vv.personal.diurnal.dbi.interactor.IDbi;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;

/**
 * @author Vivek
 * @since 27/02/21
 */
public class DiurnalUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(DiurnalUtil.class);

    public static UserMappingProto.UserMapping generateUserMappingOnPk(Long mobile) {
        return generateUserMapping(mobile, EMPTY_STR);
    }

    public static UserMappingProto.UserMapping generateUserMapping(Long mobile, String user) {
        return generateUserMapping(mobile, user, false);
    }

    public static UserMappingProto.UserMapping generateUserMapping(Long mobile, String user, Boolean powerUser) {
        return UserMappingProto.UserMapping.newBuilder()
                .setMobile(mobile)
                .setUsername(user)
                .setPowerUser(powerUser)
                .build();
    }

    public static TitleMappingProto.TitleMapping generateTitleMappingOnPk(Long mobile, Integer date) {
        return generateTitleMapping(mobile, date, EMPTY_STR);
    }

    public static TitleMappingProto.TitleMapping generateTitleMapping(Long mobile, Integer date, String title) {
        return TitleMappingProto.TitleMapping.newBuilder()
                .setMobile(mobile)
                .setDate(date)
                .setTitle(title)
                .build();
    }

    public static EntryProto.Entry generateEntryOnPk(Long mobile, Integer date, Integer serial) {
        return generateEntry(mobile, date, serial,
                EntryProto.Sign.NEGATIVE, EntryProto.Currency.INR, DEFAULT_AMOUNT, EMPTY_STR);
    }

    public static EntryProto.Entry generateEntry(Long mobile, Integer date, Integer serial,
                                                 EntryProto.Sign sign, EntryProto.Currency currency, Double amount, String description) {
        return EntryProto.Entry.newBuilder()
                .setMobile(mobile)
                .setDate(date)
                .setSerial(serial)
                .setSign(sign).setCurrency(currency).setAmount(amount).setDescription(description)
                .build();
    }

    public static EntryProto.Entry generateLightEntry(Integer date, Integer serial,
                                                      EntryProto.Sign sign, EntryProto.Currency currency, Double amount, String description) {
        return EntryProto.Entry.newBuilder()
                .setDate(date)
                .setSerial(serial)
                .setSign(sign).setCurrency(currency).setAmount(amount).setDescription(description)
                .build();
    }

    public static EntryDayProto.EntryDay generateEntryDayOnPk(Long mobile, Integer date) {
        return generateEntryDay(mobile, date, EMPTY_STR);
    }

    public static EntryDayProto.EntryDay generateEntryDay(Long mobile, Integer date, String entriesAsString) {
        return EntryDayProto.EntryDay.newBuilder()
                .setMobile(mobile)
                .setDate(date)
                .setEntriesAsString(entriesAsString)
                .build();
    }

    public static DataTransitProto.DataTransit generateDataTransit(Long mobile, Integer date, DataTransitProto.Currency currency, String backupData) {
        return DataTransitProto.DataTransit.newBuilder()
                .setMobile(mobile)
                .setDate(date)
                .setCurrency(currency)
                .setBackupData(backupData)
                .build();
    }

    public static ResponsePrimitiveProto.ResponsePrimitive generateResponsePrimitive(Boolean value) {
        return ResponsePrimitiveProto.ResponsePrimitive.newBuilder()
                .setBoolResponse(value)
                .build();
    }

    public static StopWatch procureStopWatch() {
        return new StopWatch();
    }

    public static <V> List<Integer> performBulkOpInt(List<V> listToOpOn, Function<V, Integer> operation) {
        return listToOpOn.stream().map(operation).collect(Collectors.toList());
    }

    public static <V> List<String> performBulkOpStr(List<V> listToOpOn, Function<V, String> operation) {
        return listToOpOn.stream().map(operation).collect(Collectors.toList());
    }

    public static int genericCreateTableIfNotExists(IDbi dbi) {
        LOGGER.warn("Proceeding to create table if not exists: '{}'", dbi.getTableName());
        int createTableIfNotExistsResult = dbi.createTableIfNotExists();
        LOGGER.warn("Table '{}' create if not exists result: {}", dbi.getTableName(), createTableIfNotExistsResult);
        return createTableIfNotExistsResult;
    }

    public static Boolean genericDropTable(IDbi dbi) {
        LOGGER.warn("Proceeding to drop table: '{}'", dbi.getTableName());
        Boolean dropResult = dbi.dropTable() == 0;
        LOGGER.warn("Table '{}' drop result: {}", dbi.getTableName(), dropResult);
        return dropResult;
    }

    public static Boolean genericTruncateTable(IDbi dbi) {
        LOGGER.warn("Proceeding to truncate table: '{}'", dbi.getTableName());
        Boolean truncateResult = dbi.truncateTable() == 0;
        LOGGER.warn("Table '{}' truncate result: {}", dbi.getTableName(), truncateResult);
        return truncateResult;
    }

    public static String processStringForSqlPush(String input) {
        return input.replaceAll("'", "''")
                .replaceAll(",\"", "," + REPLACE_JSON_DI) //`@% -- REPLACE_JSON_DI
                .replaceAll("\",", REPLACE_JSON_DI + ",")
                .replaceAll("\\{\"", "\\{" + REPLACE_JSON_DI)
                .replaceAll("\": \"", REPLACE_JSON_DI + ": " + REPLACE_JSON_DI)
                .replaceAll("\":", REPLACE_JSON_DI + ":")
                .replaceAll("\"}", REPLACE_JSON_DI + "}");
    }

    public static String refineDbStringForOriginal(String data) {
        return data.replaceAll("''", "'")
                .replaceAll(REPLACE_JSON_DI, "\"");
    }
}
