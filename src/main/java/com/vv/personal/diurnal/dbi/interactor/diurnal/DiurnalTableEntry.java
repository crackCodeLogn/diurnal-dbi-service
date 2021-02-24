package com.vv.personal.diurnal.dbi.interactor.diurnal;

import com.vv.personal.diurnal.artifactory.generated.EntryProto;
import com.vv.personal.diurnal.dbi.config.DbiConfigForDiurnal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import static com.vv.personal.diurnal.dbi.constants.Constants.SELECT_ALL;

/**
 * @author Vivek
 * @since 24/02/21
 */
public class DiurnalTableEntry extends DiurnalDbi<EntryProto.Entry, EntryProto.EntryList> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiurnalTableEntry.class);

    private final String INSERT_STMT_NEW_ENTRY = "INSERT INTO %s(\"mobile\", \"date\", \"serial\",\"sign\",\"curr\",\"amount\",\"description\") " +
            "VALUES(%d, %d, %d, %d, %d, '%.2f', '%s')";
    private final String DELETE_STMT_ENTRY = "DELETE FROM %s " +
            "WHERE \"%s\"=%d and \"%s\"=%d and \"%s\"=%d";

    private final String COL_DATE = "date";
    private final String COL_MOBILE = "mobile";
    private final String COL_SERIAL = "serial";
    private final String COL_SIGN = "sign";
    private final String COL_CURR = "curr";
    private final String COL_AMT = "amount";
    private final String COL_DESCRIPTION = "description";

    public DiurnalTableEntry(String table, String primaryColumns, DbiConfigForDiurnal dbiConfigForDiurnal, CachedDiurnal cachedDiurnal, Function<String, String> createTableIfNotExistSqlFunction, String createTableIfNotExistSqlLocation) {
        super(table, primaryColumns, dbiConfigForDiurnal, cachedDiurnal, createTableIfNotExistSqlFunction, createTableIfNotExistSqlLocation, LOGGER);
    }

    @Override
    public int pushNewEntity(EntryProto.Entry entry) {
        LOGGER.info("Pushing new Entry entity: {} x {} x {}", entry.getMobile(), entry.getDate(), entry.getSerial());
        return insertNewEntry(entry.getMobile(), entry.getDate(), entry.getSerial(),
                entry.getSign(), entry.getCurrency(), entry.getAmount(), entry.getDescription());
    }

    private int insertNewEntry(Long mobile, Integer date, Integer serial,
                               EntryProto.Sign sign, EntryProto.Currency currency, Double amount, String description) {
        String sql = String.format(INSERT_STMT_NEW_ENTRY, TABLE,
                mobile, date, serial, sign.getNumber(), currency.getNumber(), amount, description);
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
        //return addToCacheOnSqlResult(sqlExecResult, mobile);
    }

    @Override
    public int deleteEntity(EntryProto.Entry entry) {
        String sql = String.format(DELETE_STMT_ENTRY, TABLE,
                COL_MOBILE, entry.getMobile(),
                COL_DATE, entry.getDate(),
                COL_SERIAL, entry.getSerial());
        int sqlExecResult = executeUpdateSql(sql);
        return sqlExecResult;
        //return removeFromCacheOnSqlResult(sqlExecResult, userMapping.getMobile());
    }

    @Override
    public int updateEntity(EntryProto.Entry entry) {
        throw new UnsupportedOperationException("Entries can't be updated. They need to be deleted and re-created for DB!");
    }

    @Override
    public EntryProto.EntryList retrieveAll() {
        String sql = String.format(SELECT_ALL, TABLE);
        ResultSet resultSet = executeNonUpdateSql(sql);
        int rowsReturned = 0;
        EntryProto.EntryList.Builder entriesBuilder = EntryProto.EntryList.newBuilder();
        try {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    EntryProto.Entry entry = generateDetail(resultSet);
                    entriesBuilder.addEntries(entry);
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
    public EntryProto.EntryList retrieveSelective() {
        return null;
    }

    @Override
    public EntryProto.Entry generateDetail(ResultSet resultSet) {
        EntryProto.Entry.Builder builder = EntryProto.Entry.newBuilder();
        try {
            builder.setMobile(resultSet.getLong(COL_MOBILE));
            builder.setDate(resultSet.getInt(COL_DATE));
            builder.setSerial(resultSet.getInt(COL_SERIAL));
            builder.setSign(EntryProto.Sign.forNumber(resultSet.getInt(COL_SIGN)));
            builder.setCurrency(EntryProto.Currency.forNumber(resultSet.getInt(COL_CURR)));
            builder.setAmount(resultSet.getDouble(COL_AMT));
            builder.setDescription(resultSet.getString(COL_DESCRIPTION));
        } catch (SQLException throwables) {
            LOGGER.error("Failed to retrieve entry detail from DB. ", throwables);
        }
        return builder.build();
    }

}
