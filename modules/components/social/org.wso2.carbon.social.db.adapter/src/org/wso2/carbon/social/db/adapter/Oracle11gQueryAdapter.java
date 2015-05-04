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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 /This class is there to handle cross database pagination, insert activities using sequence.next and RETURN_GENERATED_KEYS for Oracle.
 */

public class Oracle11gQueryAdapter extends GenericQueryAdapter implements
		AdapterInterface {

	private static final Log log = LogFactory.getLog(GenericQueryAdapter.class);
	private static final String preparedStatementMsg = "Creating preparedStatement for :";

	private static final String COMMENT_SELECT_SQL_DESC = "SELECT a.* FROM (SELECT b.*, rownum b_rownum FROM (SELECT c.* FROM SOCIAL_COMMENTS c WHERE payload_context_id=? AND tenant_domain=? ORDER BY id DESC) b WHERE rownum <= ?) a WHERE b_rownum > ?";

	private static final String COMMENT_SELECT_SQL_ASC = "SELECT a.* FROM (SELECT b.*, rownum b_rownum FROM (SELECT c.* FROM SOCIAL_COMMENTS c WHERE payload_context_id= ? AND tenant_domain=? ORDER BY id ASC) b WHERE rownum <= ?) a WHERE b_rownum > ?";

	private static final String POPULAR_COMMENTS_SELECT_SQL = "SELECT a.* FROM (SELECT b.*, rownum b_rownum FROM (SELECT c.* FROM SOCIAL_COMMENTS c WHERE payload_context_id=? AND tenant_domain=? ORDER BY likes DESC) b WHERE rownum <= ?) a WHERE b_rownum > ?";

	private static final String POPULAR_ASSETS_SELECT_SQL = "SELECT a.* FROM (SELECT b.*, rownum b_rownum FROM (SELECT c.* FROM SOCIAL_RATING_CACHE c WHERE payload_context_id LIKE ? AND tenant_domain= ? ORDER BY rating_average DESC) b WHERE rownum <= ?) a WHERE b_rownum >= ?";

	private static final String INSERT_COMMENT_SQL = "INSERT INTO SOCIAL_COMMENTS (id, body, payload_context_id, user_id, tenant_domain, likes, unlikes, timestamp) VALUES(SOCIAL_COMMENTS_SEQUENCE.nextval, ?, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_RATING_SQL = "INSERT INTO SOCIAL_RATING (id, comment_id, payload_context_id, user_id, tenant_domain, rating, timestamp) VALUES(SOCIAL_RATING_SEQUENCE.nextval, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_LIKE_SQL = "INSERT INTO SOCIAL_LIKES (id, payload_context_id, user_id, tenant_domain, like_value, timestamp) VALUES(SOCIAL_LIKES_SEQUENCE.nextval, ?, ?, ?, ?, ?)";

	@Override
	public ResultSet getPaginatedActivitySet(Connection connection,
			String targetId, String tenant, String order, int limit, int offset)
			throws SQLException {

		ResultSet resultSet = super.getPaginatedActivitySet(connection,
				targetId, tenant, order, limit, offset);

		return resultSet;
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
		statement.setInt(3, limit + offset);
		statement.setInt(4, offset);
		
		return statement;
	}

	@Override
	public ResultSet getPopularTargetSet(Connection connection, String type,
			String tenantDomain, int limit, int offset) throws SQLException {

		ResultSet resultSet = super.getPopularTargetSet(connection, type,
				tenantDomain, limit, offset);
		
		return resultSet;
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
		statement.setInt(3, limit + offset);
		statement.setInt(4, offset);
		
		return statement;
	}

	@Override
	public long insertCommentActivity(Connection connection, String json,
			String targetId, String userId, String tenantDomain,
			int totalLikes, int totalUnlikes, int timeStamp)
			throws SQLException {

		return super.insertCommentActivity(connection, json, targetId, userId,
				tenantDomain, totalLikes, totalUnlikes, timeStamp);
	}

	@Override
	public PreparedStatement getInsertCommentActivityPreparedStatement(
			Connection connection, String json, String targetId, String userId,
			String tenantDomain, int totalLikes, int totalUnlikes, int timeStamp)
			throws SQLException {
		PreparedStatement commentStatement;
		String generatedColumns[] = { "id" };

		if (log.isDebugEnabled()) {
			log.debug(preparedStatementMsg + INSERT_COMMENT_SQL
					+ " with following parameters, json: " + json
					+ " targetId: " + targetId + " userId: " + userId
					+ " tenantDomain: " + tenantDomain);
		}

		commentStatement = connection.prepareStatement(INSERT_COMMENT_SQL,
				generatedColumns);
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

		return super.insertRatingActivity(connection, autoGeneratedKey,
				targetId, userId, tenantDomain, rating, timeStamp);
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

		return super.insertLikeActivity(connection, targetId, actor,
				tenantDomain, likeValue, timestamp);
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

	@Override
	public long getGenaratedKeys(ResultSet generatedKeys) throws SQLException {
		long autoGeneratedKey = -1;

		if (generatedKeys.next()) {
			autoGeneratedKey = generatedKeys.getInt(1);
			generatedKeys.close();
		}
		
		return autoGeneratedKey;
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

}
