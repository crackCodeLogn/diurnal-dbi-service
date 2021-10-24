package com.vv.personal.diurnal.dbi.interactor;

import java.sql.ResultSet;

/**
 * @author Vivek
 * @since 02/01/21
 */
public interface IDbi {

    int createTableIfNotExists();

    ResultSet executeNonUpdateSql(String sql);

    int executeUpdateSql(String sql);

    int dropTable();

    int truncateTable();

    String getTableName();
}