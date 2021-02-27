package com.vv.personal.diurnal.dbi.util;

import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;

import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_STR;

/**
 * @author Vivek
 * @since 27/02/21
 */
public class DiurnalUtil {

    public static UserMappingProto.UserMapping generateUserMappingFromMobile(Long mobile) {
        return generateUserMapping(mobile, EMPTY_STR);
    }

    public static UserMappingProto.UserMapping generateUserMapping(Long mobile, String user) {
        return UserMappingProto.UserMapping.newBuilder()
                .setMobile(mobile)
                .setUsername(user)
                .build();
    }


}
