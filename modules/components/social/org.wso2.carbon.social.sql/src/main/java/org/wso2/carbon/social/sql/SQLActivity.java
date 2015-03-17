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

import com.google.gson.JsonObject;

import org.wso2.carbon.social.core.Activity;

public class SQLActivity implements Activity {
	private final JsonObject body;

	public SQLActivity(JsonObject body) {
		this.body = body;
	}

	@Override
	public String getId() {
		JsonObject object = (JsonObject) this.body.get("object");
		return object.get("id").getAsString();
	}

	@Override
	public JsonObject getBody() {
		return this.body;
	}

	@Override
	public int getTimestamp() {
		return this.body.get("published").getAsInt();
	}

	@Override
	public String getActorId() {
		JsonObject actor = (JsonObject)this.body.get("actor");
		return actor.get("id").getAsString();
	}

	@Override
	public String getTargetId() {
		JsonObject target = (JsonObject)this.body.get("target");
		return target.get("id").getAsString();
	}

	@Override
	public int getLikeCount() {
		JsonObject object = (JsonObject) this.body.get("object");
		JsonObject likes = (JsonObject) object.get("likes");
		return likes.get("totalItems").getAsInt();
	}

	@Override
	public int getDislikeCount() {
		JsonObject object = (JsonObject) this.body.get("object");
		JsonObject dislikes = (JsonObject) object.get("dislikes");
		return dislikes.get("totalItems").getAsInt();
	}

	@Override
	public String getObjectType() {
		JsonObject object = (JsonObject) this.body.get("object");
		return object.get("type").getAsString();
	}

	@Override
	public String getVerb() {
		return this.body.get("verb").getAsString();
	}

	@Override
	public int getRating() {
		JsonObject rating = (JsonObject) this.body.get("object");
		return rating.get("rating").getAsInt();
	}

	@Override
	public void setLikeCount(int likeCount) {
		JsonObject object = (JsonObject) this.body.get("object");
		JsonObject likes = (JsonObject) object.get("likes");
		likes.addProperty("totalItems", likeCount);
		
	}

	@Override
	public void setDislikeCount(int dislikeCount) {
		JsonObject object = (JsonObject) this.body.get("object");
		JsonObject dislikes = (JsonObject) object.get("dislikes");
		dislikes.addProperty("totalItems", dislikeCount);
		
	}
	
	@Override
	public void setId(int id) {
		JsonObject object = (JsonObject) this.body.get("object");
		object.addProperty("id", id);
	}
}
