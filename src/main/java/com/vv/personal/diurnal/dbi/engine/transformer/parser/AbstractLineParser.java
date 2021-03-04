package com.vv.personal.diurnal.dbi.engine.transformer.parser;

import com.vv.personal.diurnal.artifactory.generated.EntryProto;

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

    protected EntryProto.Currency parseCurrency(String data) {
        switch (data.trim()) {
            case "$":
                return EntryProto.Currency.USD;
            case "$C":
                return EntryProto.Currency.CND;
            case "₹":
            default:
                return EntryProto.Currency.INR;
        }
    }
}
