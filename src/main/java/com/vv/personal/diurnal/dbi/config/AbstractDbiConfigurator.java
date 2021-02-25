package com.vv.personal.diurnal.dbi.config;

import com.vv.personal.diurnal.dbi.constants.DbConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static com.vv.personal.diurnal.dbi.constants.Constants.COLON_STR;

/**
 * @author Vivek
 * @since 23/02/21
 */
public abstract class AbstractDbiConfigurator implements DbiConfigurator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDbiConfigurator.class);

    protected Connection connection = null;
    protected Statement statement = null;

    @Override
    public Connection getDbConnection() throws URISyntaxException {
        if (connection == null) {
            String dbHost, dbName, user, cred;
            Integer dbPort;
            if (getDbUrl().isEmpty()) {
                dbHost = getDbServerHost();
                dbPort = getDbServerPort();
                dbName = getDbName();
                user = getDbUser();
                cred = getDbCred();
            } else {
                try {
                    String dbUrl = getDbUrl();  //procurement from Heroku - dynamic nature
                    LOGGER.info("Procured DB-URL: {}", dbUrl);
                    URI dbUri = new URI(dbUrl);
                    dbHost = dbUri.getHost();
                    dbPort = dbUri.getPort();
                    dbName = dbUri.getPath().substring(1);
                    String[] userInfo = StringUtils.split(dbUri.getUserInfo(), COLON_STR);
                    user = userInfo[0];
                    cred = userInfo[1];
                } catch (URISyntaxException e) {
                    LOGGER.error("Failed to parse URL: {}. ", getDbUrl());
                    throw e;
                }
            }
            String dbUrl = String.format(DbConstants.DB_CONNECTORS_URL, dbHost, dbPort, dbName);
            Properties properties = getProperties(user, cred);
            LOGGER.info("Establishing DB connection to: {}", dbUrl);
            try {
                Connection connection = DriverManager.getConnection(dbUrl, properties);
                LOGGER.info("DB connection successful => {}", connection.getClientInfo());
                this.connection = connection;
                return connection;
            } catch (SQLException throwables) {
                LOGGER.error("Failed to establish DB connection. ", throwables);
            }
        }
        return connection;
    }

    @Override
    public Statement getStatement() throws URISyntaxException {
        if (statement == null) {
            connection = getDbConnection();
            try {
                this.statement = connection.createStatement();
                return statement;
            } catch (SQLException throwables) {
                LOGGER.error("Failed to create statement for {}. ", getDbName(), throwables);
            }
        }
        return statement;
    }

    @Override
    public boolean closeDbConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Successfully closed DB connection for '{}'", getDbName());
                return true;
            } catch (SQLException throwables) {
                LOGGER.error("Failed to close DB connection / already closed for '{}'. ", getDbName(), throwables);
            }
            return false;
        }
        return true;
    }

    private Properties getProperties(String user, String cred) {
        Properties properties = new Properties();
        properties.setProperty(DbConstants.DB_USER_STRING, user);
        properties.setProperty(DbConstants.DB_CRED_STRING, cred);
        return properties;
    }
}
