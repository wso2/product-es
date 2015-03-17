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

package org.wso2.carbon.social.core.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.ActivityPublisher;
import org.wso2.carbon.social.core.SocialActivityException;

import java.util.List;

public abstract class SocialActivityService {

	/**
	 * Allows activity to be passed into the service. 
	 * 
	 * Eg: -
	 * {"verb":"post","object"
	 * :{"objectType":"review","content":"sample comment",
	 * "rating":4,"likes":{"totalItems"
	 * :0},"dislikes":{"totalItems":0}},"target":
	 * {"id":"319f492d-3210-4096-8ffb-f49b0fed1d2d"
	 * },"actor":{"id":"user@tenant.com","objectType":"person"}}
	 * 
	 * @param activity
	 * @throws SocialActivityException 
	 * @throws JsonSyntaxException 
	 * 
	 */
	public long publish(String activity) throws SocialActivityException {
		return getActivityPublisher().publish(activity);
	}

	/**
	 * 
	 * @param targetId
	 * @param order
	 * @param offset
	 * @param limit
	 * @return
	 * @throws SocialActivityException 
	 */
	public String[] listActivities(String targetId, String order, int offset,
			int limit) throws SocialActivityException {
		List<Activity> activities = getActivityBrowser()
				.listActivities(targetId, order, offset, limit);
		String[] serializedActivities = new String[activities.size()];
		for (int i = 0; i < activities.size(); i++) {
			serializedActivities[i] = activities.get(i).toString();
		}
		return serializedActivities;
	}

	/**
	 * Allows asset id to be passed into the service and retrieve average rating
	 * for the given asset
	 * 
	 * @param targetId
	 * @return averageRating
	 */
	public JsonObject getRating(String targetId) throws SocialActivityException {
		return getActivityBrowser().getRating(targetId);
	}

	/**
	 * Allows targetId, sortOrder, offset and limit to be passed into the
	 * service and retrieve social activities. offset and limit will be used for
	 * pagination purpose.
	 * 
	 * 1st page : offset=0 and limit =10 (returns 1st 10 activities according to
	 * the given sort order) 2nd page : offset:10 and limit=10 ...
	 * 
	 * @param targetId
	 * @param sortOrder
	 * @param offset
	 * @param limit
	 * @return
	 */
	public String getSocialObjectJson(String targetId, String sortOrder,
			int offset, int limit) throws SocialActivityException {
		JsonObject socialObject = getActivityBrowser().getSocialObject(
				targetId, sortOrder, offset, limit);

		if (socialObject != null) {
			return socialObject.toString();
		} else {
			return "{}";
		}
	}

	/**
	 * Allows average rating and limit to be passed and returns assets with
	 * greater average rating value.
	 * 
	 * @param avgRating
	 * @param limit
	 * @return
	 */
	public String getTopAssets(double avgRating, int limit) throws SocialActivityException {
		JsonObject topAssetObject = getActivityBrowser().getTopAssets(
				avgRating, limit);
		if (topAssetObject != null) {
			return topAssetObject.toString();
		} else {
			return "{}";
		}
	}

	/**
	 * Allows target id and number of likes to be passed and return social
	 * activities with greater number of likes.
	 * 
	 * @param targetId
	 * @param likes
	 * @return
	 */
	public String getTopComments(String targetId, int likes) throws SocialActivityException {
		JsonObject topCommentObject = getActivityBrowser().getTopComments(
				targetId, likes);
		if (topCommentObject != null) {
			return topCommentObject.toString();
		} else {
			return "{}";
		}
	}

	/**
	 * 
	 * @param targetId
	 * @param timestamp
	 * @return
	 */
	public String pollLatestComments(String targetId, int timestamp) throws SocialActivityException {
		JsonObject newestCommentObject = getActivityBrowser()
				.pollNewestComments(targetId, timestamp);
		if (newestCommentObject != null) {
			return newestCommentObject.toString();
		} else {
			return "{}";
		}
	}

	/**
	 * Allows activity id and user id to be passed into the service and remove
	 * given activity
	 * 
	 * @param activityId
	 * @param userId
	 * @return
	 */
	public boolean removeActivity(String activityId, String userId) throws SocialActivityException {
		return getActivityPublisher().remove(activityId, userId);
	}

	/**
	 * Allows user id, target id and like/unlike value into the service and get
	 * like/unlike status
	 * 
	 * @param userId
	 * @param targetId
	 * @param like
	 * @return
	 */
	public boolean isUserliked(String userId, String targetId, int like) throws SocialActivityException {
		return getActivityBrowser().isUserlikedActivity(userId, targetId, like);
	}

	public abstract ActivityBrowser getActivityBrowser();

	public abstract ActivityPublisher getActivityPublisher();

	/**
	 * Allows an external configuration object to be passed into the Service
	 * //TODO: remove this. config should happen independent of the service
	 *
	 * @param configObject
	 */
	public abstract void configPublisher(String configuration);
}
