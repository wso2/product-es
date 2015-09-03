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

package org.wso2.es.ui.integration.test.common;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Register a new user for ES
 * check login for store and publisher
 */
public class ESRegisterUserTestCase extends BaseUITestCase {

    private UserManagementClient userManagementClient;
    private static final String NEW_USER_NAME = "zeetestusernew";
    private static final String NEW_USER_PWD = "qwe123Q!";
    private static final String NEW_USER_FNAME = "test";
    private static final String NEW_USER_LNAME = "user";
    private static final String NEW_USER_EMAIL = "zeetestusernew@gmail.com";
    private static final String SECRET_QUESTION = "Favorite food ?";
    private static final String SECRET_ANSWER = "Ice cream";
    private static final int MAX_POLL_COUNT = 30;


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        userManagementClient = new UserManagementClient(backendURL, userInfo.getUserName(), userInfo.getPassword());

        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
    }

    @Test(groups = "wso2.es.common", description = "Testing user registration")
    public void testESRegisterUserTestCase() throws Exception {
        //Register new user
        driver.get(baseUrl + STORE_URL);
        driver.findElement(By.id("btn-register")).click();
        driver.findElement(By.id("reg-username")).clear();
        driver.findElement(By.id("reg-username")).sendKeys(NEW_USER_NAME);
        driver.findElement(By.id("reg-password")).clear();
        driver.findElement(By.id("reg-password")).sendKeys(NEW_USER_PWD);
        driver.findElement(By.id("reg-password2")).clear();
        driver.findElement(By.id("reg-password2")).sendKeys(NEW_USER_PWD);

        driver.findElement(By.name("reg-email")).clear();
        driver.findElement(By.name("reg-email")).sendKeys(NEW_USER_EMAIL);
        driver.findElement(By.name("reg-first-name")).clear();
        driver.findElement(By.name("reg-first-name")).sendKeys(NEW_USER_FNAME);
        driver.findElement(By.name("reg-last-name")).clear();
        driver.findElement(By.name("reg-last-name")).sendKeys(NEW_USER_LNAME);
        driver.findElement(By.id("registrationSubmit")).click();
        // check the success message
        assertTrue(isElementPresent(driver,By.id("regFormSuc")),"User creation failed.");
        driver.findElement(By.id("signInLink")).click();

        driver.findElementPoll(By.id("username"), MAX_POLL_COUNT);
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(NEW_USER_NAME);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(NEW_USER_PWD);
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        WebElement userNameElement = driver.findElement(By.id("logedInUser"));

        assertEquals(userNameElement.getText().trim(), NEW_USER_NAME);

        //check login for store
        assertTrue(isElementPresent(driver, By.linkText("My bookmarks")), "Login failed for Store");

        //Checking claim from carbon console.
        driver.get(baseUrl + MANAGEMENT_CONSOLE_URL);
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(adminUserName);
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(adminUserPwd);

        driver.findElement(By.cssSelector("input.button")).click();
        driver.findElement(By.linkText("Users and Roles")).click();
        driver.findElement(By.linkText("Users")).click();
        driver.findElement(By.cssSelector("#userTable tbody tr:last-child td:nth-child(2) a:nth-child(5)")).click();
        driver.findElement(By.linkText("default")).click();
        assertEquals(driver.findElement(By.cssSelector(".styledLeft tr:nth-child(7) input")).getAttribute("value"), NEW_USER_EMAIL , "Claims are not working properly.");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        //logout and delete new user
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        userManagementClient.deleteUser(NEW_USER_NAME);
        driver.quit();
    }

}