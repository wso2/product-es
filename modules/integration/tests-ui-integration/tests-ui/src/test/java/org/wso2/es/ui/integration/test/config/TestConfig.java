/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.es.ui.integration.test.config;

import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;

import java.io.File;

public class TestConfig extends ESIntegrationUITest {

    private ServerConfigurationManager serverManager;
    private String resourceLocation;
    private String backendURL;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String superAdminName = "admin";
    private String superAdminPwd = "admin";
    private String superUserName = "testuser11";
    private String superUserPwd = "testuser11";

    private String tenantAdminName = "admin@wso2.com";
    private String tenantAdminPwd = "admin";
    private String tenantUserName = "testuser11@wso2.com";
    private String tenantUserPwd = "testuser11";

    private UserManagementClient userManagementClient;

    @BeforeSuite
    public void configureESTestSuite() throws Exception {
        AutomationContext automationContext = new AutomationContext("ES", TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(automationContext);
        resourceLocation = getResourceLocation();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, superAdminName, superAdminPwd);
        serverManager.applyConfiguration(new File(resourceLocation + File.separator + "notifications" + File
                .separator + "axis2.xml"));
        userManagementClient = new UserManagementClient(backendURL, superAdminName, superAdminPwd);
        userManagementClient.updateUserListOfRole("Internal/publisher", new String[]{superUserName}, null);

        userManagementClient = new UserManagementClient(backendURL, tenantAdminName, tenantAdminPwd);
        userManagementClient.addInternalRole("publisher", new String[]{tenantUserName}, null);

    }
}
