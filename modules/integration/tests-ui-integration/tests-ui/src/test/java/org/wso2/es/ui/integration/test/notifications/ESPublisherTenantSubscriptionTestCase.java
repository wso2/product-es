/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.wso2.es.ui.integration.test.notifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.*;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.AssetUtil;
import org.wso2.es.ui.integration.util.ESUtil;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;


public class ESPublisherTenantSubscriptionTestCase extends ESIntegrationUITest {
    private static final Log log = LogFactory.getLog(ESPublisherTenantSubscriptionTestCase.class);
    private WebDriver driver;
    private String baseUrl;
    private String webApp = "publisher";
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    private String LC_SUBSCRIPTION = "Store LC State Change Event via Role Profile";
    private String UPDATE_SUBSCRIPTION = "Store Asset Update Event via Role Profile";
    private String assetName;

    private String adminUserName = "admin@wso2.com";
    private String adminUserPwd = "admin";
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
    public ESPublisherTenantSubscriptionTestCase(String user, String pwd, String assetName) {
        this.currentUserName = user;
        this.currentUserPwd = pwd;
        this.assetName = assetName;
        this.providerName = currentUserName.split("@")[0];
        this.resourcePath = "/_system/governance/gadgets/" + this.providerName + "/" + this.assetName + "/1.0.0";

    }

    @BeforeClass(alwaysRun = true, enabled = true)
    public void setUp() throws Exception {
        log.info("****************** Starting Subscription Test Case for Tenant:"+currentUserName+" **********************");
        super.init();
        driver = BrowserManager.getWebDriver();
        baseUrl = getWebAppURL();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        AutomationContext automationContext = new AutomationContext("ES", TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceLocation = getResourceLocation();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);

        ESUtil.login(driver, baseUrl, webApp, currentUserName, currentUserPwd);
        ESUtil.loginToAdminConsole(driver, baseUrl, adminUserName, adminUserPwd);
    }

    @Test(groups = "wso2.es", description = "Check if subscriptions are created", enabled = true)
    public void testSubscriptionCreation() throws Exception {
        log.info("----------------------------- Subscription Test ----------------------------------------");
        AssetUtil.addNewAsset(driver, baseUrl, "gadget", providerName, assetName, "1.0.0", "12");
        if (isAlertPresent()) {
            closeAlertAndGetItsText();
        }
        driver.get(baseUrl + "/carbon/");
        do {
            driver.findElement(By.linkText("Gadgets")).click();
        } while (!isElementPresent(By.linkText(assetName)));
        driver.findElement(By.linkText(assetName)).click();
        String subscription1 = driver.findElement(By.cssSelector("#subscriptionsTable > tbody > tr" +
                ".tableOddRow > td"))
                .getText();
        String subscription2 = driver.findElement(By.xpath("//table[@id='subscriptionsTable']/tbody/tr[3]/td")).getText
                ();
        String subscription1Name;
        String subscription2Name;
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

    @AfterClass(alwaysRun = true, enabled = true)
    public void tearDown() throws Exception {
        resourceAdminServiceClient.deleteResource(resourcePath);
        ESUtil.logoutFromAdminConsole(driver, baseUrl);
        ESUtil.logout(driver, baseUrl, "publisher", providerName);
        ESUtil.deleteAllEmail(resourceLocation + File.separator + "notifications" + File.separator + "smtp" +
                ".properties", emailPwd, email);
        driver.close();
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
        log.info("************* Finishing Notification Test Case for Tenant:"+currentUserName+" *******************");
    }

    @DataProvider(name = "userMode")
    public static Object[][] userInfoProvider() {
        return new Object[][]{{"admin@wso2.com", "admin", "Subscription asset - TenantAdmin"}, {"testuser11@wso2.com",
                "testuser11", "Subscription asset - TenantUser"}};
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
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
