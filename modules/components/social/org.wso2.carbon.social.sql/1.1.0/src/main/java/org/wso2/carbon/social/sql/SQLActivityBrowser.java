package org.wso2.carbon.social.sql;

import com.google.gson.JsonObject;
import org.wso2.carbon.social.core.Activity;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.SortOrder;

import java.util.List;

public class SQLActivityBrowser implements ActivityBrowser {
    @Override
    public double getRating(String targetId, String tenant) {
        return 0;
    }

    @Override
    public JsonObject getSocialObject(String targetId, String tenant, SortOrder order) {
        return null;
    }

    @Override
    public List<Activity> listActivities(String contextId, String tenantDomain) {
        return null;
    }

    @Override
    public List<Activity> listActivitiesChronologically(String contextId, String tenantDomain) {
        return null;
    }
}
