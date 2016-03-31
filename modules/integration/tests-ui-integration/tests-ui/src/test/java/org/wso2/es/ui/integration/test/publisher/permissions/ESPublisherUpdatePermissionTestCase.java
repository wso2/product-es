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
package org.wso2.es.ui.integration.test.publisher.permissions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.PermissionConstants;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import javax.xml.xpath.XPathExpressionException;

public class ESPublisherUpdatePermissionTestCase extends BaseUITestCase {
    private static final Log LOG = LogFactory.getLog(ESPublisherUpdatePermissionTestCase.class);
    private TestUserMode userMode;
    private User currentUser;
    private String userKey;

    public ESPublisherUpdatePermissionTestCase() throws XPathExpressionException {
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        this.currentUser = automationContext.getSuperTenant().getTenantUser(userKey);
        this.userKey = userKey;
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);

        //Obtain the user credentials
        String username = currentUser.getUserName();
        String password = currentUser.getPassword();

        //Obtain the application details
        baseUrl = getWebAppURL();

        //Create the web driver
        driver = new ESWebDriver(BrowserManager.getWebDriver());

        LOG.info("Accessing login URL " + baseUrl + " app " + PUBLISHER_APP);

        ESUtil.login(driver, baseUrl, PUBLISHER_APP, username, password);
    }

    @Test(groups = "wso2.es.publisher", description = "Test access to the update page")
    public void testAccessToUpdateAssetPage(){
        String url = updateAssetURL(PermissionConstants.TEST_ASSET_TYPE,"id");
        LOG.info("Accessing the update page: "+url);
    }

    @Test(groups = "wso2.es.publisher", description = "Test visibility of the update asset button")
    public void testVisibilityOfUpdateAssetButton(){
        LOG.info("Testing the visibility of the update asset button");
    }

    @DataProvider(name = "userProvider")
    private static Object[][] userProvider() {
        return new Object[][]{{PermissionConstants.PUB_USER_PERM_UPDATE, "Testing ability to update asset with update permission"},
                {PermissionConstants.PUB_USER_PERM_NO_UPDATE, "Testing ability to update without update permission"}};
    }

    private String updateAssetURL(String type,String id){
        return baseUrl + "/" + PUBLISHER_APP + "/asts/" + type + "/update/"+id;
    }
}
