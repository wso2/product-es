/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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

    private static final String USER_1 = "user1";
    private static final String PUBLISHER_ROLE = "publisher";
    private static final String INTERNAL_PUBLISHER_ROLE = "Internal/publisher";
    private static final String AXIS2_CONFIG = File.separator + "notifications" + File.separator + "axis2.xml";

    @BeforeSuite
    public void configureESTestSuite() throws Exception {
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        String superAdminName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        String superAdminPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        String superUserName = automationContext.getSuperTenant().getTenantUser(USER_1).getUserName();
        ServerConfigurationManager serverManager = new ServerConfigurationManager(automationContext);
        String resourceLocation = getResourceLocation();
        String backendURL = automationContext.getContextUrls().getBackEndUrl();
        //restart server with mailto config added in axis2.xml
        serverManager.applyConfiguration(new File(resourceLocation + AXIS2_CONFIG));
        //assign publisher role to the normal user
        UserManagementClient userManagementClient = new UserManagementClient(backendURL, superAdminName, superAdminPwd);
        userManagementClient.updateUserListOfRole(INTERNAL_PUBLISHER_ROLE, new String[]{superUserName}, null);

        automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.TENANT_ADMIN);
        String tenantAdminName = automationContext.getContextTenant().getTenantAdmin().getUserName();
        String tenantAdminPwd = automationContext.getContextTenant().getTenantAdmin().getPassword();
        String tenantUserName = automationContext.getContextTenant().getTenantUser(USER_1).getUserName();
        userManagementClient = new UserManagementClient(backendURL, tenantAdminName, tenantAdminPwd);
        //create a publisher role and assign it to tenant user in the tenant domain
        userManagementClient.addInternalRole(PUBLISHER_ROLE, new String[]{tenantUserName}, null);
    }
}
