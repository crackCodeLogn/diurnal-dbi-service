package com.vv.personal.diurnal.dbi.interactor.diurnal;

import com.vv.personal.diurnal.dbi.interactor.IDbi;

import java.sql.ResultSet;
import java.util.Collection;

/**
 * @author Vivek
 * @since 23/02/21
 */
public interface IDiurnalDbi<T, K> extends IDbi {

    int pushNewEntity(T t);

    int deleteEntity(T t);

    int updateEntity(T t);

    boolean checkEntity(T t);

    K retrieveAll();

    K retrieveSelective(); //TODO -- work on this later, not imp atm.

    T generateDetail(ResultSet resultSet);

    Collection<Integer> selectAllIdsForTable(String table, String column);

    void populatePrimaryIds();

    /*
    void flushCache();

    default int addToCacheOnSqlResult(Integer sqlResult, String table, Integer id) {
        if (sqlResult == 1) addToCache(table, id);
        return sqlResult;
    }

    default int removeFromCacheOnSqlResult(Integer sqlResult, String table, Integer id) {
        if (sqlResult == 1) removeFromCache(table, id);
        return sqlResult;
    }

    void addToCache(String table, Integer id);

    void removeFromCache(String table, Integer id);*/
}
