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

package org.wso2.carbon.social.db.adapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * This class is there to handle cross database pagination, insert activities and RETURN_GENERATED_KEYS for H2/MySQL.
 */

public class GenericQueryAdapter implements AdapterInterface {

	protected static final Log log = LogFactory
			.getLog(GenericQueryAdapter.class);
	protected static final String errorMsg = "Unable to generate the resultset";
	protected static final String preparedStatementMsg = "Creating preparedStatement for :";

	private static final String COMMENT_SELECT_SQL_DESC = "SELECT body, id FROM SOCIAL_COMMENTS WHERE payload_context_id = ? AND tenant_domain = ? ORDER BY id DESC LIMIT ?,?";

	private static final String COMMENT_SELECT_SQL_ASC = "SELECT body, id FROM SOCIAL_COMMENTS WHERE payload_context_id = ? AND tenant_domain = ? ORDER BY id ASC LIMIT ?,?";

	private static final String POPULAR_COMMENTS_SELECT_SQL = "SELECT body, id FROM SOCIAL_COMMENTS WHERE payload_context_id = ? AND tenant_domain = ? ORDER BY likes DESC LIMIT ?,?";

	private static final String POPULAR_ASSETS_SELECT_SQL = "SELECT payload_context_id FROM SOCIAL_RATING_CACHE WHERE payload_context_id LIKE ? AND tenant_domain = ? ORDER BY rating_average DESC LIMIT ?,?";

	private static final String INSERT_COMMENT_SQL = "INSERT INTO SOCIAL_COMMENTS (body, payload_context_id, user_id, tenant_domain, likes, unlikes, timestamp) VALUES(?, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_RATING_SQL = "INSERT INTO SOCIAL_RATING (comment_id, payload_context_id, user_id, tenant_domain, rating, timestamp) VALUES(?, ?, ?, ?, ?, ?)";

	private static final String INSERT_LIKE_SQL = "INSERT INTO SOCIAL_LIKES (payload_context_id, user_id, tenant_domain, like_value, timestamp) VALUES(?, ?, ?, ?, ?)";

	@Override
	public ResultSet getPaginatedActivitySet(Connection connection,
			String targetId, String tenant, String order, int limit, int offset)
			throws SQLException {
		PreparedStatement statement;
		ResultSet resultSet;
		try {
			statement = getPaginatedActivitySetPreparedStatement(connection,
					targetId, tenant, order, limit, offset);
			resultSet = statement.executeQuery();
			
			return resultSet;
			
		} catch (SQLException e) {
			log.error(errorMsg + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public PreparedStatement getPaginatedActivitySetPreparedStatement(
			Connection connection, String targetId, String tenant,
			String order, int limit, int offset) throws SQLException {
		PreparedStatement statement;
		String selectQuery = getSelectquery(order);

		if (log.isDebugEnabled()) {
			log.debug(preparedStatementMsg + selectQuery
					+ " with following parameters, targetId: " + targetId
					+ " tenant: " + tenant + " limit: " + limit + " offset: "
					+ offset);
		}

		statement = connection.prepareStatement(selectQuery);
		statement.setString(1, targetId);
		statement.setString(2, tenant);
		statement.setInt(3, offset);
		statement.setInt(4, limit);
		
		return statement;
	}

	@Override
	public ResultSet getPopularTargetSet(Connection connection, String type,
			String tenantDomain, int limit, int offset) throws SQLException {

		PreparedStatement statement;
		ResultSet resultSet;
		try {
			statement = getPopularTargetSetPreparedStatement(connection, type,
					tenantDomain, limit, offset);
			resultSet = statement.executeQuery();
			return resultSet;

		} catch (SQLException e) {
			log.error(errorMsg + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public PreparedStatement getPopularTargetSetPreparedStatement(
			Connection connection, String type, String tenantDomain, int limit,
			int offset) throws SQLException {
		PreparedStatement statement;

		if (log.isDebugEnabled()) {
			log.debug(preparedStatementMsg + POPULAR_ASSETS_SELECT_SQL
					+ " with following parameters, type: " + type + " tenant: "
					+ tenantDomain + " offset: " + offset + " limit : " + limit);
		}

		statement = connection.prepareStatement(POPULAR_ASSETS_SELECT_SQL);
		statement.setString(1, type + "%");
		statement.setString(2, tenantDomain);
		statement.setInt(3, offset);
		statement.setInt(4, limit);
		
		return statement;

	}

	@Override
	public long insertCommentActivity(Connection connection, String json,
			String targetId, String userId, String tenantDomain,
			int totalLikes, int totalUnlikes, int timeStamp)
			throws SQLException {

		PreparedStatement commentStatement;

		try {
			commentStatement = getInsertCommentActivityPreparedStatement(
					connection, json, targetId, userId, tenantDomain,
					totalLikes, totalUnlikes, timeStamp);
			commentStatement.executeUpdate();

			ResultSet generatedKeys = commentStatement.getGeneratedKeys();

			return getGenaratedKeys(generatedKeys);

		} catch (SQLException e) {
			log.error("Error while publishing comment activity. " + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public PreparedStatement getInsertCommentActivityPreparedStatement(
			Connection connection, String json, String targetId, String userId,
			String tenantDomain, int totalLikes, int totalUnlikes, int timeStamp)
			throws SQLException {
		PreparedStatement commentStatement;

		if (log.isDebugEnabled()) {
			log.debug(preparedStatementMsg + INSERT_COMMENT_SQL
					+ " with following parameters, json: " + json
					+ " targetId: " + targetId + " userId: " + userId
					+ " tenantDomain: " + tenantDomain);
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
		
		return commentStatement;

	}

	@Override
	public boolean insertRatingActivity(Connection connection,
			long autoGeneratedKey, String targetId, String userId,
			String tenantDomain, int rating, int timeStamp) throws SQLException {
		PreparedStatement ratingStatement;
		int returnVal = 0;
		try {
			ratingStatement = getinsertRatingActivityPreparedStatement(
					connection, autoGeneratedKey, targetId, userId,
					tenantDomain, rating, timeStamp);

			returnVal = ratingStatement.executeUpdate();

			boolean value = returnVal > 0 ? true : false;
			return value;

		} catch (SQLException e) {
			log.error("Error while publishing rating activity. " + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public PreparedStatement getinsertRatingActivityPreparedStatement(
			Connection connection, long autoGeneratedKey, String targetId,
			String userId, String tenantDomain, int rating, int timeStamp)
			throws SQLException {
		PreparedStatement ratingStatement;
		if (log.isDebugEnabled()) {
			log.debug(preparedStatementMsg + INSERT_RATING_SQL
					+ " with following parameters, generatedKey: "
					+ autoGeneratedKey + " target: " + targetId + " user: "
					+ userId + " tenant: " + tenantDomain + " rating: "
					+ rating);
		}

		ratingStatement = connection.prepareStatement(INSERT_RATING_SQL);
		ratingStatement.setLong(1, autoGeneratedKey);
		ratingStatement.setString(2, targetId);
		ratingStatement.setString(3, userId);
		ratingStatement.setString(4, tenantDomain);
		ratingStatement.setInt(5, rating);
		ratingStatement.setInt(6, timeStamp);
		
		return ratingStatement;
	}

	@Override
	public boolean insertLikeActivity(Connection connection, String targetId,
			String actor, String tenantDomain, int likeValue, int timestamp)
			throws SQLException {
		PreparedStatement insertActivityStatement;
		int returnVal = 0;
		try {
			insertActivityStatement = getinsertLikeActivityPreparedStatement(
					connection, targetId, actor, tenantDomain, likeValue,
					timestamp);
			returnVal = insertActivityStatement.executeUpdate();

			boolean value = returnVal > 0 ? true : false;
			return value;
			
		} catch (SQLException e) {
			log.error("Error while publishing like activity. " + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public PreparedStatement getinsertLikeActivityPreparedStatement(
			Connection connection, String targetId, String actor,
			String tenantDomain, int likeValue, int timestamp)
			throws SQLException {
		PreparedStatement insertActivityStatement;
		if (log.isDebugEnabled()) {
			log.debug(preparedStatementMsg + INSERT_LIKE_SQL
					+ " with following parameters, target: " + targetId
					+ " user: " + actor + " tenant: " + tenantDomain
					+ " like: " + likeValue);
		}

		insertActivityStatement = connection.prepareStatement(INSERT_LIKE_SQL);
		insertActivityStatement.setString(1, targetId);
		insertActivityStatement.setString(2, actor);
		insertActivityStatement.setString(3, tenantDomain);
		insertActivityStatement.setInt(4, likeValue);
		insertActivityStatement.setInt(5, timestamp);
		
		return insertActivityStatement;
	}

	protected static String getSelectquery(String order) {

		if ("NEWEST".equals(order)) {
			return COMMENT_SELECT_SQL_DESC;
		} else if ("OLDEST".equals(order)) {
			return COMMENT_SELECT_SQL_ASC;
		} else {
			return POPULAR_COMMENTS_SELECT_SQL;
		}
	}

	@Override
	public long getGenaratedKeys(ResultSet generatedKeys) throws SQLException {
		long autoGeneratedKey = -1;

		if (generatedKeys.next()) {
			autoGeneratedKey = generatedKeys.getLong(1);
			generatedKeys.close();
		}
		return autoGeneratedKey;
	}

}
