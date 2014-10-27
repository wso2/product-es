/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.es.ui.integration.test.common;

import org.openqa.selenium.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.ESWebDriver;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ESRegisterUserTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private UserManagementClient userManagementClient;
    private String backendURL;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver();
        baseUrl = getWebAppURL();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        AutomationContext automationContext = new AutomationContext("ES", TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        userManagementClient = new UserManagementClient(backendURL, userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = "wso2.es.common", description = "Testing user registration")
    public void testESRegisterUserTestCase() throws Exception {
        driver.get(baseUrl + "/store");
        driver.findElement(By.id("btn-register")).click();
        driver.findElement(By.id("reg-username")).clear();
        driver.findElement(By.id("reg-username")).sendKeys("testusernew");
        driver.findElement(By.id("reg-password")).clear();
        driver.findElement(By.id("reg-password")).sendKeys("testusernew");
        driver.findElement(By.id("reg-password2")).clear();
        driver.findElement(By.id("reg-password2")).sendKeys("testusernew");
        driver.findElement(By.id("registrationSubmit")).click();
        try {
            assertTrue(isElementPresent(By.linkText("My Items")));
            assertTrue(isElementPresent(By.linkText("testusernew")));
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
        driver.get(baseUrl + "/publisher");
        try {
            assertTrue(isElementPresent(By.linkText("testusernew")));
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + "/publisher/logout");
        userManagementClient.deleteUser("testusernew");
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
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