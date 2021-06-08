package com.vv.personal.diurnal.dbi.util;

import com.vv.personal.diurnal.artifactory.generated.*;
import com.vv.personal.diurnal.dbi.interactor.IDbi;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;

/**
 * @author Vivek
 * @since 27/02/21
 */
public class DiurnalUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(DiurnalUtil.class);

    public static Integer generateHash(String data) {
        final Integer hash = Objects.hashCode(data);
        LOGGER.info("Generated hash [{}] for [{}]", hash, data);
        return hash;
    }

    public static String refineEmail(String email) {
        return email.toLowerCase().trim();
    }

    public static Boolean isEmailHashAbsent(Integer emailHash) {
        return emailHash == DEFAULT_EMAIL_HASH;
    }

    public static UserMappingProto.UserMapping generateUserMapping(String email) {
        return generateUserMapping(refineEmail(email), DEFAULT_USER_NAME);
    }

    public static UserMappingProto.UserMapping generateUserMapping(String email, String user) {
        return generateUserMapping(DEFAULT_MOBILE, refineEmail(email), user, DEFAULT_PREMIUM_USER_STATUS, DEFAULT_USER_CRED_HASH);
    }

    public static UserMappingProto.UserMapping generateUserMapping(String email, UserMappingProto.Currency currency) {
        return generateCompleteUserMapping(DEFAULT_MOBILE, refineEmail(email), DEFAULT_USER_NAME, DEFAULT_PREMIUM_USER_STATUS, DEFAULT_USER_CRED_HASH, DEFAULT_EMAIL_HASH,
                DEFAULT_LAST_CLOUD_SAVE_TS, DEFAULT_LAST_SAVE_TS, DEFAULT_PAYMENT_EXPIRY_TS, DEFAULT_ACCOUNT_CREATION_TS, currency);
    }

    public static UserMappingProto.UserMapping generateUserMapping(Long mobile, String email, String user, Boolean premiumUser, String credHash) {
        return generateCompleteUserMapping(mobile, refineEmail(email), user, premiumUser, credHash, DEFAULT_EMAIL_HASH,
                DEFAULT_LAST_CLOUD_SAVE_TS, DEFAULT_LAST_SAVE_TS, DEFAULT_PAYMENT_EXPIRY_TS, DEFAULT_ACCOUNT_CREATION_TS, DEFAULT_CURRENCY);
    }

    public static UserMappingProto.UserMapping generateUserMapping(Boolean premiumUser, Integer emailHash) {
        return generateCompleteUserMapping(DEFAULT_MOBILE, DEFAULT_EMAIL, DEFAULT_USER_NAME, premiumUser, DEFAULT_USER_CRED_HASH, emailHash,
                DEFAULT_LAST_CLOUD_SAVE_TS, DEFAULT_LAST_SAVE_TS, DEFAULT_PAYMENT_EXPIRY_TS, DEFAULT_ACCOUNT_CREATION_TS, DEFAULT_CURRENCY);
    }

    public static UserMappingProto.UserMapping generateUserMappingOnPk(Integer emailHash) {
        return generateCompleteUserMapping(DEFAULT_MOBILE, DEFAULT_EMAIL, DEFAULT_USER_NAME, DEFAULT_PREMIUM_USER_STATUS, DEFAULT_USER_CRED_HASH, emailHash,
                DEFAULT_LAST_CLOUD_SAVE_TS, DEFAULT_LAST_SAVE_TS, DEFAULT_PAYMENT_EXPIRY_TS, DEFAULT_ACCOUNT_CREATION_TS, DEFAULT_CURRENCY);
    }

    public static UserMappingProto.UserMapping generateCompleteUserMapping(UserMappingProto.UserMapping userMapping, Integer emailHash) {
        return UserMappingProto.UserMapping.newBuilder()
                .mergeFrom(userMapping)
                .setHashEmail(emailHash)
                .build();
    }

    public static UserMappingProto.UserMapping generateCompleteUserMapping(Long mobile, String email, String user, Boolean premiumUser, String credHash, Integer emailHash,
                                                                           Long lastCloudSaveTs, Long lastSaveTs, Long paymentExpiryTs, Long accountCreationTs, UserMappingProto.Currency currency) {
        return UserMappingProto.UserMapping.newBuilder()
                .setMobile(mobile)
                .setEmail(email)
                .setUsername(user)
                .setPremiumUser(premiumUser)
                .setHashCred(credHash)
                .setHashEmail(emailHash)
                .setLastCloudSaveTimestamp(lastCloudSaveTs)
                .setLastSavedTimestamp(lastSaveTs)
                .setPaymentExpiryTimestamp(paymentExpiryTs)
                .setAccountCreationTimestamp(accountCreationTs)
                .setCurrency(currency)
                .build();
    }

    @Deprecated
    public static TitleMappingProto.TitleMapping generateTitleMappingOnPk(Long mobile, Integer date) {
        return generateTitleMapping(mobile, date, EMPTY_STR);
    }

    public static TitleMappingProto.TitleMapping generateTitleMapping(Long mobile, Integer date, String title) {
        return generateTitleMapping(mobile, NA_INT, date, title);
    }

    public static TitleMappingProto.TitleMapping generateTitleMapping(Integer emailHash, Integer date, String title) {
        return generateTitleMapping(NA_LONG, emailHash, date, title);
    }

    public static TitleMappingProto.TitleMapping generateTitleMapping(Long mobile, Integer emailHash, Integer date, String title) {
        return TitleMappingProto.TitleMapping.newBuilder()
                .setMobile(mobile)
                .setHashEmail(emailHash)
                .setDate(date)
                .setTitle(title)
                .build();
    }

    @Deprecated
    public static EntryProto.Entry generateEntryOnPk(Long mobile, Integer date, Integer serial) {
        return generateEntry(mobile, date, serial,
                EntryProto.Sign.NEGATIVE, UserMappingProto.Currency.INR, DEFAULT_AMOUNT, EMPTY_STR);
    }

    @Deprecated
    public static EntryProto.Entry generateEntry(Long mobile, Integer date, Integer serial,
                                                 EntryProto.Sign sign, UserMappingProto.Currency currency, Double amount, String description) {
        return EntryProto.Entry.newBuilder()
                .setMobile(mobile)
                .setDate(date)
                .setSerial(serial)
                .setSign(sign).setCurrency(currency.name()).setAmount(amount).setDescription(description)
                .build();
    }

    // generates an instance of entry which is not distinct on it's own - for insertion in entryday
    public static EntryProto.Entry generateLiteEntry(Integer serial, EntryProto.Sign sign, UserMappingProto.Currency currency, Double amount, String description) {
        return EntryProto.Entry.newBuilder()
                .setSerial(serial)
                .setSign(sign)
                .setCurrency(currency.name())
                .setAmount(amount)
                .setDescription(description)
                .build();
    }

    public static EntryDayProto.EntryDay generateEntryDayOnPk(Integer emailHash, Integer date) {
        return generateEntryDay(emailHash, date, EMPTY_STR);
    }

    public static EntryDayProto.EntryDay generateEntryDayOnHash(Integer emailHash) {
        return generateEntryDay(emailHash, NA_INT, EMPTY_STR);
    }

    public static EntryDayProto.EntryDay generateEntryDay(Integer emailHash, Integer date, String entriesAsString) {
        return generateCompleteEntryDay(emailHash, date, EMPTY_STR, entriesAsString);
    }

    public static EntryDayProto.EntryDay generateCompleteEntryDay(Integer emailHash, Integer date, String title, String entriesAsString) {
        return EntryDayProto.EntryDay.newBuilder()
                .setHashEmail(emailHash)
                .setDate(date)
                .setTitle(title)
                .setEntriesAsString(entriesAsString)
                .build();
    }

    public static DataTransitProto.DataTransit generateDataTransit(Long mobile, String email, Integer date, UserMappingProto.Currency currency, String backupData) {
        return DataTransitProto.DataTransit.newBuilder()
                .setMobile(mobile)
                .setEmail(email)
                .setDate(date)
                .setCurrency(currency.name())
                .setBackupData(backupData)
                .build();
    }

    public static DataTransitProto.DataTransit generateDataTransit(String email) {
        return DataTransitProto.DataTransit.newBuilder()
                .setEmail(email)
                .build();
    }

    public static ResponsePrimitiveProto.ResponsePrimitive generateResponsePrimitiveBool(Boolean value) {
        return ResponsePrimitiveProto.ResponsePrimitive.newBuilder()
                .setBoolResponse(value)
                .build();
    }

    public static ResponsePrimitiveProto.ResponsePrimitive generateResponsePrimitiveString(String value) {
        return ResponsePrimitiveProto.ResponsePrimitive.newBuilder()
                .setResponse(value)
                .build();
    }

    public static ResponsePrimitiveProto.ResponsePrimitive generateResponsePrimitiveInt(Integer value) {
        return ResponsePrimitiveProto.ResponsePrimitive.newBuilder()
                .setIntegralResponse(value)
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
                .replaceAll("\"}", REPLACE_JSON_DI + "}")
                .trim();
    }

    public static String refineDbStringForOriginal(String data) {
        return data.replaceAll("''", "'")
                .replaceAll(REPLACE_JSON_DI, "\"");
    }

    public static String getDefaultCsvDumpLocation() {
        return System.getProperty("java.io.tmpdir");
    }

    public static String convertEntryDayDateToDisplayFormat(Integer date) {
        LocalDate localDate = LocalDate.parse(String.valueOf(date), DTF_ENTRY_DAY_DATE_PATTERN);
        return localDate.format(DTF_APP_DISPLAY_DATE_PATTERN);
    }

    public static Integer convertDisplayDateFormatToEntryDayDate(String date) {
        LocalDate localDate = LocalDate.parse(date, DTF_APP_DISPLAY_DATE_PATTERN);
        return Integer.valueOf(localDate.format(DTF_ENTRY_DAY_DATE_PATTERN));
    }
}
