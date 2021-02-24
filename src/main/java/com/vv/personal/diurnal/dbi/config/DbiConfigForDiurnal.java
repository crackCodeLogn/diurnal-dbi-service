package com.vv.personal.diurnal.dbi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vivek
 * @since 23/02/21
 */
@Configuration
public class DbiConfigForDiurnal extends AbstractDbiConfigurator {

    @Value("${db.diurnal.server.host:localhost}")
    private String dbServerHost;

    @Value("${db.diurnal.server.port:5432}")
    private int dbServerPort;

    @Value("${db.diurnal.name:diurnal}")
    private String dbName;

    @Value("${db.diurnal.user:}")
    private String dbUser;

    @Value("${db.diurnal.cred:}")
    private String dbCred;

    @Value("${db.diurnal.url:}")
    private String dbUrl;

    @Override
    public String getDbServerHost() {
        return dbServerHost;
    }

    @Override
    public Integer getDbServerPort() {
        return dbServerPort;
    }

    @Override
    public String getDbName() {
        return dbName;
    }

    @Override
    public String getDbUser() {
        return dbUser;
    }

    @Override
    public String getDbCred() {
        return dbCred;
    }

    @Override
    public String getDbUrl() {
        return dbUrl;
    }

}
