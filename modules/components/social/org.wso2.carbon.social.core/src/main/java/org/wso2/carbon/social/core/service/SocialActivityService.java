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
import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.ActivityPublisher;

import java.util.List;

public abstract class SocialActivityService {

	public String publish(String activity) {
		return getActivityPublisher().publish(activity);
	}

	public String[] listActivities(String targetId, String order, int offset,
			int limit) {
		List<Activity> activities = getActivityBrowser()
				.listActivitiesChronologically(targetId, order, offset,
						limit);
		String[] serializedActivities = new String[activities.size()];
		for (int i = 0; i < activities.size(); i++) {
			serializedActivities[i] = activities.get(i).toString();
		}
		return serializedActivities;
	}

	public double getRating(String targetId) {
		return getActivityBrowser().getRating(targetId);
	}

	public String getSocialObjectJson(String targetId, String sortOrder,
			int offset, int limit) {
		/*SortOrder order;
		try {
			order = SortOrder.valueOf(sortOrder);
		} catch (IllegalArgumentException e) {
			order = SortOrder.NEWEST;
		}*/
		JsonObject socialObject = getActivityBrowser().getSocialObject(
				targetId, sortOrder, offset, limit);

		if (socialObject != null) {
			return socialObject.toString();
		} else {
			return "{}";
		}
	}

	public String getTopAssets(double avgRating, int limit) {
		JsonObject topAssetObject = getActivityBrowser().getTopAssets(
				avgRating, limit);
		if (topAssetObject != null) {
			return topAssetObject.toString();
		} else {
			return "{}";
		}
	}

	public String getTopComments(String targetId, int likes) {
		JsonObject topCommentObject = getActivityBrowser().getTopComments(
				targetId, likes);
		if (topCommentObject != null) {
			return topCommentObject.toString();
		} else {
			return "{}";
		}
	}
	
	public String pollLatestComments(String targetId, int timestamp) {
		JsonObject newestCommentObject = getActivityBrowser().pollNewestComments(
				targetId, timestamp);
		if (newestCommentObject != null) {
			return newestCommentObject.toString();
		} else {
			return "{}";
		}
	}

	public boolean removeActivity(String activityId) {
		return getActivityPublisher().remove(activityId);
	}

	public boolean isUserliked(String userId, String targetId, int like) {
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
