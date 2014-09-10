package org.wso2.carbon.social.core;

import com.google.gson.JsonObject;

import java.util.List;

public interface ActivityBrowser {
    double getRating(String targetId, String tenant);

    JsonObject getSocialObject(String targetId, String tenant, SortOrder order);

    List<Activity> listActivities(String contextId, String tenantDomain);

    List<Activity> listActivitiesChronologically(String contextId, String tenantDomain);
}
