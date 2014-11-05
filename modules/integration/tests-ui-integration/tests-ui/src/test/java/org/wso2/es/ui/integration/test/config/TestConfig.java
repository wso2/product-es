/*
 * Copyright (c) 2014, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.wso2.es.ui.integration.util.BaseUITestCase;

import java.io.File;

/**
 * Restarts server before the test suite
 * Add a new role for publisher in tenant domain
 * Assign publisher role to test users
 */
public class TestConfig extends BaseUITestCase {

    private ServerConfigurationManager serverManager;
    private String resourceLocation;
    private String backendURL;
    private String superAdminName;
    private String superAdminPwd;
    private String superUserName;

    private String tenantAdminName;
    private String tenantAdminPwd;
    private String tenantUserName;

    private UserManagementClient userManagementClient;

    @BeforeSuite
    public void configureESTestSuite() throws Exception {
        AutomationContext automationContext = new AutomationContext("ES",
                TestUserMode.SUPER_TENANT_ADMIN);
        superAdminName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        superAdminPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        superUserName = automationContext.getSuperTenant().getTenantUser("user1").getUserName();
        serverManager = new ServerConfigurationManager(automationContext);
        resourceLocation = getResourceLocation();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        //restart server with mailto config added in axis2.xml
        serverManager.applyConfiguration(new File(resourceLocation + File.separator +
                "notifications" + File.separator + "axis2.xml"));
        //assign publisher role to the normal user
        userManagementClient = new UserManagementClient(backendURL, superAdminName, superAdminPwd);
        userManagementClient.updateUserListOfRole("Internal/publisher",
                new String[]{superUserName}, null);

        automationContext = new AutomationContext("ES", TestUserMode.TENANT_ADMIN);
        tenantAdminName = automationContext.getContextTenant().getTenantAdmin().getUserName();
        tenantAdminPwd = automationContext.getContextTenant().getTenantAdmin().getPassword();
        tenantUserName = automationContext.getContextTenant().getTenantUser("user1").getUserName();
        userManagementClient = new UserManagementClient(backendURL, tenantAdminName,
                tenantAdminPwd);
        //create a publisher role and assign it to tenant user in the tenant domain
        userManagementClient.addInternalRole("publisher", new String[]{tenantUserName}, null);
    }
}
