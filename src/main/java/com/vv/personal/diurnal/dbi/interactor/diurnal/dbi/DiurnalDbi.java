package com.vv.personal.diurnal.dbi.interactor.diurnal.dbi;

import com.vv.personal.diurnal.dbi.config.DbiConfigForDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.cache.CachedDiurnal;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import com.vv.personal.diurnal.dbi.util.TimingUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Function;

import static com.vv.personal.diurnal.dbi.constants.Constants.EMPTY_STR;
import static com.vv.personal.diurnal.dbi.constants.Constants.PIPE;
import static com.vv.personal.diurnal.dbi.constants.DbConstants.*;


/**
 * @author Vivek
 * @since 23/02/21
 */
public abstract class DiurnalDbi<T, K> implements IDiurnalDbi<T, K> {
    protected final String TABLE;
    protected final String PRIMARY_COLUMNS;
    protected final CachedDiurnal CACHED_DIURNAL;
    private final Logger LOGGER;
    private final DbiConfigForDiurnal dbiConfigForDiurnal;
    private final Function<String, String> createTableIfNotExistSqlFunction;
    private final String createTableIfNotExistSqlLocation;
    private final ExecutorService singleWriterThread = Executors.newSingleThreadExecutor();
    private final ExecutorService multiReadThreads = Executors.newFixedThreadPool(4);
    protected String csvDumpLocationFolder = DiurnalUtil.getDefaultCsvDumpLocation();
    protected String csvLineSeparator = PIPE;

    public DiurnalDbi(String table, String primaryColumns, DbiConfigForDiurnal dbiConfigForDiurnal, CachedDiurnal CACHED_DIURNAL,
                      Function<String, String> createTableIfNotExistSqlFunction, String createTableIfNotExistSqlLocation, Logger logger) {
        this.TABLE = table;
        this.PRIMARY_COLUMNS = primaryColumns;
        this.dbiConfigForDiurnal = dbiConfigForDiurnal;
        this.CACHED_DIURNAL = CACHED_DIURNAL;
        this.createTableIfNotExistSqlFunction = createTableIfNotExistSqlFunction;
        this.createTableIfNotExistSqlLocation = createTableIfNotExistSqlLocation;
        this.LOGGER = logger;

        LOGGER.info("Created handler for '{}'", TABLE);
    }

    @Override
    public ResultSet executeNonUpdateSql(String sql) {
        //LOGGER.info("Executing SQL => {}", sql);
        Callable<ResultSet> nonUpdateSqlTask = () -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            try {
                ResultSet sqlResult = dbiConfigForDiurnal.getStatement().executeQuery(sql);
                LOGGER.info("SQL completed => [{}]", sql);
                return sqlResult;
            } catch (SQLException throwables) {
                LOGGER.error("Failed to execute SQL => [{}]. ", sql, throwables);
            } finally {
                stopWatch.stop();
                LOGGER.info("Non-update SQL execution completed in {}ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
            }
            return null;
        };
        try {
            return multiReadThreads.submit(nonUpdateSqlTask).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Interrupted / Gone bad while executing sql -> '{}'/ ", sql, e);
        }
        return null;
    }

    @Override
    public int executeUpdateSql(String sql) {
        Callable<Integer> updateSqlTask = () -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            try {
                int sqlResult = dbiConfigForDiurnal.getStatement().executeUpdate(sql);
                LOGGER.info("Result of SQL [{}] => {}", sql, sqlResult);
                return sqlResult;
            } catch (SQLException throwables) {
                LOGGER.error("Failed to execute SQL => [{}]. ", sql, throwables);
            } finally {
                stopWatch.stop();
                LOGGER.info("Update SQL execution completed in {}ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
            }
            return -1;
        };
        try {
            return singleWriterThread.submit(updateSqlTask).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Interrupted / Gone bad while executing sql -> '{}'. ", sql, e);
        }
        return -1;
    }

    @Override
    public Collection<Integer> selectAllIdsForTable(String table, String column) {
        List<Integer> ids = new ArrayList<>();
        int rowsReturned = 0;
        String sql = String.format(SELECT_ALL_IDS, column, table);
        try {
            ResultSet resultSet = executeNonUpdateSql(sql);
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    ids.add(resultSet.getInt(1));
                    rowsReturned++;
                } catch (SQLException throwables) {
                    LOGGER.error("Failed to completely extract result from the above select all query. ", throwables);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute / process sql '{}'. ", sql, e);
        }
        LOGGER.info("Received {} entries of select All for '{}' of table '{}'", rowsReturned, column, table);
        return ids;
    }

    @Override
    public int createTableIfNotExists() {
        return executeUpdateSql(
                createTableIfNotExistSqlFunction.apply(createTableIfNotExistSqlLocation));
    }

    @Override
    public int dropTable() {
        return executeUpdateSql(String.format(DROP_TABLE, TABLE));
    }

    @Override
    public int truncateTable() {
        return executeUpdateSql(String.format(TRUNCATE_TABLE, TABLE));
    }

    protected boolean checkIfEntityExists(String sql, int expectedCount) {
        ResultSet resultSet = executeNonUpdateSql(sql);
        int rowsReturned = 0;
        try {
            while (resultSet.next()) {
                rowsReturned++;
            }
        } catch (SQLException throwables) {
            LOGGER.error("Failed to completely extract result from the above select all query. ", throwables);
        }
        return rowsReturned == expectedCount;
    }

    @Override
    public void populatePrimaryIds() {
        getCachedRef().bulkAddNewIdsToEntityCache(TABLE, selectAllIdsForTable());
    }

    public Collection<Integer> selectAllIdsForTable() {
        return selectAllIdsForTable(TABLE, PRIMARY_COLUMNS);
    }

    public CachedDiurnal getCachedRef() {
        return CACHED_DIURNAL;
    }

    @Override
    public String getTableName() {
        return TABLE;
    }

    protected abstract Queue<String> processDataToCsv(K dataList);

    @Override
    public String dumpTableToCsv() {
        K dataList = retrieveAll();
        String csv = String.format("%s/%s-%d.csv", csvDumpLocationFolder, TABLE, TimingUtil.extractCurrentUtcTimestamp());
        File csvDump = new File(csv);
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(csvDump))) {
            Queue<String> dataLines = processDataToCsv(dataList);
            while (!dataLines.isEmpty())
                printWriter.println(dataLines.poll());
            printWriter.flush();
        } catch (IOException e) {
            LOGGER.error("Failed to write table dump to csv. ", e);
            return EMPTY_STR;
        }
        return csvDump.getAbsolutePath();
    }

    public void destroyExecutors() {
        LOGGER.info("Shutting down singleWriterThread executor");
        if (!singleWriterThread.isShutdown())
            singleWriterThread.shutdown();
        LOGGER.info("Shutting down multiReadThreads executor");
        if (!multiReadThreads.isShutdown())
            multiReadThreads.shutdown();
    }

    public DiurnalDbi<T, K> setCsvDumpLocationFolder(String csvDumpLocationFolder) {
        this.csvDumpLocationFolder = csvDumpLocationFolder;
        return this;
    }

    public DiurnalDbi<T, K> setCsvLineSeparator(String csvLineSeparator) {
        this.csvLineSeparator = csvLineSeparator;
        return this;
    }
}
