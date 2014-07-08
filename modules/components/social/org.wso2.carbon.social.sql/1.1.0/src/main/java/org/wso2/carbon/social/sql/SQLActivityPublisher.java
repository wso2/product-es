package org.wso2.carbon.social.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.social.core.ActivityPublisher;
import org.wso2.carbon.social.core.JSONUtil;

public class SQLActivityPublisher extends ActivityPublisher {
	
	private static Log log = LogFactory.getLog(SQLActivityPublisher.class);
	public static final String INSERT_SQL = "INSERT INTO ES_SOCIAL(id, contextId, body) VALUES(?, ?, ?)";
    @Override
    protected String publish(String id, NativeObject activity) {
    	DSConnection con = new DSConnection();
    	Connection connection = con.getConnection();
    	if(connection != null){
    		PreparedStatement statement = null;
            try{
            String json = JSONUtil.SimpleNativeObjectToJson(activity);
            String contextId = JSONUtil.getNullableProperty(activity, Constants.CONTEXT_JSON_PROP, Constants.ID_JSON_PROP);

            statement = connection.prepareStatement(INSERT_SQL);
            statement.setString(1, id);
            statement.setString(2, contextId);
            statement.setString(3, json);
            statement.executeQuery();
            }catch(Exception e){
            	log.error("failed to publish social event.", e);
            }finally{
            	con.closeConnection(connection);
            }
    	}
    	return null;
    }
       
    
}
