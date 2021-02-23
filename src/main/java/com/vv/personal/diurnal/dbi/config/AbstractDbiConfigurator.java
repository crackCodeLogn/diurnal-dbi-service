package com.vv.personal.diurnal.dbi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public Connection getDbConnection() {
        if (connection == null) {
            String dbUrl = String.format(DB_CONNECTORS_URL, getDbServerHost(), getDbServerPort(), getDbName());
            LOGGER.info("Establishing DB connection to: {}", dbUrl);
            try {
                Connection connection = DriverManager.getConnection(dbUrl, getProperties());
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
    public Statement getStatement() {
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

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty(DB_USER_STRING, getDbUser());
        properties.setProperty(DB_CRED_STRING, getDbCred());
        return properties;
    }
}
