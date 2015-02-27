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

public class Constants {
	//SOCIAL related DB/Table constants
	public static final String SOCIAL_DB_NAME = "WSO2_SOCIAL_DB";
	public static final String SOCIAL_COMMENTS_TABLE_NAME = "SOCIAL_COMMENTS";
	public static final String SOCIAL_RATING_TABLE_NAME = "SOCIAL_RATING";
	public static final String SOCIAL_LIKES_TABLE_NAME = "SOCIAL_LIKES";
	public static final String SOCIAL_RATING_CACHE_TABLE_NAME = "SOCIAL_RATING_CACHE";
	
	public static final String BODY_COLUMN = "body";
	public static final String ID_COLUMN = "id";
	public static final String CONTEXT_ID_COLUMN = "payload_context_id";
	public static final String TENANT_DOMAIN_COLUMN = "tenant_domain";
	public static final String RATING_COLUMN = "rating";
	public static final String USER_COLUMN = "user_id";
	public static final String RATING_TOTAL = "rating_total";
	public static final String RATING_COUNT = "rating_count";
	public static final String LIKES_COLUMN = "likes";
	public static final String UNLIKES_COLUMN = "unlikes";
	
	public static final String ATTACHMENTS = "attachments";
	public static final String TIMESTAMP = "timestamp";
	public static final String ASSETS = "assets";
	public static final String COMMENTS = "comments";
	public static final String RATING = "rating";

	public static final String TARGET_JSON_PROP = "target";
	public static final String VERB_JSON_PROP = "verb";
	public static final String OBJECT_JSON_PROP = "object";
	public static final String CONTEXT_JSON_PROP = "context";
	public static final String ID_JSON_PROP = "id";
	public static final String ACTOR_JSON_PROP = "actor";

	public static final String DSETUP_PATTERN = ".*-Dsetup.*";
	public static final String SETUP_CMD = "setup";
	//Use this value if limit is not specified by the client 
	public static final int MAXIMUM_ACTIVITY_SELECT_COUNT = 100;
	public static final String CONNECTION_ERROR = "Error while getting connection to the datasource.";
	public static final String ROLLBACK_ERROR = "Error while rollback operation.";
	public static final String LIKE_VALUE_COLUMN = "like_value";
	public static final String COMMENT_ID_COLUMN = "comment_id";

	public static enum VERB {
		like, dislike, unlike, undislike;
	}

}