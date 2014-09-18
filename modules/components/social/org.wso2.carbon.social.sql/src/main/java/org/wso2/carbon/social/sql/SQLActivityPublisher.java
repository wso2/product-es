package org.wso2.carbon.social.sql;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.social.core.ActivityPublisher;
import org.wso2.carbon.social.core.JSONUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SQLActivityPublisher extends ActivityPublisher {

	private static Log log = LogFactory.getLog(SQLActivityPublisher.class);

    private JsonParser parser = new JsonParser();

	@Override
	protected String publish(String id, NativeObject activity) {
		DSConnection con = new DSConnection();
		Connection connection = con.getConnection();
		if(connection != null){
			PreparedStatement statement = null;
			try{
				String json = JSONUtil.SimpleNativeObjectToJson(activity);
				String contextId = JSONUtil.getNullableProperty(activity, Constants.CONTEXT_JSON_PROP, Constants.ID_JSON_PROP);
				String userId = JSONUtil.getNullableProperty(activity, Constants.ACTOR_JSON_PROP, Constants.ID_JSON_PROP);
				String rating = JSONUtil.getNullableProperty(activity, Constants.OBJECT_JSON_PROP, Constants.RATING_JSON_PROP);
				if (contextId == null) {
					contextId = JSONUtil.getProperty(activity, Constants.TARGET_JSON_PROP, Constants.ID_JSON_PROP);
				}
				if (userId == null) {
					userId = JSONUtil.getProperty(activity, Constants.ACTOR_JSON_PROP, Constants.ID_JSON_PROP);
				}
				if (rating == null) {
					rating = JSONUtil.getProperty(activity, Constants.OBJECT_JSON_PROP, Constants.RATING_JSON_PROP);
				}

				String votesStr = "{" +
						"\"upVotes\" : 0, " +
								"\"downVotes\"  : 0 " +
								"}";

								connection.setAutoCommit(false);
								statement = connection.prepareStatement(Constants.INSERT_COMMENT_SQL);
								statement.setString(1, id);
								statement.setString(2, contextId);
								statement.setString(3, json);
								statement.setString(4, userId);
								statement.setString(5, votesStr);
								statement.setInt(6, Integer.parseInt(rating));
								statement.setTimestamp(7,getCurrentTimeStamp());

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

	@Override
	public boolean vote(String commentId, String actorId, int vote) {
		DSConnection con = new DSConnection();
		Connection connection = con.getConnection();
		if(connection != null){
			PreparedStatement statement = null;
			PreparedStatement update = null;
			ResultSet resultSet = null;
			try{
				statement = connection.prepareStatement(Constants.SELECT_VOTE_SQL);
				statement.setString(1, commentId);
				resultSet = statement.executeQuery();
				resultSet.next();

				JsonObject votes = (JsonObject) parser.parse(resultSet.getString(Constants.VOTE_COLUMN));

				//up-vote scenario
				if(vote == 1){
					JsonElement upVotes = votes.get("upVotes");
					int up = upVotes.getAsInt();
					up = up + 1;
					votes.addProperty("upVotes", up);
				}else{//down-vote scenario
					JsonElement downVotes = votes.get("downVotes");
					int down = downVotes.getAsInt();
					down = down + 1;
					votes.addProperty("downVotes", down);
				}
				String votesStr = votes.toString();

				update = connection.prepareStatement(Constants.UPDATE_VOTE_SQL);
				update.setString(1, votesStr);
				update.setString(2, commentId);
				update.executeUpdate();

			}catch(Exception e){
				log.error("Can't retrieve activities from SQL.", e);
			}finally{
				con.closeConnection(connection);
			}
		}
		return true;
	}

	private static Timestamp getCurrentTimeStamp() {
		Date today = new Date();
		return new Timestamp(today.getTime());
	}


}
