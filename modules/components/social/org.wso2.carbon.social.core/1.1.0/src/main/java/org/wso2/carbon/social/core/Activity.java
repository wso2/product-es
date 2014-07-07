package org.wso2.carbon.social.core;

import com.google.gson.JsonObject;

public interface Activity {
    String getId();

    JsonObject getBody();

    int getTimestamp();

    String getActorId();

    String getTargetId();

    int getLikeCount();

    int getDislikeCount();

    String getObjectType();

    String getVerb();

    int getRating();
}
