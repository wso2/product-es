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

import org.openqa.selenium.By;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.AssetUtil;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;
import java.io.File;
import static org.testng.Assert.assertTrue;

/**
 * Notification framework test for Super Tenant: Super admin & Super user
 * Adds a new asset
 * Update it & check for notifications
 * Do an LC transition on it & check for notifications
 */
public class ESPublisherNotificationTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String baseUrl;
    private String publisherApp = "publisher";
    private String resourceLocation;
    private String backendURL;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private String assetName;
    private TestUserMode userMode;
    private String email = "esmailsample@gmail.com";
    private String emailPwd = "esMailTest";

    private String currentUserName;
    private String currentUserPwd;
    private String adminUserName;
    private String adminUserPwd;

    private String resourcePath;

    private String LCNotificationSubject;
    private String updateNotificationSubject;

    @Factory(dataProvider = "userMode")
    public ESPublisherNotificationTestCase(TestUserMode testUserMode, String assetName) {
        this.userMode = testUserMode;
        this.assetName = assetName;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(userMode);
        driver = new ESWebDriver();
        currentUserName = userInfo.getUserName().split("@")[0];
        currentUserPwd = userInfo.getPassword();
        baseUrl = getWebAppURL();
        AutomationContext automationContext = new AutomationContext("ES",
                TestUserMode.SUPER_TENANT_ADMIN);
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName().split
                ("@")[0];
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        resourceLocation = getResourceLocation();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName,
                adminUserPwd);
        resourcePath = "/_system/governance/gadgets/" + this.currentUserName + "/" + this
                .assetName + "/1.0.0";
        LCNotificationSubject = "[StoreLifecycleStateChange] at path: " + this.resourcePath;
        updateNotificationSubject = "[StoreAssetUpdate] at path: " + this.resourcePath;

        //Update user profiles through Admin console
        ESUtil.loginToAdminConsole(driver, baseUrl, adminUserName, adminUserPwd);
        ESUtil.setupUserProfile(driver, baseUrl, currentUserName, "firstName", "lastName",
                "esmailsample@gmail.com");
        //login to publisher & add a new gadget
        ESUtil.login(driver, baseUrl, publisherApp, currentUserName, currentUserPwd);
        AssetUtil.addNewAsset(driver, baseUrl, "gadget", currentUserName, assetName, "1.0.0", "12");
    }

    @Test(groups = "wso2.es.notification", description = "Testing mails for LC state change " +
            "event")
    public void testLCNotification() throws Exception {
        //check notification for initial LC state change
        driver.findElementPoll(By.linkText(assetName), 30);
        //read email using smtp
        boolean hasMail = ESUtil.containsEmail(resourceLocation + File.separator + "notifications"
                + File.separator + "smtp.properties", emailPwd, email, LCNotificationSubject);
        assertTrue(hasMail, "LC Notification failed for user:" + currentUserName);
    }

    @Test(groups = "wso2.es.notification", description = "Testing mails for asset update " +
            "event", dependsOnMethods = "testLCNotification")
    public void testUpdateNotification() throws Exception {
        //Update gadget and check lC state change notification
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        AssetUtil.updateAsset(driver, baseUrl, "gadget", assetName, "Test Description");
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        //read email using smtp
        boolean hasMail = ESUtil.containsEmail(resourceLocation + File.separator +
                "notifications" + File.separator + "smtp.properties", emailPwd, email,
                updateNotificationSubject);
        assertTrue(hasMail, "Asset Update Notification failed for user:" + currentUserName);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        //delete gadget and email, logout from admin console and publisher
        resourceAdminServiceClient.deleteResource(resourcePath);
        ESUtil.logoutFromAdminConsole(driver, baseUrl);
        driver.get(baseUrl + "/publisher/logout");
        ESUtil.deleteAllEmail(resourceLocation + File.separator + "notifications" + File
                .separator + "smtp" + ".properties", emailPwd, email);
        driver.quit();
    }

    @DataProvider(name = "userMode")
    private static Object[][] userModeProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN, "Notification asset - SuperAdmin"},
                {TestUserMode.SUPER_TENANT_USER, "Notification asset - SuperUser"}};
    }

}