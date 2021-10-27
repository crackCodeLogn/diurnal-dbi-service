package com.vv.personal.diurnal.dbi.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.vv.personal.diurnal.dbi.constants.Constants.COMMA_STR;
import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_STR;

/**
 * @author Vivek
 * @since 02/01/21
 */
public class DbiUtil {

    private DbiUtil() {
    }

    public static String extractContactNumbers(Collection<String> contactNumbers) {
        String contactNumbersString = EMPTY_STR;
        if (!contactNumbers.isEmpty())
            contactNumbersString = contactNumbers.stream()
                    .map(String::trim)
                    .filter(number -> !number.isEmpty())
                    .collect(Collectors.joining(COMMA_STR));
        return contactNumbersString;
    }

    public static Collection<String> convertToContactList(String contactNumbers) {
        if (contactNumbers.isEmpty()) return Collections.emptyList();
        return Arrays.stream(StringUtils.split(contactNumbers, COMMA_STR))
                .collect(Collectors.toList());
    }
}