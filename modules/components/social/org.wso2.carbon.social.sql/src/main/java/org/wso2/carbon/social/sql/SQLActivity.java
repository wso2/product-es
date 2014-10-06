package org.wso2.carbon.social.sql;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.social.core.Activity;

public class SQLActivity implements Activity {
    private static final Log log = LogFactory.getLog(SQLActivity.class);
    private final String bodyString;
    private static final JsonParser parser = new JsonParser();
    private final long timestamp;

    private JsonObject body;

    public SQLActivity(String bodyString, long timestamp) {
        this.bodyString = bodyString;
        this.timestamp = timestamp;
    }

    private void parse() {
        if (body == null) {
            JsonElement parsedBody = parser.parse(bodyString);

            if (parsedBody instanceof JsonObject) {
                body = (JsonObject) parsedBody;
            } else {
                log.error("activity is not a valid json object");//TODO: throw error
            }
        }
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
        return (int) timestamp;
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
