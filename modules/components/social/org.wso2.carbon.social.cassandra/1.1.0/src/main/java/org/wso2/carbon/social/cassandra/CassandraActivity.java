package org.wso2.carbon.social.cassandra;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.wso2.carbon.social.core.Activity;

public class CassandraActivity implements Activity {

    private final JsonObject body;
    private final int timestamp;

    public CassandraActivity(JsonObject body, int timestamp) {
        this.body = body;
        this.timestamp = timestamp;
    }

    @Override
    public String getId() {
        return body.get("id").getAsString();
    }

    @Override
    public JsonObject getBody() {
        return body;
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return body.toString();
    }

    @Override
    public String getActorId() {
        return body.getAsJsonObject("actor").get("id").getAsString();
    }

    @Override
    public String getTargetId() {
        JsonObject target = body.getAsJsonObject("target");
        if (target != null) {
            JsonElement targetId = target.get("id");
            if (targetId != null) {
                return targetId.getAsString();
            }
        }
        return null;
    }

    @Override
    public int getLikeCount() {
        JsonObject likes = body.getAsJsonObject("likes");
        if (likes != null) {
            JsonElement count = likes.get("totalItems");
            if (count != null) {
                return count.getAsInt();
            }
        }
        return 0;
    }


    @Override
    public int getDislikeCount() {
        JsonObject likes = body.getAsJsonObject("dislikes");
        if (likes != null) {
            JsonElement count = likes.get("totalItems");
            if (count != null) {
                return count.getAsInt();
            }
        }
        return 0;
    }

    @Override
    public String getObjectType() {
        JsonObject object = body.getAsJsonObject("object");
        if (object != null) {
            JsonElement type = object.get("objectType");
            if (type != null) {
                return type.getAsString();
            }
        }
        return null;
    }

    @Override
    public String getVerb() {
        JsonElement verb = body.get("verb");
        if (verb != null) {
            return verb.getAsString();
        }
        return null;
    }

    @Override
    public int getRating() {
        JsonObject object = body.getAsJsonObject("object");
        if (object != null) {
            JsonElement type = object.get("rating");
            if (type != null) {
                return type.getAsInt();
            }
        }
        return 0;
    }
}
