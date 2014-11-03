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

package org.wso2.es.ui.integration.test.notifications;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.AssetUtil;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;
import java.io.File;
import static org.testng.Assert.assertEquals;

/**
 * Subscription test for Tenant: Tenant Admin & Tenant user
 * test if subscriptions are created on asset creation
 * subscriptions for: LC sate change and Asset update via role profile
 */
public class ESPublisherTenantSubscriptionTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String baseUrl;
    private String webApp = "publisher";
    private boolean acceptNextAlert = true;

    private String LC_SUBSCRIPTION = "Store LC State Change Event via Role Profile";
    private String UPDATE_SUBSCRIPTION = "Store Asset Update Event via Role Profile";
    private String assetName;

    private String adminUserName;
    private String adminUserPwd;
    private String providerName;

    private String currentUserName;
    private String currentUserPwd;
    private String resourcePath;

    private String backendURL;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String resourceLocation;

    private String email = "esmailsample@gmail.com";
    private String emailPwd = "esMailTest";

    @Factory(dataProvider = "userMode")
    public ESPublisherTenantSubscriptionTestCase(TestUserMode testUserMode, String assetName) {
        this.userMode = testUserMode;
        this.assetName = assetName;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(userMode);
        driver = new ESWebDriver();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        baseUrl = getStorePublisherUrl();
        AutomationContext automationContext = new AutomationContext("ES",
                TestUserMode.TENANT_ADMIN);
        adminUserName = automationContext.getContextTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getContextTenant().getTenantAdmin().getPassword();
        providerName = currentUserName.split("@")[0];
        resourcePath = "/_system/governance/gadgets/" + this.providerName + "/" + this.assetName
                + "/1.0.0";
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceLocation = getResourceLocation();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName,
                adminUserPwd);

        ESUtil.login(driver, baseUrl, webApp, currentUserName, currentUserPwd);
        ESUtil.loginToAdminConsole(driver, baseUrl, adminUserName, adminUserPwd);
    }

    @Test(groups = "wso2.es.notification", description = "Check if subscriptions are created")
    public void testSubscriptionCreation() throws Exception {
        //add new gadget
        AssetUtil.addNewAsset(driver, baseUrl, "gadget", providerName, assetName, "1.0.0", "12");
        if (isAlertPresent()) {
            closeAlertAndGetItsText();
        }
        //navigate to admin console
        driver.get(baseUrl + "/carbon/");
        driver.findElement(By.linkText("Gadgets")).click();
        driver.findElementPoll(By.linkText(assetName), 30);
        driver.findElement(By.linkText(assetName)).click();
        //check two subscriptions
        String subscription1 = driver.findElement(By.cssSelector("#subscriptionsTable > tbody > " +
                "tr.tableOddRow > td")).getText();
        String subscription2 = driver.findElement(By.xpath
                ("//table[@id='subscriptionsTable']/tbody/tr[3]/td")).getText();
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
        driver.get(baseUrl + "/publisher/logout");
        ESUtil.deleteAllEmail(resourceLocation + File.separator + "notifications" + File
                .separator + "smtp.properties", emailPwd, email);
        driver.quit();
    }

    @DataProvider(name = "userMode")
    private static Object[][] userModeProvider() {
        return new Object[][]{{TestUserMode.TENANT_ADMIN, "Notification asset - TenantAdmin"},
                {TestUserMode.TENANT_USER, "Notification asset - TenantUser"}};
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    private String closeAlertAndGetItsText() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (acceptNextAlert) {
                alert.accept();
            } else {
                alert.dismiss();
            }
            return alertText;
        } finally {
            acceptNextAlert = true;
        }
    }

}
