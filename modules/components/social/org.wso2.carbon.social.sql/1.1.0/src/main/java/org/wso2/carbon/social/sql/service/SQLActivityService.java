package org.wso2.carbon.social.sql.service;


import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.social.core.ActivityBrowser;
import org.wso2.carbon.social.core.ActivityPublisher;
import org.wso2.carbon.social.core.service.SocialActivityService;
import org.wso2.carbon.social.sql.SQLActivityBrowser;
import org.wso2.carbon.social.sql.SQLActivityPublisher;

public class SQLSocialActivityService extends SocialActivityService {
    private ActivityPublisher activityPublisher = new SQLActivityPublisher();
    private ActivityBrowser activityBrowser = new SQLActivityBrowser();

    @Override
    public ActivityBrowser getActivityBrowser() {
        return activityBrowser;
    }

    @Override
    public ActivityPublisher getActivityPublisher() {
        return activityPublisher; 
    }

    @Override
    public void configPublisher(NativeObject configObject) {
    }
}
