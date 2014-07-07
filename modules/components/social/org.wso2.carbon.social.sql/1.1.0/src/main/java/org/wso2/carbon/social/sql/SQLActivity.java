package org.wso2.carbon.social.sql;

import com.google.gson.JsonObject;
import org.wso2.carbon.social.core.Activity;

public class SQLActivity implements Activity {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public JsonObject getBody() {
        return null;
    }

    @Override
    public int getTimestamp() {
        return 0;
    }

    @Override
    public String getActorId() {
        return null;
    }

    @Override
    public String getTargetId() {
        return null;
    }

    @Override
    public int getLikeCount() {
        return 0;
    }

    @Override
    public int getDislikeCount() {
        return 0;
    }

    @Override
    public String getObjectType() {
        return null;
    }

    @Override
    public String getVerb() {
        return null;
    }

    @Override
    public int getRating() {
        return 0;
    }
}
