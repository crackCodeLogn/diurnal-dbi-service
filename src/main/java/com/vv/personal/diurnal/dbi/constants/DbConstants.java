package com.vv.personal.diurnal.dbi.constants;

/**
 * @author Vivek
 * @since 25/02/21
 */
public class DbConstants {

    private DbConstants() {
    }

    public static final String DB_USER_STRING = "user";
    public static final String DB_CRED_STRING = "password";

    public static final String DB_CONNECTORS_URL = "jdbc:postgresql://%s:%d/%s";

    public static final String TABLE_DIURNAL_USER_MAPPING = "user_mapping";
    public static final String TABLE_DIURNAL_TITLE_MAPPING = "title_mapping";

    public static final String SELECT_ALL_IDS = "SELECT %s FROM %s";
    public static final String DROP_TABLE = "DROP TABLE %s";
    public static final String TRUNCATE_TABLE = "TRUNCATE TABLE %s";
}