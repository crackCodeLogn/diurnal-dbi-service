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

import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_STR;
import static com.vv.personal.diurnal.dbi.constants.Constants.ONE;
import static com.vv.personal.diurnal.dbi.constants.DbConstants.PRIMARY_COL_USER_MAPPING;
import static com.vv.personal.diurnal.dbi.constants.DbConstants.SELECT_ALL;

/**
 * @author Vivek
 * @since 23/02/21
 */
public class DiurnalTableUserMapping extends DiurnalDbi<UserMappingProto.UserMapping, UserMappingProto.UserMappingList> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiurnalTableUserMapping.class);

    private final String INSERT_STMT_NEW_USER = "INSERT INTO %s(\"mobile\", \"user\", \"power_user\", \"cred\") " +
            "VALUES(%d, '%s', '%s', '%s')";
    private final String DELETE_STMT_USER = "DELETE FROM %s " +
            "WHERE \"%s\"=%d";
    private final String UPDATE_STMT_USER = "UPDATE %s " +
            "SET \"%s\"='%s' " +
            "WHERE \"%s\"=%d";
    private final String CHECK_STMT_ENTRY_SINGLE_COL = "SELECT %s from %s " +
            "WHERE \"%s\"=%d";

    private final String COL_USER = "user";
    private final String COL_MOBILE = "mobile";
    private final String COL_POWER_USER = "power_user";
    private final String COL_CRED = "cred";

    public DiurnalTableUserMapping(String table, String primaryColumns, DbiConfigForDiurnal dbiConfigForDiurnal, CachedDiurnal cachedDiurnal, Function<String, String> createTableIfNotExistSqlFunction, String createTableIfNotExistSqlLocation) {
        super(table, primaryColumns, dbiConfigForDiurnal, cachedDiurnal, createTableIfNotExistSqlFunction, createTableIfNotExistSqlLocation, LOGGER);
    }

    @Override
    public int pushNewEntity(UserMappingProto.UserMapping userMapping) {
        LOGGER.info("Pushing new User entity: {} x {} x {}", userMapping.getMobile(), userMapping.getUsername(), userMapping.getPowerUser());
        return insertNewUser(userMapping.getMobile(), userMapping.getUsername(), userMapping.getPowerUser(), userMapping.getCred());
    }

    private int insertNewUser(Long mobile, String username, Boolean powerUser, String cred) {
        String sql = String.format(INSERT_STMT_NEW_USER, TABLE,
                mobile, username, powerUser, cred);
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
        //return addToCacheOnSqlResult(sqlExecResult, mobile);
    }

    @Override
    public int deleteEntity(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(DELETE_STMT_USER, TABLE,
                COL_MOBILE, userMapping.getMobile());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
        //return removeFromCacheOnSqlResult(sqlExecResult, userMapping.getMobile());
    }

    @Override
    public int updateEntity(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER, TABLE,
                COL_USER, userMapping.getUsername(),
                COL_MOBILE, userMapping.getMobile());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updatePowerUserStatus(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER, TABLE,
                COL_POWER_USER, userMapping.getPowerUser(),
                COL_MOBILE, userMapping.getMobile());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public int updateCred(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(UPDATE_STMT_USER, TABLE,
                COL_CRED, userMapping.getCred(),
                COL_MOBILE, userMapping.getMobile());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    public String retrieveCred(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(CHECK_STMT_ENTRY_SINGLE_COL, COL_CRED, TABLE,
                COL_MOBILE, userMapping.getMobile());
        ResultSet resultSet = executeNonUpdateSql(sql);
        try {
            if (resultSet.next()) return generateCredDetail(resultSet).getCred();
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve cred hash from db. ", throwables);
        }
        return EMPTY_STR;
    }

    @Override
    public boolean checkEntity(UserMappingProto.UserMapping userMapping) {
        String sql = String.format(CHECK_STMT_ENTRY_SINGLE_COL, PRIMARY_COL_USER_MAPPING, TABLE,
                COL_MOBILE, userMapping.getMobile());
        return checkIfEntityExists(sql, ONE);
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
    public UserMappingProto.UserMappingList retrieveSelective() {
        return null;
    }

    @Override
    public UserMappingProto.UserMapping generateDetail(ResultSet resultSet) {
        UserMappingProto.UserMapping.Builder builder = UserMappingProto.UserMapping.newBuilder();
        try {
            builder.setMobile(resultSet.getLong(COL_MOBILE));
            builder.setUsername(resultSet.getString(COL_USER));
            builder.setPowerUser(resultSet.getBoolean(COL_POWER_USER));
            builder.setCred(resultSet.getString(COL_CRED));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve user-mapping detail from DB. ", throwables);
        }
        return builder.build();
    }

    public UserMappingProto.UserMapping generateCredDetail(ResultSet resultSet) {
        UserMappingProto.UserMapping.Builder builder = UserMappingProto.UserMapping.newBuilder();
        try {
            builder.setCred(resultSet.getString(COL_CRED));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve user-mapping cred detail from DB. ", throwables);
        }
        return builder.build();
    }

    /*@Override
    public int addToCacheOnSqlResult(Integer sqlResult, String table, Integer id) {
        return 0;
    }*/

    /*@Override
    public int pushNewEntity(Problem problem) {
        LOGGER.info("Pushing new problem '{}' into the DB", problem);
        if (cachedDiurnal.isIdPresentInEntityCache(TABLE, problem.getProblemId())) {
            LOGGER.info("Problem '{}' already present", problem.getProblemName());
            return 0;
        }
        return insertNewIntegerAndString(TABLE, problem.getProblemId(), problem.getProblemName());
    }*/

}
