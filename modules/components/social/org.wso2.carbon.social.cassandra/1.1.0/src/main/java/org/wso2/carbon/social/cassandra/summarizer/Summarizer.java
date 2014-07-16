package org.wso2.carbon.social.cassandra.summarizer;

import com.google.gson.JsonObject;
import org.wso2.carbon.social.core.Activity;

import java.util.Map;

public interface Summarizer {

    boolean add(Activity activity);

    void summarize(JsonObject root, Map<String, Activity> activities);
}
