package com.vv.personal.diurnal.dbi.engine.transformer.parser;

import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;

/**
 * @author Vivek
 * @since 04/03/21
 */
public abstract class AbstractLineParser implements LineParser {
    protected final String line;

    public AbstractLineParser(String line) {
        this.line = line;
    }

    protected EntryProto.Sign parseSign(String data) {
        switch (data.trim()) {
            case "+":
                return EntryProto.Sign.POSITIVE;
            case "-":
            default:
                return EntryProto.Sign.NEGATIVE;
        }
    }

    protected UserMappingProto.Currency parseCurrency(String data) {
        switch (data.trim()) {
            case "₹":
                return UserMappingProto.Currency.INR;
            case "$":
                return UserMappingProto.Currency.USD;
            case "$C":
                return UserMappingProto.Currency.CND;
            case "¥":
                return UserMappingProto.Currency.YEN;
            case "€":
                return UserMappingProto.Currency.EUR;
            case "£":
                return UserMappingProto.Currency.GBP;
            case "₽":
                return UserMappingProto.Currency.RUB;
            case "₣":
                return UserMappingProto.Currency.FR;
            default:
                return UserMappingProto.Currency.INR;
        }
    }
}
