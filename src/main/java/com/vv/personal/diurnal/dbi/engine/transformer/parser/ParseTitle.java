package com.vv.personal.diurnal.dbi.engine.transformer.parser;

import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static com.vv.personal.diurnal.dbi.constants.Constants.DEFAULT_TITLE;
import static com.vv.personal.diurnal.dbi.constants.Constants.TITLES_TO_EXEMPT;

/**
 * @author Vivek
 * @since 04/03/21
 */
@Getter
public class ParseTitle extends AbstractLineParser {
    private int date;
    private String title;
    //private EntryProto.Sign sign;
    //private UserMappingProto.Currency currency;
    //private Double dayTotal;

    public ParseTitle(String line) {
        super(line);
    }

    @Override
    public void parse() {
        String[] split = StringUtils.split(line, "::");
        int spaceIndex = split[0].indexOf(' ');
        this.date = DiurnalUtil.convertDisplayDateFormatToEntryDayDate(split[0].substring(0, spaceIndex).trim());
        this.title = split[0].substring(spaceIndex).trim();
//        split = StringUtils.split(split[1].trim(), " ");
//        this.sign = parseSign(split[0]);
//        this.currency = parseCurrency(split[1]);
//        this.dayTotal = Double.parseDouble(split[2]);
    }

    public String getRefinedTitle() {
        return isTitleToExempt() ? DEFAULT_TITLE :
                DiurnalUtil.processStringForSqlPush(title);
    }

    public boolean isTitleToExempt() {
        return TITLES_TO_EXEMPT.contains(title);
    }
}