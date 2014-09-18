package org.wso2.carbon.social.core;

import org.mozilla.javascript.NativeObject;

import java.util.UUID;

public abstract class ActivityPublisher {
    public String publish(NativeObject activity) {
        String id = UUID.randomUUID().toString();
        return publish(id, activity);
    }

    protected abstract String publish(String id, NativeObject activity);
    public abstract boolean vote(String commentId, String actorId, int vote);
}