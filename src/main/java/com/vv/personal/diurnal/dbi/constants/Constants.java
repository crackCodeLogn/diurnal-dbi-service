package com.vv.personal.diurnal.dbi.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek
 * @since 23/12/20
 */
public class Constants {

    public static final String EMPTY_STR = "";
    public static final String SPACE_STR = " ";
    public static final String COMMA_STR = ",";
    public static final String COLON_STR = ":";
    public static final String DEFAULT_STR_INVALID = "-1";

    public static final int DEFAULT_INT_INVALID = -1;
    public static final int ONE = 1;
    public static final double DEFAULT_AMOUNT = 0.0;

    public static final List<Integer> EMPTY_LIST_INT = new ArrayList<>(0);

    //RESPONSES
    public static final Integer INT_RESPONSE_WONT_PROCESS = -13; //N Proc

    //FORMATTERS
    public static final String HEROKU_SWAGGER_UI_URL = "https://%s/swagger-ui/index.html";
    public static final String SWAGGER_UI_URL = "http://%s:%s/swagger-ui/index.html";
    public static final String HEROKU_HOST_URL = "https://%s";
    public static final String HOST_URL = "http://%s:%s";

    public static final String LOCALHOST = "localhost";
    public static final String LOCAL_SPRING_HOST = "local.server.host";
    public static final String LOCAL_SPRING_PORT = "local.server.port";
    public static final String SPRING_APPLICATION_HEROKU = "spring.application.heroku";

}
