package org.wso2.es.integration.common.utils;

import org.wso2.carbon.automation.engine.extensions.ExecutionListenerExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created with IntelliJ IDEA.
 * User: sameeramwso2com
 * Date: 10/15/14
 * Time: 8:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class ESExtensionManager extends ExecutionListenerExtension {

    private static final Log log = LogFactory.getLog(ESExtensionManager.class);

    @Override
    public void initiate() throws Exception {
        log.info("############################### Initialize called ###################################");
    }

    @Override
    public void onExecutionStart() throws Exception {
        log.info("############################### Execution started ###################################");
    }

    @Override
    public void onExecutionFinish() throws Exception {
        log.info("############################### Execution finished ###################################");
    }
}
