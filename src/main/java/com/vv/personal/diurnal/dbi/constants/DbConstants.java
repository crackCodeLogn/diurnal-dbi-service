package com.vv.personal.diurnal.dbi.constants;

/**
 * @author Vivek
 * @since 25/02/21
 */
public class DbConstants {

    public static final String DB_USER_STRING = "user";
    public static final String DB_CRED_STRING = "password";
    public static final String DB_DIURNAL = "diurnal";

    public static final String DB_CONNECTORS_URL = "jdbc:postgresql://%s:%d/%s";

    public static final String TABLE_DIURNAL_USER_MAPPING = "user_mapping";
    public static final String TABLE_DIURNAL_TITLE_MAPPING = "title_mapping";
    public static final String TABLE_DIURNAL_ENTRY = "entry";
    public static final String TABLE_DIURNAL_ENTRY_DAY = "entry_day";

    public static final String PRIMARY_COL_ENTRY = "mobile,date,serial";
    public static final String PRIMARY_COL_ENTRY_DAY = "mobile,date";
    public static final String PRIMARY_COL_USER_MAPPING = "mobile";
    public static final String PRIMARY_COL_TITLE_MAPPING = "mobile,date";

    public static final String SELECT_ALL_IDS = "SELECT %s FROM %s";
    public static final String SELECT_ALL = "SELECT * FROM %s";

    public static final String FILE_SQL_LOCATION_BASE_CREATETABLES = "sql/createTables";
    public static final String DIURNAL_USER_MAPPING_SQL = "diurnal.user_mapping";
    public static final String DIURNAL_ENTRY_SQL = "diurnal.entry";
    public static final String DIURNAL_ENTRY_DAY_SQL = "diurnal.entry_day";
    public static final String DIURNAL_TITLE_MAPPING_SQL = "diurnal.title_mapping";
}
