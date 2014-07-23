package org.wso2.carbon.social.cassandra.service;

import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.social.cassandra.CassandraActivityBrowser;
import org.wso2.carbon.social.cassandra.CassandraActivityPublisher;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.ActivityPublisher;
import org.wso2.carbon.social.core.service.SocialActivityService;

public class CassandraActivityService extends SocialActivityService {

    private ActivityPublisher activityPublisher = new CassandraActivityPublisher();
    private ActivityBrowser activityBrowser = new CassandraActivityBrowser();

    /**
     * Allows an external configuration object to be passed into the Service
     *
     * @param configObject
     */
    public void configPublisher(NativeObject configObject) {
        //TODO: config should happen via core
        ((CassandraActivityPublisher) getActivityPublisher()).parseJSONConfig(configObject);
    }

    @Override
    public ActivityPublisher getActivityPublisher() {
        return activityPublisher;
    }

    @Override
    public ActivityBrowser getActivityBrowser() {
        return activityBrowser;
    }
}
