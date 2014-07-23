package org.wso2.carbon.social.cassandra.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.social.cassandra.service.CassandraActivityService;
import org.wso2.carbon.social.core.service.SocialActivityService;

/**
 * Registering {@link SocialActivityService}
 *
 * @scr.component name="org.wso2.carbon.social.component" immediate="true"
 */


public class CassandraSocialComponent {

    private static Log log = LogFactory.getLog(CassandraSocialComponent.class);

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(SocialActivityService.class, new CassandraActivityService(), null);
        log.info("Social Activity service is activated  with Cassandra Implementation");
    }

}