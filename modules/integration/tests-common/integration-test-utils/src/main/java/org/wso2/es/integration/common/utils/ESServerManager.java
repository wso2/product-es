/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.es.integration.common.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.*;
import org.wso2.carbon.integration.common.extensions.utils.ExtensionCommonConstants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ESServerManager {
    private CarbonServerManager carbonServer;
    private String carbonZip;
    private int portOffset;
    private Map<String, String> commandMap = new HashMap<String, String>();
    private static final Log log = LogFactory.getLog(TestServerManager.class);
    private String carbonHome;

    protected ESServerManager(AutomationContext context) {
        carbonServer = new CarbonServerManager(context);
    }

    protected ESServerManager(AutomationContext context, String carbonZip) {
        carbonServer = new CarbonServerManager(context);
        this.carbonZip = carbonZip;
    }

    protected ESServerManager(AutomationContext context, int portOffset) {
        carbonServer = new CarbonServerManager(context);
        this.portOffset = portOffset;
    }

    protected ESServerManager(AutomationContext context, String carbonZip, Map<String, String> commandMap) {
        carbonServer = new CarbonServerManager(context);
        this.carbonZip = carbonZip;
        if (commandMap.get(ExtensionCommonConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND) != null) {
            this.portOffset = Integer.parseInt(commandMap.get(ExtensionCommonConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND));
        } else {
            throw new IllegalArgumentException("portOffset value must be set in command list");
        }
//        this.portOffset = portOffset;
        this.commandMap = commandMap;
    }

    public String getCarbonZip() {
        return carbonZip;
    }

    public String getCarbonHome() {
        return carbonHome;
    }

    public Map<String, String> getCommands() {
        return commandMap;
    }

    /**
     * This method is called for starting a Carbon server in preparation for execution of a
     * TestSuite
     * <p/>
     * Add the @BeforeSuite TestNG annotation in the method overriding this method
     *
     * @return The CARBON_HOME
     * @throws java.io.IOException If an error occurs while copying the deployment artifacts into the
     *                             Carbon server
     */
    protected String startServer() throws Exception {
        if (carbonZip == null) {
            carbonZip = System.getProperty(FrameworkConstants.SYSTEM_PROPERTY_CARBON_ZIP_LOCATION);
        }
        if (carbonZip == null) {
            throw new IllegalArgumentException("carbon zip file cannot find in the given location");
        }
        carbonHome = carbonServer.setUpCarbonHome(carbonZip);
        log.info("Carbon Home - " + carbonHome);
        setupTestResources();
        carbonServer.startServerUsingCarbonHome(carbonHome, commandMap);
        return carbonHome;
    }

    /**
     * This method is called for stopping a Carbon server
     * <p/>
     * Add the @AfterSuite annotation in the method overriding this method
     *
     * @throws Exception If an error occurs while shutting down the server
     */
    protected void stopServer() throws Exception {
        carbonServer.serverShutdown(portOffset);
    }

    protected void setupTestResources() throws Exception {
        log.info("Reading test manifest (test_manifest.json) and copying test resources...");
        ManifestManager manager=new ManifestManager(carbonHome);
        manager.execute();
        log.info("Finished copying test resources");
    }
}
