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

package org.wso2.es.ui.integration.test.notifications;

import org.openqa.selenium.By;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.ui.integration.util.AssetUtil;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import java.io.File;

import static org.testng.Assert.assertEquals;

/**
 * Subscription test for Tenant: Tenant Admin & Tenant user
 * test if subscriptions are created on asset creation
 * subscriptions for: LC sate change and Asset update via role profile
 */
public class ESPublisherTenantSubscriptionTestCase extends BaseUITestCase {

    private static final String LC_SUBSCRIPTION = "Store LC State Change Event via Role Profile";
    private static final String UPDATE_SUBSCRIPTION = "Store Asset Update Event via Role Profile";
    private static final String SMTP_PROPERTY_FILE = File.separator + "notifications" + File.separator + "smtp.properties";
    private static final String ASSET_VERSION = "1.0.0";
    private static final String CREATED_TIME = "12";
    private static final String ASSET_TYPE = "gadget";
    private static final int MAX_POLL_COUNT = 30;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String assetName;

    @Factory(dataProvider = "userMode")
    public ESPublisherTenantSubscriptionTestCase(TestUserMode testUserMode, String assetName) {
        this.userMode = testUserMode;
        this.assetName = assetName;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(userMode);
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        baseUrl = getStorePublisherUrl();
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.TENANT_ADMIN);
        adminUserName = automationContext.getContextTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getContextTenant().getTenantAdmin().getPassword();
        providerName = currentUserName.split("@")[0];
        resourcePath = GADGET_REGISTRY_BASE_PATH + this.providerName + "/" + this.assetName + "/" + ASSET_VERSION;
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        String resourceLocation = getResourceLocation();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        smtpPropertyLocation = resourceLocation + SMTP_PROPERTY_FILE;

        ESUtil.login(driver, baseUrl, PUBLISHER_APP, currentUserName, currentUserPwd);
        ESUtil.loginToAdminConsole(driver, baseUrl, adminUserName, adminUserPwd);
    }

    @Test(groups = "wso2.es.notification", description = "Check if subscriptions are created")
    public void testSubscriptionCreation() throws Exception {
        //add new gadget
        AssetUtil.addNewAsset(driver, baseUrl, ASSET_TYPE, providerName, assetName, ASSET_VERSION, CREATED_TIME);
        if (isAlertPresent()) {
            closeAlertAndGetItsText();
        }
        //navigate to admin console
        driver.get(baseUrl + MANAGEMENT_CONSOLE_URL);
        driver.findElement(By.linkText("Gadgets")).click();
        driver.findElementPoll(By.linkText(assetName), MAX_POLL_COUNT);
        driver.findElement(By.linkText(assetName)).click();
        //check two subscriptions
        String subscription1 = driver.findElement(By.cssSelector("#subscriptionsTable > tbody > tr.tableOddRow > td"))
                .getText();
        String subscription2 = driver.findElement(By.xpath("//table[@id='subscriptionsTable']/tbody/tr[3]/td"))
                .getText();
        String subscription1Name;
        String subscription2Name;
        //used to make the test independent of the subscription creation order
        if (LC_SUBSCRIPTION.equalsIgnoreCase(subscription1)) {
            subscription1Name = LC_SUBSCRIPTION;
            subscription2Name = UPDATE_SUBSCRIPTION;
        } else {
            subscription1Name = UPDATE_SUBSCRIPTION;
            subscription2Name = LC_SUBSCRIPTION;
        }
        assertEquals(subscription1Name, subscription1);
        assertEquals(subscription2Name, subscription2);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        //logout, delete gadget and emails
        resourceAdminServiceClient.deleteResource(resourcePath);
        ESUtil.logoutFromAdminConsole(driver, baseUrl);
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        driver.quit();
    }

    @DataProvider(name = "userMode")
    private static Object[][] userModeProvider() {
        return new Object[][]{{TestUserMode.TENANT_ADMIN, "Notification asset - TenantAdmin"},
                {TestUserMode.TENANT_USER, "Notification asset - TenantUser"}};
    }

}
