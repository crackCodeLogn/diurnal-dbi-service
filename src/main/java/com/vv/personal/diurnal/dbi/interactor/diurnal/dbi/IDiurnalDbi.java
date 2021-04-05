package com.vv.personal.diurnal.dbi.interactor.diurnal.dbi;

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

    T retrieveSelective(T t);

    T generateDetail(ResultSet resultSet);

    Collection<Integer> selectAllIdsForTable(String table, String column);

    void populatePrimaryIds();

    String dumpTableToCsv();
}
