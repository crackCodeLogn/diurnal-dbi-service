package com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables;

import com.vv.personal.diurnal.artifactory.generated.EntryDayProto;
import com.vv.personal.diurnal.dbi.config.DbiConfigForDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.cache.CachedDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.DiurnalDbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import static com.vv.personal.diurnal.dbi.constants.Constants.ONE;
import static com.vv.personal.diurnal.dbi.constants.DbConstants.PRIMARY_COL_ENTRY;
import static com.vv.personal.diurnal.dbi.constants.DbConstants.SELECT_ALL;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.processStringForSqlPush;

/**
 * @author Vivek
 * @since 06/03/21
 */
public class DiurnalTableEntryDay extends DiurnalDbi<EntryDayProto.EntryDay, EntryDayProto.EntryDayList> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiurnalTableEntryDay.class);

    private final String INSERT_STMT_NEW_ENTRY = "INSERT INTO %s(\"mobile\", \"date\", \"entries_as_string\") " +
            "VALUES(%d, %d, '%s')";
    private final String DELETE_STMT_ENTRY = "DELETE FROM %s " +
            "WHERE \"%s\"=%d and \"%s\"=%d";
    private final String CHECK_STMT_ENTRY_EXISTS = "SELECT %s from %s " +
            "WHERE \"%s\"=%d and \"%s\"=%d";

    private final String COL_DATE = "date";
    private final String COL_MOBILE = "mobile";
    private final String COL_ENTRIES_AS_STRING = "entries_as_string";

    public DiurnalTableEntryDay(String table, String primaryColumns, DbiConfigForDiurnal dbiConfigForDiurnal, CachedDiurnal cachedDiurnal, Function<String, String> createTableIfNotExistSqlFunction, String createTableIfNotExistSqlLocation) {
        super(table, primaryColumns, dbiConfigForDiurnal, cachedDiurnal, createTableIfNotExistSqlFunction, createTableIfNotExistSqlLocation, LOGGER);
    }

    private int insertNewEntry(Long mobile, Integer date, String description) {
        String sql = String.format(INSERT_STMT_NEW_ENTRY, TABLE,
                mobile, date, processStringForSqlPush(description));
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
        //return addToCacheOnSqlResult(sqlExecResult, mobile);
    }

    @Override
    public int pushNewEntity(EntryDayProto.EntryDay entryDay) {
        LOGGER.info("Pushing new EntryDay entity: {} x {}", entryDay.getMobile(), entryDay.getDate());
        return insertNewEntry(entryDay.getMobile(), entryDay.getDate(), entryDay.getEntriesAsString());
    }

    @Override
    public int deleteEntity(EntryDayProto.EntryDay entryDay) {
        String sql = String.format(DELETE_STMT_ENTRY, TABLE,
                COL_MOBILE, entryDay.getMobile(),
                COL_DATE, entryDay.getDate());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
        //return removeFromCacheOnSqlResult(sqlExecResult, userMapping.getMobile());
    }

    @Override
    public int updateEntity(EntryDayProto.EntryDay entryDay) {
        throw new UnsupportedOperationException("Entry-Day can't be updated. They need to be deleted and re-created for DB!");
    }

    @Override
    public boolean checkEntity(EntryDayProto.EntryDay entryDay) {
        String sql = String.format(CHECK_STMT_ENTRY_EXISTS, PRIMARY_COL_ENTRY, TABLE,
                COL_MOBILE, entryDay.getMobile(),
                COL_DATE, entryDay.getDate());
        return checkIfEntityExists(sql, ONE);
    }

    @Override
    public EntryDayProto.EntryDayList retrieveAll() {
        String sql = String.format(SELECT_ALL, TABLE);
        ResultSet resultSet = executeNonUpdateSql(sql);
        int rowsReturned = 0;
        EntryDayProto.EntryDayList.Builder entriesBuilder = EntryDayProto.EntryDayList.newBuilder();
        try {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    EntryDayProto.EntryDay entryDay = generateDetail(resultSet);
                    entriesBuilder.addEntryDay(entryDay);
                    rowsReturned++;
                } catch (SQLException throwables) {
                    LOGGER.error("Failed to completely extract result from the above select all query. ", throwables);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute / process sql '{}'. ", sql, e);
        }
        LOGGER.info("Received {} entries for sql => '{}'", rowsReturned, sql);
        return entriesBuilder.build();
    }

    @Override
    public EntryDayProto.EntryDayList retrieveSelective() {
        return null;
    }

    @Override
    public EntryDayProto.EntryDay generateDetail(ResultSet resultSet) {
        EntryDayProto.EntryDay.Builder builder = EntryDayProto.EntryDay.newBuilder();
        try {
            builder.setMobile(resultSet.getLong(COL_MOBILE));
            builder.setDate(resultSet.getInt(COL_DATE));
            builder.setEntriesAsString(resultSet.getString(COL_ENTRIES_AS_STRING));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve entry detail from DB. ", throwables);
        }
        return builder.build();
    }

}
