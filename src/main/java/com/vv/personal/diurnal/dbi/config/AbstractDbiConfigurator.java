package com.vv.personal.diurnal.dbi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;

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
            Properties properties;
            String dbUrl;
            if (getDbUrl().isEmpty()) {
                dbUrl = String.format(DB_CONNECTORS_URL, getDbServerHost(), getDbServerPort(), getDbName());
                properties = getProperties(getDbUser(), getDbCred());
            } else {
                try {
                    URI dbUri = new URI(getDbUrl());

                    String user = dbUri.getUserInfo().split(":")[0];
                    String cred = dbUri.getUserInfo().split(":")[1];
                    dbUrl = String.format(DB_CONNECTORS_URL, dbUri.getHost(), dbUri.getPort(), dbUri.getPath());
                    properties = getProperties(user, cred);
                } catch (URISyntaxException e) {
                    LOGGER.error("Failed to parse URL: {}. ", getDbUrl());
                    throw e;
                }
            }
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
        properties.setProperty(DB_USER_STRING, user);
        properties.setProperty(DB_CRED_STRING, cred);
        return properties;
    }
}
