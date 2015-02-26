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

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.social.sql.Constants;
import org.wso2.carbon.social.sql.SocialUtil;
import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityPublisher;
import org.wso2.carbon.social.core.JSONUtil;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SQLActivityPublisher extends ActivityPublisher {

	private static Log log = LogFactory.getLog(SQLActivityPublisher.class);

	public static final String INSERT_SQL = "INSERT INTO "
			+ Constants.SOCIAL_TABLE_NAME + "(" + Constants.ID_COLUMN + ","
			+ Constants.CONTEXT_ID_COLUMN + "," + Constants.BODY_COLUMN + ", "
			+ Constants.TENANT_DOMAIN_COLUMN + ", " + Constants.TIMESTAMP
			+ ") VALUES(?, ?, ?, ?, ?)";

	public static final String INSERT_RATING_SQL = "INSERT INTO "
			+ Constants.SOCIAL_RATING_TABLE_NAME + "(" + Constants.ID_COLUMN
			+ "," + Constants.CONTEXT_ID_COLUMN + "," + Constants.BODY_COLUMN
			+ ", " + Constants.TENANT_DOMAIN_COLUMN + ", "
			+ Constants.RATING_COLUMN + ", " + Constants.TIMESTAMP
			+ ") VALUES(?, ?, ?, ?, ?, ?)";

	public static final String SELECT_CACHE_SQL = "SELECT * FROM "
			+ Constants.SOCIAL_RATING_CACHE_TABLE_NAME + " WHERE "
			+ Constants.CONTEXT_ID_COLUMN + "=?";
	public static final String UPDATE_CACHE_SQL = "UPDATE "
			+ Constants.SOCIAL_RATING_CACHE_TABLE_NAME + " SET "
			+ Constants.RATING_TOTAL + "=?, " + Constants.RATING_COUNT
			+ "=? WHERE " + Constants.CONTEXT_ID_COLUMN + "=?";
	public static final String INSERT_CACHE_SQL = "INSERT INTO "
			+ Constants.SOCIAL_RATING_CACHE_TABLE_NAME + " ("
			+ Constants.CONTEXT_ID_COLUMN + ", " + Constants.RATING_TOTAL
			+ ", " + Constants.RATING_COUNT + ") VALUES(?, ?, ?) ";

	public static final String ACTIVITY_SELECT_SQL = "SELECT * FROM "
			+ Constants.SOCIAL_TABLE_NAME + " WHERE " + Constants.ID_COLUMN
			+ " =?";
	public static final String ACTIVITY_UPDATE_SQL = "UPDATE "
			+ Constants.SOCIAL_TABLE_NAME + " SET " + Constants.BODY_COLUMN
			+ "=? WHERE " + Constants.ID_COLUMN + "=?";

	public static final String ErrorStr = "Failed to publish the social event.";

	private JsonParser parser = new JsonParser();

	@Override
	protected String publish(String id, NativeObject activity) {
		DSConnection con = new DSConnection();
		Connection connection = con.getConnection();
		int commentRet = 0;
		int ratingRet = 0;

		if (connection == null) {
			return null;
		}

		PreparedStatement commentStatement;
		PreparedStatement ratingStatement;

		String json = null;
		try {
			json = JSONUtil.SimpleNativeObjectToJson(activity);

			String targetId = JSONUtil.getNullableProperty(activity,
					Constants.CONTEXT_JSON_PROP, Constants.ID_JSON_PROP);
			if (targetId == null) {
				targetId = JSONUtil.getProperty(activity,
						Constants.TARGET_JSON_PROP, Constants.ID_JSON_PROP);
			}

			String timeStamp = JSONUtil.getProperty(activity,
					Constants.TIMESTAMP);
			String tenantDomain = SocialUtil.getTenantDomain();

			if ("post".equals(SocialUtil.getVerb(activity))) {
				connection.setAutoCommit(false);
				commentStatement = connection.prepareStatement(INSERT_SQL);
				commentStatement.setString(1, id);
				commentStatement.setString(2, targetId);
				commentStatement.setString(3, json);
				commentStatement.setString(4, tenantDomain);
				commentStatement.setString(5, timeStamp);
				commentRet = commentStatement.executeUpdate();

				if (SocialUtil.isValidRating(activity)) {
					int rating = Integer.parseInt(JSONUtil.getProperty(
							activity, Constants.OBJECT_JSON_PROP,
							Constants.RATING));

					ratingStatement = connection
							.prepareStatement(INSERT_RATING_SQL);
					ratingStatement.setString(1, id);
					ratingStatement.setString(2, targetId);
					ratingStatement.setString(3, json);
					ratingStatement.setString(4, tenantDomain);
					ratingStatement.setInt(5, rating);
					ratingStatement.setString(6, timeStamp);
					ratingRet = ratingStatement.executeUpdate();

					updateRatingCache(connection, targetId, rating);
				}

				connection.commit();

				if (commentRet > 0) {
					return id;
				}

				if (log.isDebugEnabled()) {
					if (commentRet > 0) {
						log.debug("Activity published successfully. "
								+ " Activity ID: " + id + " TargetID: "
								+ targetId + " JSON: " + json);
					} else {
						log.debug(ErrorStr + " Activity ID: " + id
								+ " TargetID: " + targetId + " JSON: " + json);
					}
				}
			} else {
				// Handle like,dislike,unlike,undislike verbs
				publishLikes(connection, activity);
			}

		} catch (SQLException e) {
			log.error(ErrorStr + e);
		} catch (JSONException e) {
			log.error("Unable to retrieve the JSON activity. " + e);
		} finally {
			if (con != null) {
				con.closeConnection(connection);
			}
		}

		return null;
	}

	private void publishLikes(Connection connection, NativeObject activity) {
		DSConnection con = new DSConnection();
		Connection selectConnection = con.getConnection();
		ResultSet resultSet;
		String targetId = JSONUtil.getProperty(activity,
				Constants.TARGET_JSON_PROP, Constants.ID_JSON_PROP);

		PreparedStatement selectActivityStatement;
		PreparedStatement updateActivityStatement;

		try {
			selectActivityStatement = selectConnection
					.prepareStatement(ACTIVITY_SELECT_SQL);
			selectActivityStatement.setString(1, targetId);
			resultSet = selectActivityStatement.executeQuery();

			if (resultSet.next()) {

				JsonObject currentBody = (JsonObject) parser.parse(resultSet
						.getString(Constants.BODY_COLUMN));
				Activity currentActivity = new SQLActivity(currentBody);
				int likeCount = currentActivity.getLikeCount();
				int dislikeCount = currentActivity.getDislikeCount();
				String verb = SocialUtil.getVerb(activity);

				switch (Constants.VERB.valueOf(verb)) {
				case like:
					likeCount += 1;
					break;
				case unlike:
					likeCount -= 1;
					break;
				case dislike:
					dislikeCount += 1;
					break;
				case undislike:
					dislikeCount -= 1;
					break;
				default:
					// TODO Add a proper debug log
					break;
				}
				currentActivity.setLikeCount(likeCount);
				currentActivity.setDislikeCount(dislikeCount);

				JsonObject json = currentActivity.getBody();

				connection.setAutoCommit(false);
				updateActivityStatement = connection
						.prepareStatement(ACTIVITY_UPDATE_SQL);

				updateActivityStatement.setString(1, json.toString());
				updateActivityStatement.setString(2, targetId);
				updateActivityStatement.executeUpdate();
				connection.commit();
			}
		} catch (SQLException e) {
			log.error("Error while publishing like/unlike activity. " + e);
		}
	}

	private void updateRatingCache(Connection connection, String targetId,
			int rating) {
		DSConnection con = new DSConnection();
		Connection selectConnection = con.getConnection();
		ResultSet resultSet = null;

		PreparedStatement selectCacheStatement;
		PreparedStatement updateCacheStatement;
		PreparedStatement insertCacheStatement;

		try {
			selectCacheStatement = selectConnection
					.prepareStatement(SELECT_CACHE_SQL);
			selectCacheStatement.setString(1, targetId);
			resultSet = selectCacheStatement.executeQuery();

			if (!resultSet.next()) {
				insertCacheStatement = connection
						.prepareStatement(INSERT_CACHE_SQL);
				insertCacheStatement.setString(1, targetId);
				insertCacheStatement.setInt(2, rating);
				insertCacheStatement.setInt(3, 1);
				insertCacheStatement.executeUpdate();
			} else {
				int total, count;
				total = Integer.parseInt(resultSet
						.getString(Constants.RATING_TOTAL));
				count = Integer.parseInt(resultSet
						.getString(Constants.RATING_COUNT));

				updateCacheStatement = connection
						.prepareStatement(UPDATE_CACHE_SQL);

				updateCacheStatement.setInt(1, total + rating);
				updateCacheStatement.setInt(2, count + 1);
				updateCacheStatement.setString(3, targetId);
				updateCacheStatement.executeUpdate();
			}
		} catch (SQLException e) {
			log.error("Unable to update the cache. " + e);
		} finally {
			con.closeConnection(selectConnection);
		}
	}

}
