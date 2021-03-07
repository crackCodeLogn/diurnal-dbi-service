package com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables;

import com.vv.personal.diurnal.artifactory.generated.TitleMappingProto;
import com.vv.personal.diurnal.dbi.config.DbiConfigForDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.cache.CachedDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.DiurnalDbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import static com.vv.personal.diurnal.dbi.constants.Constants.ONE;
import static com.vv.personal.diurnal.dbi.constants.DbConstants.PRIMARY_COL_TITLE_MAPPING;
import static com.vv.personal.diurnal.dbi.constants.DbConstants.SELECT_ALL;

/**
 * @author Vivek
 * @since 24/02/21
 */
@Deprecated
public class DiurnalTableTitleMapping extends DiurnalDbi<TitleMappingProto.TitleMapping, TitleMappingProto.TitleMappingList> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiurnalTableTitleMapping.class);

    private final String INSERT_STMT_NEW_TITLE = "INSERT INTO %s(\"mobile\", \"date\", \"title\") " +
            "VALUES(%d, %d, '%s')";
    private final String DELETE_STMT_TITLE = "DELETE FROM %s " +
            "WHERE \"%s\"=%d and \"%s\"=%d";
    private final String UPDATE_STMT_TITLE = "UPDATE %s " +
            "SET \"%s\"='%s' " +
            "WHERE \"%s\"=%d and \"%s\"=%d";
    private final String CHECK_STMT_TITLE_EXISTS = "SELECT %s from %s " +
            "WHERE \"%s\"=%d and \"%s\"=%d";

    private final String COL_DATE = "date";
    private final String COL_MOBILE = "mobile";
    private final String COL_TITLE = "title";

    public DiurnalTableTitleMapping(String table, String primaryColumns, DbiConfigForDiurnal dbiConfigForDiurnal, CachedDiurnal cachedDiurnal, Function<String, String> createTableIfNotExistSqlFunction, String createTableIfNotExistSqlLocation) {
        super(table, primaryColumns, dbiConfigForDiurnal, cachedDiurnal, createTableIfNotExistSqlFunction, createTableIfNotExistSqlLocation, LOGGER);
    }

    @Override
    public int pushNewEntity(TitleMappingProto.TitleMapping titleMapping) {
        LOGGER.info("Pushing new Title entity: {} x {} - {}", titleMapping.getMobile(), titleMapping.getDate(), titleMapping.getTitle());
        return insertNewTitle(titleMapping.getMobile(), titleMapping.getDate(), titleMapping.getTitle());
    }

    private int insertNewTitle(Long mobile, Integer date, String title) {
        String sql = String.format(INSERT_STMT_NEW_TITLE, TABLE, mobile, date, title);
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
        //return addToCacheOnSqlResult(sqlExecResult, mobile);
    }

    @Override
    public int deleteEntity(TitleMappingProto.TitleMapping titleMapping) {
        String sql = String.format(DELETE_STMT_TITLE, TABLE,
                COL_MOBILE, titleMapping.getMobile(),
                COL_DATE, titleMapping.getDate());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
        //return removeFromCacheOnSqlResult(sqlExecResult, userMapping.getMobile());
    }

    @Override
    public int updateEntity(TitleMappingProto.TitleMapping titleMapping) {
        String sql = String.format(UPDATE_STMT_TITLE, TABLE,
                COL_TITLE, titleMapping.getTitle(),
                COL_MOBILE, titleMapping.getMobile(),
                COL_DATE, titleMapping.getDate());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
    }

    @Override
    public boolean checkEntity(TitleMappingProto.TitleMapping titleMapping) {
        String sql = String.format(CHECK_STMT_TITLE_EXISTS, PRIMARY_COL_TITLE_MAPPING, TABLE,
                COL_MOBILE, titleMapping.getMobile(),
                COL_DATE, titleMapping.getDate());
        return checkIfEntityExists(sql, ONE);
    }

    @Override
    public TitleMappingProto.TitleMappingList retrieveAll() {
        String sql = String.format(SELECT_ALL, TABLE);
        ResultSet resultSet = executeNonUpdateSql(sql);
        int rowsReturned = 0;
        TitleMappingProto.TitleMappingList.Builder titleMappingsBuilder = TitleMappingProto.TitleMappingList.newBuilder();
        try {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    TitleMappingProto.TitleMapping userMapping = generateDetail(resultSet);
                    titleMappingsBuilder.addTitleMapping(userMapping);
                    rowsReturned++;
                } catch (SQLException throwables) {
                    LOGGER.error("Failed to completely extract result from the above select all query. ", throwables);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute / process sql '{}'. ", sql, e);
        }
        LOGGER.info("Received {} entries for sql => '{}'", rowsReturned, sql);
        return titleMappingsBuilder.build();
    }

    @Override
    public TitleMappingProto.TitleMappingList retrieveSelective() {
        return null;
    }

    @Override
    public TitleMappingProto.TitleMapping generateDetail(ResultSet resultSet) {
        TitleMappingProto.TitleMapping.Builder builder = TitleMappingProto.TitleMapping.newBuilder();
        try {
            builder.setMobile(resultSet.getLong(COL_MOBILE));
            builder.setDate(resultSet.getInt(COL_DATE));
            builder.setTitle(resultSet.getString(COL_TITLE));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve user-mapping detail from DB. ", throwables);
        }
        return builder.build();
    }

}
