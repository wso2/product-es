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
import static org.testng.Assert.assertTrue;

/**
 * Notification framework test for Super Tenant: Super admin & Super user
 * Adds a new asset
 * Update it & check for notifications
 * Do an LC transition on it & check for notifications
 */
public class ESPublisherNotificationTestCase extends BaseUITestCase {

    private TestUserMode userMode;
    private static final String EMAIL = "esmailsample@gmail.com";
    private static final String EMAIL_PWD = "esMailTest";
    private static final String FIRST_NAME = "name 1";
    private static final String LAST_NAME = "name 2";
    private static final String VERSION = "1.0.0";
    private static final String CREATED_TIME = "12";
    private static final String ASSET_TYPE = "gadget";
    private static final String ASSET_DESCRIPTION = "Test Description";
    private static final String SMTP_PROPERTY_FILE = File.separator + "notifications" + File.separator + "smtp.properties";
    private static final int MAX_POLL_COUNT = 30;
    private String LCNotificationSubject = "[StoreLifecycleStateChange] at path: ";
    private String updateNotificationSubject = "[StoreAssetUpdate] at path: ";
    private String assetName;

    @Factory(dataProvider = "userMode")
    public ESPublisherNotificationTestCase(TestUserMode testUserMode, String assetName) {
        this.userMode = testUserMode;
        this.assetName = assetName;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(userMode);
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        currentUserName = userInfo.getUserName().split("@")[0];
        currentUserPwd = userInfo.getPassword();
        baseUrl = getWebAppURL();
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName().split("@")[0];
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        String resourceLocation = getResourceLocation();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        resourcePath = GADGET_REGISTRY_BASE_PATH + currentUserName + "/" + assetName + "/" + VERSION;
        LCNotificationSubject += resourcePath;
        updateNotificationSubject += resourcePath;
        smtpPropertyLocation = resourceLocation + SMTP_PROPERTY_FILE;

        //Update user profiles through Admin console
        ESUtil.loginToAdminConsole(driver, baseUrl, adminUserName, adminUserPwd);
        ESUtil.setupUserProfile(driver, baseUrl, currentUserName, FIRST_NAME, LAST_NAME, EMAIL);
        //login to publisher & add a new gadget
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, currentUserName, currentUserPwd);
        AssetUtil.addNewAsset(driver, baseUrl, ASSET_TYPE, currentUserName, assetName, VERSION, CREATED_TIME);
    }

    @Test(groups = "wso2.es.notification", description = "Testing mails for LC state change event")
    public void testLCNotification() throws Exception {
        //check notification for initial LC state change
        driver.findElementPoll(By.linkText(assetName), MAX_POLL_COUNT);
        //read email using smtp
        boolean hasMail = ESUtil.containsEmail(smtpPropertyLocation, EMAIL_PWD, EMAIL, LCNotificationSubject);
        assertTrue(hasMail, "LC Notification failed for user:" + currentUserName);
    }

    @Test(groups = "wso2.es.notification", description = "Testing mails for asset update event",
            dependsOnMethods = "testLCNotification")
    public void testUpdateNotification() throws Exception {
        //Update gadget and check lC state change notification
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        AssetUtil.updateAsset(driver, baseUrl, ASSET_TYPE, assetName, ASSET_DESCRIPTION);
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        //read email using smtp
        boolean hasMail = ESUtil.containsEmail(smtpPropertyLocation, EMAIL_PWD, EMAIL, updateNotificationSubject);
        assertTrue(hasMail, "Asset Update Notification failed for user:" + currentUserName);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        //delete gadget and email, logout from admin console and publisher
        resourceAdminServiceClient.deleteResource(resourcePath);
        ESUtil.logoutFromAdminConsole(driver, baseUrl);
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        driver.quit();
    }

    @DataProvider(name = "userMode")
    private static Object[][] userModeProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN, "Notification asset - SuperAdmin"},
                {TestUserMode.SUPER_TENANT_USER, "Notification asset - SuperUser"}};
    }

}