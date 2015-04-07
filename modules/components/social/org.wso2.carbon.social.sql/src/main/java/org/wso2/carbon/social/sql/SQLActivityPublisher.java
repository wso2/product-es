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
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.social.sql.Constants;
import org.wso2.carbon.social.sql.SocialUtil;
import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityPublisher;
import org.wso2.carbon.social.core.SocialActivityException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * 
 * SQLActivityPublisher class is responsible for all activity
 * publishing/removing related tasks. 1. Publish Comments/Rating and Likes. 2.
 * Maintain Rating cache.
 * 
 */

public class SQLActivityPublisher extends ActivityPublisher {

	private static final Log log = LogFactory
			.getLog(SQLActivityPublisher.class);
	public static final String errorStr = "Failed to publish the social event.";
	// TODO revisit constants for SQL queries
	private static final String INSERT_COMMENT_SQL = "INSERT INTO "
			+ Constants.SOCIAL_COMMENTS_TABLE_NAME + "("
			+ Constants.BODY_COLUMN + "," + Constants.CONTEXT_ID_COLUMN + ","
			+ Constants.USER_COLUMN + "," + Constants.TENANT_DOMAIN_COLUMN
			+ ", " + Constants.LIKES_COLUMN + ", " + Constants.UNLIKES_COLUMN
			+ ", " + Constants.TIMESTAMP + ") VALUES(?, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_RATING_SQL = "INSERT INTO "
			+ Constants.SOCIAL_RATING_TABLE_NAME + "("
			+ Constants.COMMENT_ID_COLUMN + "," + Constants.CONTEXT_ID_COLUMN
			+ "," + Constants.USER_COLUMN + ", "
			+ Constants.TENANT_DOMAIN_COLUMN + ", " + Constants.RATING_COLUMN
			+ ", " + Constants.TIMESTAMP + ") VALUES(?, ?, ?, ?, ?, ?)";

	private static final String INSERT_LIKE_SQL = "INSERT INTO "
			+ Constants.SOCIAL_LIKES_TABLE_NAME + "("
			+ Constants.CONTEXT_ID_COLUMN + "," + Constants.USER_COLUMN + ", "
			+ Constants.TENANT_DOMAIN_COLUMN + ", "
			+ Constants.LIKE_VALUE_COLUMN + "," + Constants.TIMESTAMP
			+ ") VALUES(?, ?, ?, ?, ?)";

	private static final String DELETE_LIKE_ACTIVITY = "DELETE FROM "
			+ Constants.SOCIAL_LIKES_TABLE_NAME + " WHERE "
			+ Constants.CONTEXT_ID_COLUMN + " = ? AND " + Constants.USER_COLUMN
			+ " =?";

	private static final String COMMENT_ACTIVITY_SELECT_FOR_UPDATE_SQL = "SELECT "
			+ Constants.BODY_COLUMN
			+ " FROM "
			+ Constants.SOCIAL_COMMENTS_TABLE_NAME
			+ " WHERE "
			+ Constants.ID_COLUMN + " = ?";

	public static final String COMMENT_ACTIVITY_SELECT_SQL = "SELECT "
			+ Constants.BODY_COLUMN + " FROM "
			+ Constants.SOCIAL_COMMENTS_TABLE_NAME + " WHERE "
			+ Constants.ID_COLUMN + " = ?";

	private static final String COMMENT_ACTIVITY_UPDATE_SQL = "UPDATE "
			+ Constants.SOCIAL_COMMENTS_TABLE_NAME + " SET "
			+ Constants.BODY_COLUMN + "= ?, " + Constants.LIKES_COLUMN
			+ "= ?, " + Constants.UNLIKES_COLUMN + "= ? WHERE "
			+ Constants.ID_COLUMN + " = ?";

	public static final String SELECT_CACHE_SQL = "SELECT "
			+ Constants.RATING_TOTAL + "," + Constants.RATING_COUNT + " FROM "
			+ Constants.SOCIAL_RATING_CACHE_TABLE_NAME + " WHERE "
			+ Constants.CONTEXT_ID_COLUMN + "= ?";

	public static final String UPDATE_CACHE_SQL = "UPDATE "
			+ Constants.SOCIAL_RATING_CACHE_TABLE_NAME + " SET "
			+ Constants.RATING_TOTAL + "= ?, " + Constants.RATING_COUNT
			+ "= ?, " + Constants.AVERAGE_RATING + " = ? WHERE " + Constants.CONTEXT_ID_COLUMN + "= ?";

	public static final String INSERT_CACHE_SQL = "INSERT INTO "
			+ Constants.SOCIAL_RATING_CACHE_TABLE_NAME + " ("
			+ Constants.CONTEXT_ID_COLUMN + ", " + Constants.RATING_TOTAL
			+ ", " + Constants.RATING_COUNT + ", "+ Constants.AVERAGE_RATING + ", " + Constants.TENANT_DOMAIN_COLUMN + ") VALUES(?, ?, ?, ?, ?)";

	public static final String DELETE_COMMENT_SQL = "DELETE FROM "
			+ Constants.SOCIAL_COMMENTS_TABLE_NAME + " WHERE "
			+ Constants.ID_COLUMN + " = ?";

	public static final String DELETE_RATING_SQL = "DELETE FROM "
			+ Constants.SOCIAL_RATING_TABLE_NAME + " WHERE "
			+ Constants.ID_COLUMN + " = ?";

	public static final String DELETE_LIKES_SQL = "DELETE FROM "
			+ Constants.SOCIAL_LIKES_TABLE_NAME + " WHERE "
			+ Constants.CONTEXT_ID_COLUMN + " = ?";

	private JsonParser parser = new JsonParser();

	@Override
	protected long publishActivity(JsonObject jsonObject)
			throws SocialActivityException {
		long activityId = -1;
		SQLActivity activity = new SQLActivity(jsonObject);
		String errorMessage = "Error while publishing the social activity.";

		// TODO use review as the verb instead of post
		try {
			if (Constants.POST_VERB.equals(activity.getVerb())) {
				activityId = publishPostActivity(activity, jsonObject);
			} else {
				// Handle like,dislike,unlike,undislike verbs
				activityId = publishLikeActivity(activity);
			}
		} catch (SQLException e) {
			throw new SocialActivityException(errorMessage, e);
		} catch (DataSourceException e) {
			throw new SocialActivityException(errorMessage, e);
		} catch (JsonSyntaxException e){
			throw new SocialActivityException(errorMessage, e);
		}
		return activityId;
	}

	private long publishPostActivity(SQLActivity activity, JsonObject jsonObject)
			throws SQLException, DataSourceException, SocialActivityException {

		PreparedStatement commentStatement;
		PreparedStatement ratingStatement;
		int commentReturn = -1;
		long autoGeneratedKey = -1;

		String json = jsonObject.toString();
		String targetId = activity.getTargetId();
		String userId = activity.getActorId();
		int timeStamp = activity.getTimestamp();
		int totalLikes = activity.getLikeCount();
		int totalUnlikes = activity.getDislikeCount();
		int rating = activity.getRating();
		Connection connection = null;

		String tenantDomain = SocialUtil.getTenantDomain();

		try {
			connection = DSConnection.getConnection();
			connection.setAutoCommit(false);

			if (log.isDebugEnabled()) {
				log.debug("Executing: " + INSERT_COMMENT_SQL);
			}

			commentStatement = connection.prepareStatement(INSERT_COMMENT_SQL,
					Statement.RETURN_GENERATED_KEYS);
			commentStatement.setString(1, json);
			commentStatement.setString(2, targetId);
			commentStatement.setString(3, userId);
			commentStatement.setString(4, tenantDomain);
			commentStatement.setInt(5, totalLikes);
			commentStatement.setInt(6, totalUnlikes);
			commentStatement.setInt(7, timeStamp);
			commentReturn = commentStatement.executeUpdate();

			ResultSet generatedKeys = commentStatement.getGeneratedKeys();

			if (!generatedKeys.next()) {
				log.error("Unable to publish the activity.");
			} else {
				autoGeneratedKey = generatedKeys.getLong(1);
				generatedKeys.close();
				// handle rating activity which comes inside the review
				// TODO introduce proper value when there is empty rating value
				if (rating > 0) {
					if (log.isDebugEnabled()) {
						log.debug("Executing: " + INSERT_RATING_SQL);
					}

					ratingStatement = connection
							.prepareStatement(INSERT_RATING_SQL);
					ratingStatement.setLong(1, autoGeneratedKey);
					ratingStatement.setString(2, targetId);
					ratingStatement.setString(3, userId);
					ratingStatement.setString(4, tenantDomain);
					ratingStatement.setInt(5, rating);
					ratingStatement.setInt(6, timeStamp);
					ratingStatement.executeUpdate();

					updateRatingCache(connection, targetId, rating);
				}
				connection.commit();
				if (commentReturn > 0) {
					if (log.isDebugEnabled()) {
						log.debug("Activity published successfully. "
								+ " Activity ID: " + autoGeneratedKey
								+ " TargetID: " + targetId + " JSON: " + json);
					}
					return autoGeneratedKey;
				} else {
					String message = "Error while publishing the activity. ";
					if(log.isDebugEnabled()){
						log.debug(message + json);
					}else{
						log.error(message);
					}
					throw new SocialActivityException(message);
				}
			}

		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				log.error(Constants.ROLLBACK_ERROR + " " + e1.getMessage(), e1);
				throw e1;
			}
			log.error(errorStr + e.getMessage(), e);
			throw e;
		} catch (DataSourceException e) {
			log.error(errorStr + e.getMessage(), e);
			throw e;
		} finally {

			DSConnection.closeConnection(connection);

		}
		return autoGeneratedKey;
	}

	/**
	 * publish like/dislike activity
	 * 
	 * @param activity
	 * @return
	 * @throws SQLException
	 * @throws DataSourceException
	 * @throws SocialActivityException 
	 */
	private long publishLikeActivity(SQLActivity activity)
			throws JsonSyntaxException, SQLException, DataSourceException, SocialActivityException {

		Connection connection = null;
		PreparedStatement selectActivityStatement;
		PreparedStatement updateActivityStatement;
		ResultSet resultSet;
		long activityId = 0;

		try {
			connection = DSConnection.getConnection();
			connection.setAutoCommit(false);
			String verb = activity.getVerb();
			// TODO update this to a boolean if possible
			int likeValue;
			// target of a like activity is a comment
			String commentID = activity.getTargetId();

			if (log.isDebugEnabled()) {
				log.debug("Executing: "
						+ COMMENT_ACTIVITY_SELECT_FOR_UPDATE_SQL);
			}

			selectActivityStatement = connection
					.prepareStatement(COMMENT_ACTIVITY_SELECT_FOR_UPDATE_SQL);
			selectActivityStatement.setString(1, commentID);
			resultSet = selectActivityStatement.executeQuery();
			if (!resultSet.next()) {
				log.error("Unable to publish like activity for comment : "
						+ commentID);
			} else {
				JsonObject currentBody;
				currentBody = (JsonObject) parser.parse(resultSet
						.getString(Constants.BODY_COLUMN));

				Activity commentActivity = new SQLActivity(currentBody);

				int likeCount = commentActivity.getLikeCount();
				int dislikeCount = commentActivity.getDislikeCount();
				// TODO revisit switch block
				switch (Constants.VERB.valueOf(verb)) {
				case like:
					likeCount++;
					likeValue = 1;
					insertLikeActivity(activity, likeValue, connection);
					break;
				case dislike:
					dislikeCount++;
					likeValue = 0;
					insertLikeActivity(activity, likeValue, connection);
					break;
				case unlike:
					likeCount--;
					removeLikeActivity(activity, connection);
					break;
				case undislike:
					dislikeCount--;
					removeLikeActivity(activity, connection);
					break;
				default:
					String message = "Provided verb: " + verb
							+ " not supported by the social framework.";
					log.error(message);
					throw new SocialActivityException(message);
				}

				commentActivity.setLikeCount(likeCount);
				commentActivity.setDislikeCount(dislikeCount);

				JsonObject json = commentActivity.getBody();
				// UPDATE SOCIAL_COMMENTS SET body=?, likes=?, dislikes=?
				// WHERE
				// id=?;
				if (log.isDebugEnabled()) {
					log.debug("Executing: " + COMMENT_ACTIVITY_UPDATE_SQL);
				}

				updateActivityStatement = connection
						.prepareStatement(COMMENT_ACTIVITY_UPDATE_SQL);

				updateActivityStatement.setString(1, json.toString());
				updateActivityStatement.setInt(2, likeCount);
				updateActivityStatement.setInt(3, dislikeCount);
				updateActivityStatement.setString(4, commentID);
				updateActivityStatement.executeUpdate();
				connection.commit();
			}
			resultSet.close();
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				log.error(Constants.ROLLBACK_ERROR + " " + e1.getMessage(), e1);
				throw e1;
			}
			log.error(errorStr + e.getMessage(), e);
			throw e;
		} catch (DataSourceException e) {
			log.error(errorStr + e.getMessage(), e);
			throw e;
		} catch (JsonSyntaxException e) {
			log.error("Malformed JSON element found: " + e.getMessage(), e);
			throw e;
		} finally {
			DSConnection.closeConnection(connection);
		}
		return activityId;

	}

	private void removeLikeActivity(SQLActivity activity, Connection connection)
			throws SQLException {
		PreparedStatement deleteActivityStatement = null;
		String targetId = activity.getTargetId();
		String actor = activity.getActorId();

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + DELETE_LIKE_ACTIVITY);
			}

			deleteActivityStatement = connection
					.prepareStatement(DELETE_LIKE_ACTIVITY);
			deleteActivityStatement.setString(1, targetId);
			deleteActivityStatement.setString(2, actor);
			deleteActivityStatement.executeUpdate();

		} catch (SQLException e) {
			log.error("Error while removing like activity from the table: "
					+ Constants.SOCIAL_LIKES_TABLE_NAME + e.getMessage(), e);
			throw e;
		}
	}

	private void insertLikeActivity(SQLActivity activity, int likeValue,
			Connection connection) throws SQLException {
		PreparedStatement insertActivityStatement;
		String targetId = activity.getTargetId();
		String actor = activity.getActorId();
		int timestamp = activity.getTimestamp();
		String tenantDomain = SocialUtil.getTenantDomain();

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + INSERT_LIKE_SQL);
			}

			insertActivityStatement = connection
					.prepareStatement(INSERT_LIKE_SQL);
			insertActivityStatement.setString(1, targetId);
			insertActivityStatement.setString(2, actor);
			insertActivityStatement.setString(3, tenantDomain);
			insertActivityStatement.setInt(4, likeValue);
			insertActivityStatement.setInt(5, timestamp);
			insertActivityStatement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error while adding like activity to the table: "
					+ Constants.SOCIAL_LIKES_TABLE_NAME + e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * rating cache consists of pre-populated rating-average values for a
	 * particular target. We update rating cache when there is a new rating
	 * activity.Rating activity always occurs with a comment activity.
	 * 
	 * @param connection
	 * @param targetId
	 * @param rating
	 * @throws SQLException
	 */
	private void updateRatingCache(Connection connection, String targetId,
			int rating) throws SQLException {
		ResultSet resultSet = null;
		PreparedStatement selectCacheStatement;
		PreparedStatement updateCacheStatement;
		PreparedStatement insertCacheStatement;

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + SELECT_CACHE_SQL);
			}
			selectCacheStatement = connection
					.prepareStatement(SELECT_CACHE_SQL);
			selectCacheStatement.setString(1, targetId);
			resultSet = selectCacheStatement.executeQuery();
			if (!resultSet.next()) {
				//TODO get rid of this block as we are warming up the cache during asset creation
				String tenantDomain = SocialUtil.getTenantDomain();

				if (log.isDebugEnabled()) {
					log.debug("Executing: " + INSERT_CACHE_SQL);
				}
				insertCacheStatement = connection
						.prepareStatement(INSERT_CACHE_SQL);
				insertCacheStatement.setString(1, targetId);
				insertCacheStatement.setInt(2, rating);
				insertCacheStatement.setInt(3, 1);
				insertCacheStatement.setDouble(4, rating);
				insertCacheStatement.setString(5,tenantDomain);
				insertCacheStatement.executeUpdate();
			} else {
				int total, count;
				total = resultSet.getInt(Constants.RATING_TOTAL) + rating;
				count = resultSet.getInt(Constants.RATING_COUNT) + 1;

				if (log.isDebugEnabled()) {
					log.debug("Executing: " + UPDATE_CACHE_SQL);
				}

				updateCacheStatement = connection
						.prepareStatement(UPDATE_CACHE_SQL);

				updateCacheStatement.setInt(1, total);
				updateCacheStatement.setInt(2, count);
				updateCacheStatement.setDouble(3, (double) total/count);
				updateCacheStatement.setString(4, targetId);
				updateCacheStatement.executeUpdate();
			}
		} catch (SQLException e) {
			log.error("Unable to update the cache. " + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean remove(String activityId, String userId)
			throws SocialActivityException {
		Connection connection = null;
		String errorMessage = "Error while removing the activity. Activity ID: "
				+ activityId + ".";

		PreparedStatement deleteComment;
		int ret = 0;
		try {
			connection = DSConnection.getConnection();
			connection.setAutoCommit(false);
			boolean retVal;
			retVal = removeRating(activityId, connection, userId);
			if (!retVal) {
				if (log.isDebugEnabled()) {
					log.debug("Unable to execute cascade delete for activity :" + activityId);
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Executing cascade delete: " + DELETE_COMMENT_SQL);
				}

				deleteComment = connection.prepareStatement(DELETE_COMMENT_SQL);
				deleteComment.setString(1, activityId);
				ret = deleteComment.executeUpdate();
			}
			connection.commit();
		} catch (JsonSyntaxException e) {
			log.error(errorMessage + e.getMessage(), e);
			throw new SocialActivityException(errorMessage, e);
		} catch (SQLException e) {

			try {
				connection.rollback();
			} catch (SQLException e1) {
				String rollbackErrorMessage = Constants.ROLLBACK_ERROR + " "
						+ e1.getMessage();
				log.error(rollbackErrorMessage, e1);
				throw new SocialActivityException(rollbackErrorMessage, e1);
			}
			log.error(errorMessage + e.getMessage(), e);
			throw new SocialActivityException(errorMessage, e);
		} catch (DataSourceException e) {
			log.error(errorMessage + e.getMessage(), e);
			throw new SocialActivityException(errorMessage, e);
		} finally {
			DSConnection.closeConnection(connection);
		}
		return (ret == 1) ? true : false;
	}

	/**
	 * Use this method to update rating-cache if we remove the attached comment
	 * activity
	 * 
	 * @param activityId
	 * @return
	 * @throws SQLException
	 * @throws SocialActivityException
	 */
	private boolean removeRating(String activityId, Connection connection,
			String userId) throws SQLException, JsonSyntaxException,
			SocialActivityException {
		ResultSet selectResultSet;
		ResultSet cacheResultSet;
		PreparedStatement selectStatement;
		PreparedStatement CacheStatement;
		PreparedStatement updateCacheStatement;

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + COMMENT_ACTIVITY_SELECT_SQL);
			}

			selectStatement = connection
					.prepareStatement(COMMENT_ACTIVITY_SELECT_SQL);
			selectStatement.setString(1, activityId);
			selectResultSet = selectStatement.executeQuery();

			if (!selectResultSet.next()) {
				log.error("Unable to remove rating for the given activity : "
						+ activityId);
				return false;
			} else {
				JsonObject body;
					body = (JsonObject) parser.parse(selectResultSet
							.getString(Constants.BODY_COLUMN));
				
				Activity activity = new SQLActivity(body);
				String actorId = activity.getActorId();

				if (!actorId.equals(userId)) {
					if (log.isDebugEnabled()) {
						log.debug("User: "
								+ userId
								+ " not authorized to perform activity remove action.");
					}
					throw new SocialActivityException(
							"User: "
									+ userId
									+ " not authorized to perform activity remove action.");
				}

				int rating = activity.getRating();
				if (rating > 0) {
					// reduce this rating value from target
					String targetId = activity.getTargetId();
					if (log.isDebugEnabled()) {
						log.debug("Executing: " + SELECT_CACHE_SQL);
					}
					CacheStatement = connection
							.prepareStatement(SELECT_CACHE_SQL);
					CacheStatement.setString(1, targetId);
					cacheResultSet = CacheStatement.executeQuery();

					if (cacheResultSet.next()) {
						int total, count;
						total = cacheResultSet.getInt(Constants.RATING_TOTAL);
						count = cacheResultSet.getInt(Constants.RATING_COUNT);

						if (log.isDebugEnabled()) {
							log.debug("Executing: " + UPDATE_CACHE_SQL);
						}

						updateCacheStatement = connection
								.prepareStatement(UPDATE_CACHE_SQL);

						updateCacheStatement.setInt(1, total - rating);
						updateCacheStatement.setInt(2, count - 1);
						updateCacheStatement.setDouble(3, (double)total-rating/count-1);
						updateCacheStatement.setString(4, targetId);
						updateCacheStatement.executeUpdate();
					}
					cacheResultSet.close();
				}
				selectResultSet.close();
				return true;
			}	
		} catch (SQLException e) {
			log.error("Unable to update the rating cache. " + e.getMessage(), e);
			throw e;
		} catch (JsonSyntaxException e) {
			log.error(
					"Malformed JSON element found: " + e.getMessage(),
					e);
			throw e;
		}
	}
	
	@Override
	public int warmUpRatingCache(String targetId)
			throws SocialActivityException {
		PreparedStatement insertCacheWarmUpStatement;
		Connection connection;
		String tenantDomain = SocialUtil.getTenantDomain();
		String errorMessage = "Unable to publish the target: " + targetId
				+ " in to the rating cache.";

		try {
			if (log.isDebugEnabled()) {
				log.debug("Executing: " + INSERT_CACHE_SQL);
			}
			connection = DSConnection.getConnection();
			insertCacheWarmUpStatement = connection
					.prepareStatement(INSERT_CACHE_SQL);
			insertCacheWarmUpStatement.setString(1, targetId);
			insertCacheWarmUpStatement.setInt(2, 0);
			insertCacheWarmUpStatement.setInt(3, 0);
			insertCacheWarmUpStatement.setDouble(4, 0);
			insertCacheWarmUpStatement.setString(5, tenantDomain);

			int returnVal = insertCacheWarmUpStatement.executeUpdate();
			return returnVal;

		} catch (SQLException e) {
			log.error(errorMessage + e.getMessage(), e);
			throw new SocialActivityException(errorMessage, e);
		} catch (DataSourceException e) {
			log.error(errorMessage + e.getMessage(), e);
			throw new SocialActivityException(errorMessage, e);
		}
	}

}
