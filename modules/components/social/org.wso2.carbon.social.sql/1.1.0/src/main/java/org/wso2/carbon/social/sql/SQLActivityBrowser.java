package org.wso2.carbon.social.sql;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.SortOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQLActivityBrowser implements ActivityBrowser {
    private static final Log log = LogFactory.getLog(SQLActivityBrowser.class);
    public static final String SELECT_SQL = "SELECT * FROM ES_SOCIAL WHERE " +
            Constants.CONTEXT_ID_COLUMN + "=?";
    private JsonParser parser = new JsonParser();
    
    @Override
    public double getRating(String targetId, String tenant) {
        return 0;
    }

	@Override
    public JsonObject getSocialObject(String targetId, String tenant, SortOrder order) {
        List<Activity> activities = listActivitiesChronologically(targetId, tenant);
        //JsonArray attachments = new JsonArray();
        JsonArray attachments = new JsonArray();
        JsonObject jsonObj = new JsonObject();
        jsonObj.add("attachments", attachments);
        
        for (Activity activity : activities) {
        	JsonObject body = activity.getBody();
        	//jsonObj.add("attachments", body);
        	attachments.add(body);
        }
        
        /*
        JsonElement attachmentsElement = to.get("attachments");
        JsonArray attachments;
        if (attachmentsElement == null) {
            attachments = new JsonArray();
            to.add("attachments", attachments);
        } else {
            attachments = attachmentsElement.getAsJsonArray();
        }
        attachments.add(activity.getBody());
         * 
         * */
        
        
		/*Gson gson = new Gson();
		//{"rating" : 4, "attachments" : [{"verb" : "post", "object" : {"objectType" : "review", "content" : "test12333", "rating" : 4}, "target" : {"id" : "gadget:c5da3589-8131-4201-85af-9d1c17383f70"}, "actor" : {"id" : "admin@carbon.super", "objectType" : "person"}, "id" : "2939ed64-8bf0-4896-977a-728290ccae4e"}]} 
    	String jsonStr = gson.toJson(activities);
    	JsonObject jsonObj = (JsonObject)  parser.parse(jsonStr);
    	//JsonElement jsonElement = parser.parse(jsonStr);
    	JsonElement jm = jsonObj.getAsJsonObject().get("body");
    	//JsonObject jsonObject = (JsonObject) parser.parse(jsonStr).getAsJsonObject();*/

        return jsonObj;
    }

    @Override
    public List<Activity> listActivities(String contextId, String tenantDomain) {
    	List<Activity> activities = null;
    	DSConnection con = new DSConnection();
    	Connection connection = con.getConnection();
    	if(connection != null){
    		PreparedStatement statement = null;
    		ResultSet resultSet = null;
            try{
            statement = connection.prepareStatement(SELECT_SQL);
            statement.setString(1, contextId);
            resultSet = statement.executeQuery();
            activities = new ArrayList<Activity>();
            while (resultSet.next()) {
                JsonObject body = (JsonObject) parser.parse(resultSet.getString(Constants.BODY_COLUMN));
                String tenant = getTenant(body);
                if (tenantDomain.equals(tenant)) {
                    Activity activity = new SQLActivity(body.getAsJsonObject());
                    activities.add(activity);
                }
            }
            }catch(Exception e){
            	log.error("Can't retrieve activities from SQL.", e);
            }finally{
            	con.closeConnection(connection);
            }
    	}
        if (activities != null) {
            return activities;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Activity> listActivitiesChronologically(String contextId, String tenantDomain) {
        List<Activity> activities = listActivities(contextId, tenantDomain);
       /* Collections.sort(activities, new Comparator<Activity>() {
            @Override
            public int compare(Activity a1, Activity a2) {
                return a1.getTimestamp() - a2.getTimestamp();
            }
        });*/
        return activities;
    }
    
    private String getTenant(JsonObject body) {
        JsonObject actor = body.getAsJsonObject("actor");
        if (actor != null) {
            String id = actor.get("id").getAsString();
            int j = id.lastIndexOf('@') + 1;
            if (j > 0) {
                return id.substring(j);
            }
        }
        return null;
    }
}
