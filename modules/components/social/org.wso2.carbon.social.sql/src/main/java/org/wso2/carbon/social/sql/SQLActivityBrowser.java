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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.SortOrder;
import org.wso2.carbon.social.sql.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.wso2.carbon.social.sql.SocialUtil;

public class SQLActivityBrowser implements ActivityBrowser {
	private static final Log log = LogFactory.getLog(SQLActivityBrowser.class);
	public static final String PAGIN_SELECT_SQL = "SELECT * FROM "
			+ Constants.SOCIAL_TABLE_NAME + " WHERE "
			+ Constants.CONTEXT_ID_COLUMN + "=? AND "
			+ Constants.TENANT_DOMAIN_COLUMN + "=? AND " + Constants.TIMESTAMP
			+ " < (SELECT " + Constants.TIMESTAMP + " FROM "
			+ Constants.SOCIAL_TABLE_NAME + " WHERE " + Constants.ID_COLUMN
			+ " =?) ORDER BY " + Constants.TIMESTAMP + " DESC LIMIT ?";

	public static final String INIT_SELECT_SQL = "SELECT * FROM "
			+ Constants.SOCIAL_TABLE_NAME + " WHERE "
			+ Constants.CONTEXT_ID_COLUMN + "=? AND "
			+ Constants.TENANT_DOMAIN_COLUMN + "=? " + "ORDER BY "
			+ Constants.TIMESTAMP + " DESC LIMIT ?";

	public static final String SELECT_CACHE_SQL = "SELECT * FROM "
			+ Constants.SOCIAL_RATING_CACHE_TABLE_NAME + " WHERE "
			+ Constants.CONTEXT_ID_COLUMN + "=?";

	private JsonParser parser = new JsonParser();

	@Override
	public double getRating(String targetId) {
		DSConnection con = new DSConnection();
		Connection connection = con.getConnection();

		if (connection == null) {
			return 0;
		}

		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(SELECT_CACHE_SQL);

			statement.setString(1, targetId);
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				int total, count;
				total = Integer.parseInt(resultSet
						.getString(Constants.RATING_TOTAL));
				count = Integer.parseInt(resultSet
						.getString(Constants.RATING_COUNT));
				return (double) total / count;
			} else {
				return 0;
			}

		} catch (SQLException e) {
			log.error("Unable to retrieve rating for target: " + targetId + e);
		} finally {
			if (con != null) {
				con.closeConnection(connection);
			}
		}

		return 0;
	}

	@Override
	public JsonObject getSocialObject(String targetId, SortOrder order,
			String PreviousActivityID, int limit) {

		List<Activity> activities = listActivities(targetId,
				PreviousActivityID, limit);

		JsonArray attachments = new JsonArray();
		JsonObject jsonObj = new JsonObject();

		jsonObj.add(Constants.ATTACHMENTS, attachments);

		for (Activity activity : activities) {
			JsonObject body = activity.getBody();
			attachments.add(body);
		}

		return jsonObj;
	}

	@Override
	public List<Activity> listActivities(String targetId,
			String PreviousActivityID, int limit) {
		List<Activity> activities = null;
		DSConnection con = new DSConnection();
		Connection connection = con.getConnection();
		if (connection != null) {
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			String tenantDomain = SocialUtil.getTenantDomain();
			limit = SocialUtil.getActivityLimit(limit);
			PreviousActivityID = SocialUtil
					.getPreviousActivityID(PreviousActivityID);
			String SELECT_SQL;

			if (PreviousActivityID != null) {
				SELECT_SQL = PAGIN_SELECT_SQL;
			} else {
				SELECT_SQL = INIT_SELECT_SQL;
			}

			try {
				statement = connection.prepareStatement(SELECT_SQL);

				statement.setString(1, targetId);
				statement.setString(2, tenantDomain);

				if (PreviousActivityID != null) {
					statement.setString(3, PreviousActivityID);
					statement.setInt(4, limit);
				} else {
					statement.setInt(3, limit);
				}

				resultSet = statement.executeQuery();
				activities = new ArrayList<Activity>();
				while (resultSet.next()) {
					JsonObject body = (JsonObject) parser.parse(resultSet
							.getString(Constants.BODY_COLUMN));
					Activity activity = new SQLActivity(body);
					activities.add(activity);
				}
			} catch (SQLException e) {
				log.error("Unable to retrieve activities. " + e);
			} finally {
				if (con != null) {
					con.closeConnection(connection);
				}
			}

		}
		if (activities != null) {
			return activities;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<Activity> listActivitiesChronologically(String targetId,
			String PreviousActivityID, int limit) {
		List<Activity> activities = listActivities(targetId,
				PreviousActivityID, limit);
		return activities;
	}

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
}
