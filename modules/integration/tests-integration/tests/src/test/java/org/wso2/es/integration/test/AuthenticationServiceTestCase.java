/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.es.integration.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.es.integration.common.utils.ESIntegrationTest;

public class AuthenticationServiceTestCase extends ESIntegrationTest {

    @Test(groups = "wso2.es")
    public void loginTest() throws Exception{
        esContext = new AutomationContext(PRODUCT_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(esContext);
        sessionCookie = loginLogoutClient.login();
        Assert.assertTrue(sessionCookie.contains("JSESSIONID="), "JSESSIONID= not found");
    }
}
