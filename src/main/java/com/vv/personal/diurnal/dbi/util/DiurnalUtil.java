package com.vv.personal.diurnal.dbi.util;

import com.vv.personal.diurnal.artifactory.generated.DataTransitProto;
import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.artifactory.generated.TitleMappingProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import org.apache.commons.lang3.time.StopWatch;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.DEFAULT_AMOUNT;
import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_STR;

/**
 * @author Vivek
 * @since 27/02/21
 */
public class DiurnalUtil {

    public static UserMappingProto.UserMapping generateUserMappingOnPk(Long mobile) {
        return generateUserMapping(mobile, EMPTY_STR);
    }

    public static UserMappingProto.UserMapping generateUserMapping(Long mobile, String user) {
        return UserMappingProto.UserMapping.newBuilder()
                .setMobile(mobile)
                .setUsername(user)
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

    public static DataTransitProto.DataTransit generateDataTransit(Long mobile, Integer date, DataTransitProto.Currency currency, String backupData) {
        return DataTransitProto.DataTransit.newBuilder()
                .setMobile(mobile)
                .setDate(date)
                .setCurrency(currency)
                .setBackupData(backupData)
                .build();
    }

    public static StopWatch procureStopWatch() {
        return new StopWatch();
    }

    public static <V> List<Integer> performBulkOp(List<V> listToOpOn, Function<V, Integer> operation) {
        return listToOpOn.stream().map(operation).collect(Collectors.toList());
    }
}
