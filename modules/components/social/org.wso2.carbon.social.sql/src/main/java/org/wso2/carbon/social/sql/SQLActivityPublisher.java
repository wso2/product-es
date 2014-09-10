package org.wso2.carbon.social.sql;

import java.sql.PreparedStatement;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.social.sql.Constants;
import org.wso2.carbon.social.core.ActivityPublisher;
import org.wso2.carbon.social.core.JSONUtil;

public class SQLActivityPublisher extends ActivityPublisher {
	
	private static Log log = LogFactory.getLog(SQLActivityPublisher.class);
	public static final String INSERT_SQL = "INSERT INTO " + Constants.ES_SOCIAL + "(id, payload_context_id, body) VALUES(?, ?, ?)";
    @Override
    protected String publish(String id, NativeObject activity) {
    	DSConnection con = new DSConnection();
    	Connection connection = con.getConnection();
    	if(connection != null){
    		PreparedStatement statement = null;
            try{
	            String json = JSONUtil.SimpleNativeObjectToJson(activity);
	            String contextId = JSONUtil.getNullableProperty(activity, Constants.CONTEXT_JSON_PROP, Constants.ID_JSON_PROP);
	            if (contextId == null) {
	                contextId = JSONUtil.getProperty(activity, Constants.TARGET_JSON_PROP, Constants.ID_JSON_PROP);
	            }
	            connection.setAutoCommit(false);
	            statement = connection.prepareStatement(INSERT_SQL);
	            statement.setString(1, id);
	            statement.setString(2, contextId);
	            statement.setString(3, json);
	            int ret = statement.executeUpdate();
	            connection.commit();
	            if(ret > 0){
	            	return id;
	            }else{
	            	log.info("failed to publish social event.");
	            }
	            }catch(Exception e){
            	log.error("failed to publish social event.", e);
            }finally{
            	con.closeConnection(connection);
            }
    	}
    	return null;
    }
       
    
}
