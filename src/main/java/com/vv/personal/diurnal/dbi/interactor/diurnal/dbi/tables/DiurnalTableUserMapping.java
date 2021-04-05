package com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables;

import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;
import com.vv.personal.diurnal.dbi.config.DbiConfigForDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.cache.CachedDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.DiurnalDbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    private final String INSERT_STMT_NEW_USER = "INSERT INTO %s(\"mobile\", \"email\", \"user\", \"premium_user\", \"hash_cred\", \"hash_email\") " +
            "VALUES(%d, '%s', '%s', '%s', '%s', %d)";
    private final String DELETE_STMT_USER = "DELETE FROM %s " +
            "WHERE \"%s\"=%d";
    private final String UPDATE_STMT_USER = "UPDATE %s " +
            "SET \"%s\"='%s' " +
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

    public DiurnalTableUserMapping(String table, String primaryColumns, DbiConfigForDiurnal dbiConfigForDiurnal, CachedDiurnal cachedDiurnal, Function<String, String> createTableIfNotExistSqlFunction, String createTableIfNotExistSqlLocation) {
        super(table, primaryColumns, dbiConfigForDiurnal, cachedDiurnal, createTableIfNotExistSqlFunction, createTableIfNotExistSqlLocation, LOGGER);
    }

    @Override
    public int pushNewEntity(UserMappingProto.UserMapping userMapping) {
        String email = refineEmail(userMapping.getEmail());
        LOGGER.info("Pushing new User entity: {} x {} x {}", email, userMapping.getUsername(), userMapping.getPremiumUser());
        return insertNewUser(userMapping.getMobile(), email, userMapping.getUsername(), userMapping.getPremiumUser(), userMapping.getHashCred(),
                generateHash(email));
    }

    private int insertNewUser(Long mobile, String email, String username, Boolean premiumUser, String credHash, Integer emailHash) {
        String sql = String.format(INSERT_STMT_NEW_USER, TABLE,
                mobile, email, username, premiumUser, credHash, emailHash);
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
    public int updateEntity(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER, TABLE,
                COL_USER, userMapping.getUsername(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updatePremiumUserStatus(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER, TABLE,
                COL_PREMIUM_USER, userMapping.getPremiumUser(),
                COL_HASH_EMAIL, userMapping.getHashEmail());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updateHashCred(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER, TABLE,
                COL_HASH_CRED, userMapping.getHashCred(),
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
    public UserMappingProto.UserMapping retrieveSelective(UserMappingProto.UserMapping userMapping) {
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
    public UserMappingProto.UserMapping generateDetail(ResultSet resultSet) {
        UserMappingProto.UserMapping.Builder builder = UserMappingProto.UserMapping.newBuilder();
        try {
            builder.setMobile(resultSet.getLong(COL_MOBILE));
            builder.setEmail(resultSet.getString(COL_EMAIL));
            builder.setUsername(resultSet.getString(COL_USER));
            builder.setPremiumUser(resultSet.getBoolean(COL_PREMIUM_USER));
            builder.setHashCred(resultSet.getString(COL_HASH_CRED));
            builder.setHashEmail(resultSet.getInt(COL_HASH_EMAIL));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve user-mapping detail from DB. ", throwables);
        }
        return builder.build();
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
