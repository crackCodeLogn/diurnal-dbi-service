package com.vv.personal.diurnal.dbi.config;

import java.sql.Connection;
import java.sql.Statement;

/**
 * @author Vivek
 * @since 23/02/21
 */
public interface DbiConfigurator {

    String getDbServerHost();

    Integer getDbServerPort();

    String getDbName();

    String getDbUser();

    String getDbCred();

    Connection getDbConnection();

    boolean closeDbConnection();

    Statement getStatement();
}
