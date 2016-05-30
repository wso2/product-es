/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.es.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.es.migration.util.Constants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.StringTokenizer;
import javax.sql.DataSource;

/**
 * This class initilzes the datsources and contains methods which executes the migration scripts on registry database
 * and um tables.
 */
public class MigrationDatabaseCreator {

    private static Log log = LogFactory.getLog(MigrationDatabaseCreator.class);
    private Connection conn = null;
    private Statement statement;
    private DataSource dataSource;
    private DataSource umDataSource;
    private String delimiter = ";";
    private String dataBaseType;

    public MigrationDatabaseCreator(DataSource dataSource, DataSource umDataSource) {
        this.dataSource = dataSource;
        this.umDataSource = umDataSource;
    }

    public MigrationDatabaseCreator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataBaseType() {
        return dataBaseType;
    }

    public void setDataBaseType(String dataBaseType) {
        this.dataBaseType = dataBaseType;
    }
    /**
     * Execute Migration Script
     *
     * @throws Exception
     */
    public void executeRegistryMigrationScript() throws SQLException, IOException {

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            String dbscriptName = Constants.REGISTRY_MIGRATION_SCRIPT;
            executeSQLScript(dbscriptName);
            conn.commit();
            if (log.isTraceEnabled()) {
                log.trace("Migration script executed successfully.");
            }
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Failed to close database connection.", e);
            }
        }
    }

    public void executeUmMigrationScript() throws Exception {

        try {
            conn = umDataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            String dbscriptName = Constants.UM_MIGRATION_SCRIPT;
            executeSQLScript(dbscriptName);
            conn.commit();
            if (log.isTraceEnabled()) {
                log.trace("Migration script executed successfully.");
            }
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Failed to close database connection.", e);
            }
        }
    }





    /**
     * executes content in SQL script
     *
     * @throws Exception
     */
    private void executeSQLScript(String dbscriptName) throws IOException, SQLException {

        boolean keepFormat = false;
        StringBuffer sql = new StringBuffer();
        BufferedReader reader = null;
        try {
            InputStream is = getClass().getResourceAsStream(dbscriptName);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!keepFormat) {
                    if (line.startsWith("//")) {
                        continue;
                    }
                    if (line.startsWith("--")) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(line);
                    if (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if ("REM".equalsIgnoreCase(token)) {
                            continue;
                        }
                    }
                }
                sql.append(keepFormat ? "\n" : " ").append(line);
                if (!keepFormat && line.indexOf("--") >= 0) {
                    sql.append("\n");
                }
                if ((DatabaseCreator.checkStringBufferEndsWith(sql, delimiter))) {
                    executeSQL(sql.substring(0, sql.length() - delimiter.length()));
                    sql.replace(0, sql.length(), "");
                }
            }
            if (sql.length() > 0) {
                executeSQL(sql.toString());
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * executes given sql
     *
     * @param sql
     * @throws Exception
     */
    private void executeSQL(String sql) throws SQLException {

        // Check and ignore empty statements
        if ("".equals(sql.trim())) {
            return;
        }

        ResultSet resultSet = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("SQL : " + sql);
            }

            boolean ret;
            int updateCount = 0, updateCountTotal = 0;
            ret = statement.execute(sql);
            updateCount = statement.getUpdateCount();
            resultSet = statement.getResultSet();
            do {
                if (!ret && updateCount != -1) {
                    updateCountTotal += updateCount;
                }
                ret = statement.getMoreResults();
                if (ret) {
                    updateCount = statement.getUpdateCount();
                    resultSet = statement.getResultSet();
                }
            } while (ret);

            if (log.isDebugEnabled()) {
                log.debug(sql + " : " + updateCountTotal + " rows affected");
            }
            SQLWarning warning = conn.getWarnings();
            while (warning != null) {
                log.debug(warning + " sql warning");
                warning = warning.getNextWarning();
            }
            conn.clearWarnings();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error occurred while closing result set.", e);
                }
            }
        }
    }
}
