package org.wso2.carbon.social.sql.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.social.sql.service.SQLActivityService;
import org.wso2.carbon.social.core.service.SocialActivityService;

/**
 * Registering {@link SocialActivityService}
 *
 * @scr.component name="org.wso2.carbon.social.component" immediate="true"
 */


public class SQLSocialComponent {

    private static Log log = LogFactory.getLog(SQLSocialComponent.class);

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(SocialActivityService.class, new SQLActivityService(), null);
        log.info("Social Activity service is activated  with SQL Implementation");
    }

}