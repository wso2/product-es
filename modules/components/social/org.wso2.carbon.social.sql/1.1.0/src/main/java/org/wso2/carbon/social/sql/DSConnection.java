package org.wso2.carbon.social.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceManager;


public class DSConnection {
	private static Log log = LogFactory.getLog(DSConnection.class);
	
	public Connection getConnection() {
    	Connection conn = null;
            try {
                CarbonDataSource carbonDataSource = DataSourceManager.getInstance().getDataSourceRepository().getDataSource("WSO2_CARBON_DB");
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
