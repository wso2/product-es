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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.SocialActivityException;
import org.wso2.carbon.social.sql.Constants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.social.sql.SocialUtil;

/**
 * 
 * SQLActivityBrowser class is responsible for activity retrieving.
 * 
 */

public class SQLActivityBrowser implements ActivityBrowser {
	private static final Log log = LogFactory.getLog(SQLActivityBrowser.class);

	public static final String SELECT_CACHE_SQL = "SELECT "
			+ Constants.RATING_TOTAL + "," + Constants.RATING_COUNT + " FROM "
			+ Constants.SOCIAL_RATING_CACHE_TABLE_NAME + " WHERE "
			+ Constants.CONTEXT_ID_COLUMN + "= ?";

	public static final String TOP_ASSETS_SELECT_SQL = "SELECT "
			+ Constants.RATING_TOTAL + "," + Constants.RATING_COUNT + ","
			+ Constants.CONTEXT_ID_COLUMN + "  FROM "
			+ Constants.SOCIAL_RATING_CACHE_TABLE_NAME;

	public static final String TOP_COMMENTS_SELECT_SQL = "SELECT "
			+ Constants.BODY_COLUMN + " FROM "
			+ Constants.SOCIAL_COMMENTS_TABLE_NAME + "WHERE "
			+ Constants.CONTEXT_ID_COLUMN + " = ? AND "
			+ Constants.LIKES_COLUMN + " > ?";

	public static final String SELECT_LIKE_STATUS = "SELECT "
			+ Constants.ID_COLUMN + " FROM "
			+ Constants.SOCIAL_LIKES_TABLE_NAME + " WHERE "
			+ Constants.USER_COLUMN + " = ? AND " + Constants.CONTEXT_ID_COLUMN
			+ " = ? AND " + Constants.LIKE_VALUE_COLUMN + " = ?";

	public static final String POLL_COMMENTS_SQL = "SELECT "
			+ Constants.BODY_COLUMN + ", " + Constants.ID_COLUMN + " FROM "
			+ Constants.SOCIAL_COMMENTS_TABLE_NAME + " WHERE "
			+ Constants.CONTEXT_ID_COLUMN + " =? AND "
			+ Constants.TENANT_DOMAIN_COLUMN + " =? AND " + Constants.ID_COLUMN
			+ " > ? OREDR BY " + Constants.ID_COLUMN + " DESC";

	public static final String POPULAR_ASSETS_SELECT_SQL = "SELECT PAYLOAD_CONTEXT_ID FROM SOCIAL_RATING_CACHE WHERE PAYLOAD_CONTEXT_ID LIKE ? AND TENANT_DOMAIN =? ORDER BY rating_average DESC LIMIT ?,?";

	private JsonParser parser = new JsonParser();

	public static Object obj = null;
	public static Class<?> cls = null;

	@Override
	/**
	 * Retrieve average rating of a target
	 * 
	 * @param targetId
	 * @return average rating JsonObject 
	 * @throws SocialActivityException 
	 */
	public JsonObject getRating(String targetId) throws SocialActivityException {
		Connection connection = null;
		PreparedStatement statement;
		ResultSet resultSet;
		String errorMsg = "Unable to retrieve rating for target: ";

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + SELECT_CACHE_SQL);
			}
			connection = DSConnection.getConnection();
			statement = connection.prepareStatement(SELECT_CACHE_SQL);

			statement.setString(1, targetId);
			resultSet = statement.executeQuery();

			if (resultSet.next()) {
				int total, count;
				total = resultSet.getInt(Constants.RATING_TOTAL);
				count = resultSet.getInt(Constants.RATING_COUNT);
				resultSet.close();
				if (total != 0) {
					JsonObject object = new JsonObject();
					object.addProperty(Constants.RATING, (double) total / count);
					object.addProperty(Constants.COUNT, count);
					return object;
				}
			}

		} catch (SQLException e) {
			String message = errorMsg + targetId + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (DataSourceException e) {
			String message = errorMsg + targetId + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} finally {
			DSConnection.closeConnection(connection);
		}

		return null;
	}

	@Override
	/**
	 * Retrieve paginated ordered activities of a given target.
	 * 
	 * @param targetId
	 * @param order
	 * @param offset
	 * @param limit
	 * @return activity JsonObject
	 * @throws SocialActivityException 
	 */
	public JsonObject getSocialObject(String targetId, String order,
			int offset, int limit) throws SocialActivityException {

		List<Activity> activities = listActivities(targetId, order, offset,
				limit);

		JsonArray attachments = new JsonArray();
		JsonObject jsonObj = new JsonObject();

		jsonObj.add(Constants.ATTACHMENTS, attachments);

		for (Activity activity : activities) {
			JsonObject body = activity.getBody();
			attachments.add(body);
		}

		return jsonObj;
	}

	@SuppressWarnings("rawtypes")
	@Override
	/**
	 * Retrieve paginated ordered activities List of a given target.
	 * 
	 * @param targetId
	 * @param order
	 * @param offset
	 * @return limit
	 * @return List<Activity>
	 * @throws SocialActivityException 
	 */
	public List<Activity> listActivities(String targetId, String order,
			int offset, int limit) throws SocialActivityException {
		Connection connection = null;
		List<Activity> activities = null;
		// PreparedStatement statement;
		ResultSet resultSet;
		String tenantDomain = SocialUtil.getTenantDomain();
		limit = SocialUtil.getActivityLimit(limit);
		String errorMsg = "Unable to retrieve activities. ";

		try {
			connection = DSConnection.getConnection();

			if (obj == null) {
				cls = SocialUtil.loadQueryAdaptorClass();
				obj = SocialUtil.getQueryAdaptorObject(cls);
			}

			Class[] param = { Connection.class, String.class, String.class,
					String.class, int.class, int.class };

			Method method = cls.getDeclaredMethod("getPaginatedActivitySet",
					param);
			resultSet = (ResultSet) method.invoke(obj, connection, targetId,
					tenantDomain, order, limit, offset);

			activities = new ArrayList<Activity>();
			while (resultSet.next()) {
				JsonObject body = (JsonObject) parser.parse(resultSet
						.getString(Constants.BODY_COLUMN));
				int id = resultSet.getInt(Constants.ID_COLUMN);
				Activity activity = new SQLActivity(body);
				activity.setId(id);
				activities.add(activity);
			}
			resultSet.close();
		} catch (SQLException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (DataSourceException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (IllegalAccessException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (NoSuchMethodException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (SecurityException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (IllegalArgumentException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (InvocationTargetException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} finally {
			DSConnection.closeConnection(connection);
		}
		/*
		 * if (activities != null) { return activities; } else { return
		 * Collections.emptyList(); }
		 */
		return activities;
	}

	/*
	 * @Override public List<Activity> listActivitiesChronologically(String
	 * targetId, String order, int offset, int limit) { List<Activity>
	 * activities; activities = listActivities(targetId, order, offset, limit);
	 * return activities; }
	 */

	@SuppressWarnings("unused")
	private String getTenant(JsonObject body) {
		JsonObject actor = body.getAsJsonObject("actor");
		if (log.isDebugEnabled()) {
			log.debug("Generating tenantDomain from Actor: " + actor);
		}
		if (actor != null) {
			String id = actor.get("id").getAsString();
			int j = id.lastIndexOf('@') + 1;
			if (j > 0) {
				return id.substring(j);
			}
		}
		return null;
	}

	@Override
	/**
	 * Retrieve targets with average rating, greater than the given value.
	 * 
	 * @param avgRating
	 * @param limit
	 * @return top assets JsonObject
	 * @throws SocialActivityException 
	 */
	public JsonObject getTopAssets(double avgRating, int limit)
			throws SocialActivityException {
		Connection connection = null;
		String errorMsg = "Unable to retrieve top assets. ";

		PreparedStatement statement;
		ResultSet resultSet;
		JsonArray assets = new JsonArray();
		JsonObject jsonObj = new JsonObject();

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + TOP_ASSETS_SELECT_SQL);
			}
			connection = DSConnection.getConnection();
			statement = connection.prepareStatement(TOP_ASSETS_SELECT_SQL);
			// TODO need to implement limit
			resultSet = statement.executeQuery();
			jsonObj.add(Constants.ASSETS, assets);

			if (resultSet.next()) {

				int total, count;
				double avg;
				total = resultSet.getInt(Constants.RATING_TOTAL);
				count = resultSet.getInt(Constants.RATING_COUNT);
				avg = (double) total / count;

				if (avg >= avgRating) {
					String targetId = resultSet
							.getString(Constants.CONTEXT_ID_COLUMN);
					assets.add((JsonElement) parser.parse(targetId));
				}
			}
			resultSet.close();
		} catch (SQLException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (DataSourceException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} finally {
			DSConnection.closeConnection(connection);
		}
		if (assets.size() > 0) {
			return jsonObj;
		} else {
			return null;
		}

	}

	@Override
	/**
	 * Retrieve comments belongs to a particular target with likes greater than the given value.
	 * 
	 * @param targetId
	 * @param likes
	 * @return top comments JsonObject
	 * @throws SocialActivityException 
	 */
	public JsonObject getTopComments(String targetId, int likes)
			throws SocialActivityException {
		Connection connection = null;
		PreparedStatement statement;
		ResultSet resultSet;
		JsonArray comments = new JsonArray();
		JsonObject jsonObj = new JsonObject();
		String errorMsg = "Unable to retrieve top comments. ";

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + TOP_COMMENTS_SELECT_SQL);
			}
			connection = DSConnection.getConnection();
			statement = connection.prepareStatement(TOP_COMMENTS_SELECT_SQL);
			statement.setString(1, targetId);
			statement.setInt(2, likes);
			resultSet = statement.executeQuery();
			jsonObj.add(Constants.COMMENTS, comments);

			while (resultSet.next()) {
				JsonObject body = (JsonObject) parser.parse(resultSet
						.getString(Constants.BODY_COLUMN));
				int id = resultSet.getInt(Constants.ID_COLUMN);
				Activity activity = new SQLActivity(body);
				activity.setId(id);
				comments.add(activity.getBody());
			}
			resultSet.close();
		} catch (SQLException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (DataSourceException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} finally {
			DSConnection.closeConnection(connection);
		}
		if (comments.size() > 0) {
			return jsonObj;
		} else {
			return null;
		}
	}

	@Override
	/**
	 * Retrieve comments belongs to a particular target with likes greater than the given value.
	 * 
	 * @param userId
	 * @param targetId
	 * @param like (1 for LIKE 0 for DISLIKE)
	 * @return boolean
	 * @throws SocialActivityException 
	 */
	public boolean isUserlikedActivity(String userId, String targetId, int like)
			throws SocialActivityException {
		Connection connection = null;
		String errorMsg = "Error while checking user like activity. ";

		PreparedStatement statement;
		ResultSet resultSet;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + SELECT_LIKE_STATUS);
			}
			connection = DSConnection.getConnection();
			statement = connection.prepareStatement(SELECT_LIKE_STATUS);
			statement.setString(1, userId);
			statement.setString(2, targetId);
			statement.setInt(3, like);
			resultSet = statement.executeQuery();

			if (resultSet.next()) {
				return true;
			}

		} catch (SQLException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (DataSourceException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} finally {
			DSConnection.closeConnection(connection);
		}
		return false;
	}

	@Override
	/**
	 * Poll for latest comments.
	 * 
	 * @param targetId
	 * @param id
	 * @return latest comments JsonObject
	 * @throws SocialActivityException 
	 */
	public JsonObject pollNewestComments(String targetId, int id)
			throws SocialActivityException {
		Connection connection = null;
		PreparedStatement statement;
		ResultSet resultSet;
		JsonArray comments = new JsonArray();
		JsonObject jsonObj = new JsonObject();
		String errorMsg = "Unable to retrieve latest comments. ";

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + POLL_COMMENTS_SQL);
			}
			connection = DSConnection.getConnection();
			statement = connection.prepareStatement(POLL_COMMENTS_SQL);
			statement.setString(1, targetId);
			statement.setInt(2, id);
			resultSet = statement.executeQuery();
			jsonObj.add(Constants.COMMENTS, comments);

			while (resultSet.next()) {
				JsonObject body = (JsonObject) parser.parse(resultSet
						.getString(Constants.BODY_COLUMN));
				int activityId = resultSet.getInt(Constants.ID_COLUMN);
				Activity activity = new SQLActivity(body);
				activity.setId(activityId);
				comments.add(activity.getBody());
			}
			resultSet.close();
		} catch (SQLException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (DataSourceException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} finally {
			DSConnection.closeConnection(connection);
		}
		if (comments.size() > 0) {
			return jsonObj;
		} else {
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	/**
	 * Get paginated targets according to the average rating.
	 * 
	 * @param type
	 * @param tenantId
	 * @param limit
	 * @param offset
	 * @return JsonObject
	 * @throws SocialActivityException 
	 */
	public JsonObject getPopularAssets(String type, String tenantId, int limit,
			int offset) throws SocialActivityException {
		Connection connection = null;
		String errorMsg = "Unable to retrieve top assets. ";

		ResultSet resultSet;
		JsonArray assets = new JsonArray();
		JsonObject jsonObj = new JsonObject();
		String tenantDomain = SocialUtil.getTenantDomain();

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + POPULAR_ASSETS_SELECT_SQL);
			}
			connection = DSConnection.getConnection();

			if (obj == null) {
				cls = SocialUtil.loadQueryAdaptorClass();
				obj = SocialUtil.getQueryAdaptorObject(cls);
			}

			Class[] param = { Connection.class, String.class, String.class,
					int.class, int.class };

			Method method = cls.getDeclaredMethod("getPopularTargetSet", param);
			resultSet = (ResultSet) method.invoke(obj, connection, type,
					tenantDomain, limit, offset);

			jsonObj.add(Constants.ASSETS, assets);
			Gson gson = new Gson();

			while (resultSet.next()) {
				String targetId = resultSet
						.getString(Constants.CONTEXT_ID_COLUMN);
				assets.add(parser.parse(gson.toJson(targetId)));
			}
			resultSet.close();
		} catch (SQLException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (DataSourceException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (NoSuchMethodException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (SecurityException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (IllegalAccessException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (IllegalArgumentException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} catch (InvocationTargetException e) {
			String message = errorMsg + e.getMessage();
			log.error(message, e);
			throw new SocialActivityException(message, e);
		} finally {
			DSConnection.closeConnection(connection);
		}
		if (assets.size() > 0) {
			return jsonObj;
		} else {
			return null;
		}

	}

}
