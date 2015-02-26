/*
* Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.social.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.social.sql.Constants;


public class DSConnection {
	private static Log log = LogFactory.getLog(DSConnection.class);
	
	public Connection getConnection() {
    	Connection conn = null;
            try {
                CarbonDataSource carbonDataSource = DataSourceManager.getInstance().getDataSourceRepository().getDataSource(Constants.SOCIAL_DB_NAME);
                DataSource dataSource = (DataSource) carbonDataSource.getDSObject();
                conn = dataSource.getConnection();
            } catch (SQLException e) {
            	log.error("Can't create JDBC connection to the SQL Server", e);
            } catch (DataSourceException e) {
            	log.error("Can't create data source for SQL Server", e);
            }
        return conn;
    }
    
    public void closeConnection(Connection connection){
    	try {
			connection.close();
		} catch (SQLException e) {
			log.error("Can't close JDBC connection to the SQL server", e);
		}  	
    }

}
