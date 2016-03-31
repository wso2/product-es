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

package org.wso2.es.ui.integration.test.store.permissions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ESStoreListPermissionTestCase extends BaseUITestCase{
    private static final Log LOG = LogFactory.getLog(ESStoreListPermissionTestCase.class);
    private User currentUser;
    private String userKey;

    @Factory(dataProvider = "userProvider")
    public ESStoreListPermissionTestCase(String userKey, String scenario) throws XPathExpressionException {
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

        LOG.info("Accessing login URL " + baseUrl + " app " + STORE_APP);

        ESUtil.login(driver, baseUrl, STORE_APP, username, password);
    }

    @Test(groups = "wso2.es.store", description = "Test accessing listing page")
    public void testAccessToAssetListingPage() {
        String url = assetListingURL(PermissionConstants.TEST_ASSET_TYPE);
        driver.get(url);
        if (this.userKey == PermissionConstants.PUB_USER_PERM_LIST) {
            //Should see the gadget listing title
            assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("Gadgets"),"Unable to navigate to " +
                    "gadget listing page");
        } else if (this.userKey == PermissionConstants.PUB_USER_PERM_NO_LIST) {
            //Should see a 401 page
            assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("HTTP Status 401 - You do not " +
                    "have access to this page"),"User is able to access the gadget listing page even without permission");
        }
    }

    @Test(groups = "wso2.es.store", description = "Test visibility of asset type in asset corral")
    public void testVisibilityOfAssetTypeInCorral(){
        //Navigate to the landing page and check if there is a gadget in the corral
        String url = landingPageURL();
        driver.get(url);
        assertTrue(isElementPresent(driver,By.cssSelector("span.btn-asset")),"The asset type corral is not present");
        if (this.userKey == PermissionConstants.PUB_USER_PERM_LIST) {
            //The gadget type must be present in the corral
            assertTrue(isElementPresent(driver,By.linkText("Gadget")),"The gadget type is not listed in the corral");
        } else if (this.userKey == PermissionConstants.PUB_USER_PERM_NO_LIST) {
            //The gadget type must not be present in the corral
            assertFalse(isElementPresent(driver,By.linkText("Gadget")),"The gadget type should not appear in the corral");
        }
    }

    @DataProvider(name = "userProvider")
    private static Object[][] userProvider() {
        return new Object[][]{{PermissionConstants.STORE_USER_PERM_LIST, "Testing access to /list with list permission"},
                {PermissionConstants.STORE_USER_PERM_NO_LIST, "Testing access to /list without list permission"}};
    }

    private String assetListingURL(String type) {
        return baseUrl + "/" + STORE_APP + "/asts/" + type + "/list";
    }

    private String landingPageURL(){
        return baseUrl + "/" + STORE_APP;
    }
}
