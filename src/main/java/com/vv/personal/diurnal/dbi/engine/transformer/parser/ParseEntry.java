package com.vv.personal.diurnal.dbi.engine.transformer.parser;

import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import org.apache.commons.lang3.StringUtils;

import static com.vv.personal.diurnal.dbi.constants.Constants.DEFAULT_AMOUNT;

/**
 * @author Vivek
 * @since 04/03/21
 */
public class ParseEntry extends AbstractLineParser {
    private EntryProto.Sign sign;
    private UserMappingProto.Currency currency;
    private Double amount;
    private String description;

    public ParseEntry(String line) {
        super(line);
    }

    @Override
    public void parse() {
        if (line.startsWith("//")) {
            this.sign = EntryProto.Sign.COMMENT;
            this.amount = DEFAULT_AMOUNT;
            this.description = line.substring(2);
            this.currency = UserMappingProto.Currency.INR;
        } else {
            String[] parts = StringUtils.split(line.trim(), ":");
            this.description = parts[1].trim();
            parts = StringUtils.split(parts[0].trim(), " ");
            this.sign = parseSign(parts[0]);
            this.currency = parseCurrency(parts[1]);
            this.amount = Double.parseDouble(parts[2].trim());
        }
    }

    public EntryProto.Sign getSign() {
        return sign;
    }

    public UserMappingProto.Currency getCurrency() {
        return currency;
    }

    public Double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
