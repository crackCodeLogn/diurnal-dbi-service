package com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables;

import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.config.DbiConfigForDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.cache.CachedDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.DiurnalDbi;
import com.vv.personal.diurnal.dbi.util.TimingUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;
import static com.vv.personal.diurnal.dbi.constants.DbConstants.SELECT_ALL;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateHash;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.refineEmail;

/**
 * @author Vivek
 * @since 23/02/21
 */
public class DiurnalTableUserMapping extends DiurnalDbi<UserMappingProto.UserMapping, UserMappingProto.UserMappingList> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiurnalTableUserMapping.class);

    private final String INSERT_STMT_NEW_USER = "INSERT INTO %s(\"mobile\", \"email\", \"user\", \"premium_user\", \"hash_cred\", \"hash_email\"," +
            " \"timestamp_save_cloud_last\", \"timestamp_save_last\", \"timestamp_expiry_payment\", \"timestamp_creation_account\", \"currency\") " +
            "VALUES(%d, '%s', '%s', '%s', '%s', %d, %d, %d, %d, %d, '%s')";
    private final String DELETE_STMT_USER = "DELETE FROM %s " +
            "WHERE \"%s\"=%d";
    private final String UPDATE_STMT_USER_STR = "UPDATE %s " +
            "SET \"%s\"='%s' " +
            "WHERE \"%s\"=%d";
    private final String UPDATE_STMT_USER_LONG = "UPDATE %s " +
            "SET \"%s\"=%d " +
            "WHERE \"%s\"=%d";
    private final String CHECK_STMT_ENTRY_SINGLE_COL = "SELECT %s from %s " +
            "WHERE \"%s\"=%d";
    private final String SELECT_STMT_ENTRY_SINGLE_COL_STR = "SELECT %s from %s " +
            "WHERE \"%s\"='%s'";
    private final String SELECT_STMT_ENTRY_SINGLE_ROW = "SELECT * FROM %s " +
            "WHERE \"%s\"='%s'";

    private final String COL_MOBILE = "mobile";
    private final String COL_EMAIL = "email";
    private final String COL_USER = "user";
    private final String COL_PREMIUM_USER = "premium_user";
    private final String COL_HASH_CRED = "hash_cred";
    private final String COL_HASH_EMAIL = "hash_email";
    private final String COL_LAST_CLOUD_SAVE_TIMESTAMP = "timestamp_save_cloud_last";
    private final String COL_LAST_SAVE_TIMESTAMP = "timestamp_save_last";
    private final String COL_PAYMENT_EXPIRY_TIMESTAMP = "timestamp_expiry_payment";
    private final String COL_ACCOUNT_CREATION_TIMESTAMP = "timestamp_creation_account";
    private final String COL_CURRENCY = "currency";

    public DiurnalTableUserMapping(String table, String primaryColumns, DbiConfigForDiurnal dbiConfigForDiurnal, CachedDiurnal cachedDiurnal, Function<String, String> createTableIfNotExistSqlFunction, String createTableIfNotExistSqlLocation) {
        super(table, primaryColumns, dbiConfigForDiurnal, cachedDiurnal, createTableIfNotExistSqlFunction, createTableIfNotExistSqlLocation, LOGGER);
    }

    @Override
    public int pushNewEntity(UserMappingProto.UserMapping userMapping) {
        String email = refineEmail(userMapping.getEmail());
        LOGGER.info("Pushing new User entity: {} x {} x {}", email, userMapping.getUsername(), false);
        return insertNewUser(userMapping.getMobile(), email, userMapping.getUsername(), false, userMapping.getHashCred(), generateHash(email),
                NA_LONG, NA_LONG, NA_LONG, TimingUtil.extractCurrentUtcTimestamp(), userMapping.getCurrency()); //new user is always non-premium
    }

    private int insertNewUser(Long mobile, String email, String username, Boolean premiumUser, String credHash, Integer emailHash,
                              Long lastCloudSaveTs, Long lastSaveTs, Long paymentExpiryTs, Long accountCreationTs, UserMappingProto.Currency currency) {
        String sql = String.format(INSERT_STMT_NEW_USER, TABLE,
                mobile, email, username, premiumUser, credHash, emailHash,
                lastCloudSaveTs, lastSaveTs, paymentExpiryTs, accountCreationTs, currency.name());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
        //return addToCacheOnSqlResult(sqlExecResult, mobile);
    }

    @Override
    public int deleteEntity(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(DELETE_STMT_USER, TABLE,
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    @Override
    public int updateEntity(UserMappingProto.UserMapping userMapping) { //updates the user name
        String sql = String.format(UPDATE_STMT_USER_STR, TABLE,
                COL_USER, userMapping.getUsername(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updatePremiumUserStatus(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER_STR, TABLE,
                COL_PREMIUM_USER, userMapping.getPremiumUser(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updateHashCred(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER_STR, TABLE,
                COL_HASH_CRED, userMapping.getHashCred(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updateMobile(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER_LONG, TABLE,
                COL_MOBILE, userMapping.getMobile(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updateCurrency(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER_STR, TABLE,
                COL_CURRENCY, userMapping.getCurrency(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updateLastCloudSaveTimestamp(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER_LONG, TABLE,
                COL_LAST_CLOUD_SAVE_TIMESTAMP, userMapping.getLastCloudSaveTimestamp(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updateLastSavedTimestamp(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER_LONG, TABLE,
                COL_LAST_SAVE_TIMESTAMP, userMapping.getLastSavedTimestamp(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updatePaymentExpiryTimestamp(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER_LONG, TABLE,
                COL_PAYMENT_EXPIRY_TIMESTAMP, userMapping.getPaymentExpiryTimestamp(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    //Shouldn't be invoked as doesn't make sense
    public int updateAccountCreationTimestamp(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER_LONG, TABLE,
                COL_ACCOUNT_CREATION_TIMESTAMP, userMapping.getAccountCreationTimestamp(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public String retrieveHashCred(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(CHECK_STMT_ENTRY_SINGLE_COL, COL_HASH_CRED, TABLE,
                COL_HASH_EMAIL, userMapping.getHashEmail());
        ResultSet resultSet = executeNonUpdateSql(sql);
        try {
            if (resultSet.next()) return generateHashCredDetail(resultSet).getHashCred();
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve cred hash from db. ", throwables);
        }
        return EMPTY_STR;
    }

    public Integer retrieveHashEmail(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(SELECT_STMT_ENTRY_SINGLE_COL_STR, COL_HASH_EMAIL, TABLE,
                COL_EMAIL, userMapping.getEmail());
        ResultSet resultSet = executeNonUpdateSql(sql);
        try {
            if (resultSet.next()) return generateHashEmailDetail(resultSet).getHashEmail();
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve email hash from db. ", throwables);
        }
        return NA_INT;
    }

    public Boolean retrievePremiumUserStatus(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(CHECK_STMT_ENTRY_SINGLE_COL, COL_PREMIUM_USER, TABLE,
                COL_HASH_EMAIL, userMapping.getHashEmail());
        ResultSet resultSet = executeNonUpdateSql(sql);
        try {
            if (resultSet.next()) return generatePremiumUserDetail(resultSet).getPremiumUser();
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve cred hash from db. ", throwables);
        }
        return false;
    }

    @Override
    public boolean checkEntity(UserMappingProto.UserMapping userMapping) {
        throw new UnsupportedOperationException("Check Entity to be done by hash retrieval attempt. Refer to #retrieveHashEmail method");
    }

    @Override
    public UserMappingProto.UserMappingList retrieveAll() {
        String sql = String.format(SELECT_ALL, TABLE);
        ResultSet resultSet = executeNonUpdateSql(sql);
        int rowsReturned = 0;
        UserMappingProto.UserMappingList.Builder userMappingsBuilder = UserMappingProto.UserMappingList.newBuilder();
        try {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    UserMappingProto.UserMapping userMapping = generateDetail(resultSet);
                    userMappingsBuilder.addUserMapping(userMapping);
                    rowsReturned++;
                } catch (SQLException throwables) {
                    LOGGER.error("Failed to completely extract result from the above select all query. ", throwables);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute / process sql '{}'. ", sql, e);
        }
        LOGGER.info("Received {} entries for sql => '{}'", rowsReturned, sql);
        return userMappingsBuilder.build();
    }

    @Override
    public UserMappingProto.UserMapping retrieveSingle(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(SELECT_STMT_ENTRY_SINGLE_ROW, TABLE,
                COL_HASH_EMAIL, userMapping.getHashEmail());
        ResultSet resultSet = executeNonUpdateSql(sql);
        try {
            if (resultSet.next()) return generateDetail(resultSet);
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve email hash from db. ", throwables);
        }
        return EMPTY_USER_MAPPING;
    }

    @Override
    public UserMappingProto.UserMappingList retrieveSome(UserMappingProto.UserMapping userMapping) {
        return null;
    }

    @Override
    public UserMappingProto.UserMapping generateDetail(ResultSet resultSet) {
        UserMappingProto.UserMapping.Builder builder = UserMappingProto.UserMapping.newBuilder();
        try {
            builder.setMobile(resultSet.getLong(COL_MOBILE));
            builder.setEmail(resultSet.getString(COL_EMAIL));
            builder.setUsername(resultSet.getString(COL_USER));
            builder.setPremiumUser(resultSet.getBoolean(COL_PREMIUM_USER));
            builder.setHashCred(resultSet.getString(COL_HASH_CRED));
            builder.setHashEmail(resultSet.getInt(COL_HASH_EMAIL));
            builder.setLastCloudSaveTimestamp(resultSet.getLong(COL_LAST_CLOUD_SAVE_TIMESTAMP));
            builder.setLastSavedTimestamp(resultSet.getLong(COL_LAST_SAVE_TIMESTAMP));
            builder.setPaymentExpiryTimestamp(resultSet.getLong(COL_PAYMENT_EXPIRY_TIMESTAMP));
            builder.setAccountCreationTimestamp(resultSet.getLong(COL_ACCOUNT_CREATION_TIMESTAMP));
            builder.setCurrency(UserMappingProto.Currency.valueOf(resultSet.getString(COL_CURRENCY)));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve user-mapping detail from DB. ", throwables);
        }
        return builder.build();
    }

    @Override
    protected Queue<String> processDataToCsv(UserMappingProto.UserMappingList dataList) {
        Queue<String> dataLines = new LinkedList<>();
        dataList.getUserMappingList().forEach(userMapping -> dataLines.add(
                StringUtils.joinWith(csvLineSeparator,
                        String.valueOf(userMapping.getMobile()), userMapping.getEmail(), userMapping.getUsername(), userMapping.getPremiumUser(), userMapping.getHashCred(), userMapping.getHashEmail(),
                        userMapping.getLastCloudSaveTimestamp(), userMapping.getLastSavedTimestamp(), userMapping.getPaymentExpiryTimestamp(), userMapping.getAccountCreationTimestamp(), userMapping.getCurrency())
                )
        );
        return dataLines;
    }

    public UserMappingProto.UserMapping generateHashCredDetail(ResultSet resultSet) {
        UserMappingProto.UserMapping.Builder builder = UserMappingProto.UserMapping.newBuilder();
        try {
            builder.setHashCred(resultSet.getString(COL_HASH_CRED));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve user-mapping cred detail from DB. ", throwables);
        }
        return builder.build();
    }

    public UserMappingProto.UserMapping generateHashEmailDetail(ResultSet resultSet) {
        UserMappingProto.UserMapping.Builder builder = UserMappingProto.UserMapping.newBuilder();
        try {
            builder.setHashEmail(resultSet.getInt(COL_HASH_EMAIL));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve user-mapping hash email detail from DB. ", throwables);
        }
        return builder.build();
    }

    public UserMappingProto.UserMapping generatePremiumUserDetail(ResultSet resultSet) {
        UserMappingProto.UserMapping.Builder builder = UserMappingProto.UserMapping.newBuilder();
        try {
            builder.setPremiumUser(resultSet.getBoolean(COL_PREMIUM_USER));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve user-mapping premium-user detail from DB. ", throwables);
        }
        return builder.build();
    }

}
