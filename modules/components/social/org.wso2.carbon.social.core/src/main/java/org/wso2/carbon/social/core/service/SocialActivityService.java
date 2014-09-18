package org.wso2.carbon.social.core.service;


import com.google.gson.JsonObject;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.ActivityPublisher;
import org.wso2.carbon.social.core.SortOrder;

import java.util.List;

public abstract class SocialActivityService {

    public String publish(NativeObject activity) {
        return getActivityPublisher().publish(activity);
    }

    public String[] listActivities(String contextId, String tenant) {
        List<Activity> activities = getActivityBrowser().listActivitiesChronologically(contextId, tenant);
        String[] serializedActivities = new String[activities.size()];
        for (int i = 0; i < activities.size(); i++) {
            serializedActivities[i] = activities.get(i).toString();
        }
        return serializedActivities;
    }

    public double getRating(String targetId, String tenant) {
        return getActivityBrowser().getRating(targetId, tenant);
    }

    public boolean vote(String commentId, String actorId, int vote) {
        return getActivityPublisher().vote(commentId, actorId, vote);
    }
    public String getSocialObjectJson(String targetId, String tenant, String sortOrder) {
        SortOrder order;
        try {
            order = SortOrder.valueOf(sortOrder);
        } catch (IllegalArgumentException e) {
            order = SortOrder.NEWEST;
        }
        JsonObject socialObject = getActivityBrowser().getSocialObject(targetId, tenant, order);

        if (socialObject != null) {
            return socialObject.toString();
        } else {
            return "{}";
        }
    }

    public abstract ActivityBrowser getActivityBrowser();

    public abstract ActivityPublisher getActivityPublisher();

    /**
     * Allows an external configuration object to be passed into the Service
     * //TODO: remove this. config should happen independent of the service
     *
     * @param configObject
     */
    public abstract void configPublisher(NativeObject configObject);
}
