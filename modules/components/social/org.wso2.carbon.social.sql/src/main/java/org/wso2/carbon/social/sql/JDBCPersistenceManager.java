
package org.wso2.carbon.social.sql;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.social.sql.Constants;

/**
 * This class is used for creating SOACIAl database. During
 * the server start-up, it checks whether the user has provided -Dsetup argument and if so it creates the db.
 * This is implemented as a singleton. An instance of this class can be obtained through
 * JDBCPersistenceManager.getInstance() method.
 */
public class JDBCPersistenceManager {

	private static Log log = LogFactory.getLog(JDBCPersistenceManager.class);
	private static JDBCPersistenceManager instance;
	private static DataSourceService carbonDataSourceService;


	private JDBCPersistenceManager() {

	}

	public static DataSourceService getCarbonDataSourceService() {
		return carbonDataSourceService;
	}

	public static void setCarbonDataSourceService(DataSourceService dataSourceService) {
		carbonDataSourceService = dataSourceService;
	}

	public static JDBCPersistenceManager getInstance() throws Exception {
		if (instance == null) {
			synchronized (JDBCPersistenceManager.class) {
				if (instance == null) {
					instance = new JDBCPersistenceManager();
				}
			}
		}
		return instance;
	}

	public void initializeDatabase() throws Exception {

		CarbonDataSource cds = carbonDataSourceService.getDataSource(Constants.SOCIAL_DB_NAME);
		DataSource dataSource = (DataSource) cds.getDSObject();

		SocialDBInitilizer dbInitializer = new SocialDBInitilizer(dataSource);

		try {
			dbInitializer.createSocialDatabase();
		} catch (Exception e) {
			log.error("Error when creating the SOCIAL database" + e);
		}
	}
}

