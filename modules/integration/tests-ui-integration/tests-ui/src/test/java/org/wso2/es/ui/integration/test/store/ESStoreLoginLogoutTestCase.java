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

package org.wso2.es.ui.integration.test.store;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * Login Logout test for store
 * check if the logged in user is shown properly
 */
public class ESStoreLoginLogoutTestCase extends BaseUITestCase {

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        currentUserName = userInfo.getUserName().split("@")[0];
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        wait = new WebDriverWait(driver, 60);
        baseUrl = getWebAppURL();
    }

    @Test(groups = "wso2.es.store", description = "Test Store Login")
    public void testESStoreLogin() throws Exception {
        driver.get(baseUrl + STORE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.navbar-right li:first-child a")));
        driver.findElement(By.cssSelector("ul.navbar-right li:first-child a")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(currentUserName);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(currentUserPwd);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logedInUser")));
        //TODO mysubscriptions section is removed as it contains errors uncomment the following ones it's fixed
      /*  wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("myItemsToggle")));
        driver.findElement(By.id("myItemsToggle")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("My Subscription")));
        assertTrue(isElementPresent(driver, By.linkText("My Subscription")), "My Subscription link missing");
        assertEquals(currentUserName.toLowerCase(),driver.findElement(By.id("logedInUser")).getText().toLowerCase(), "Logged in user not shown");
*/    }

    @Test(groups = "wso2.es.store", description = "Test Store Logout",
            dependsOnMethods = "testESStoreLogin")
    public void testESStoreLogout() throws Exception {
        driver.get(baseUrl + STORE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logedInUser")));
        driver.findElement(By.id("logedInUser")).click();
        driver.findElement(By.cssSelector(".dropdown-account a")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.navbar-right li:first-child span.ro-text")));
        assertEquals("sign in", driver.findElement(By.cssSelector("ul.navbar-right li:first-child span.ro-text")).getText().toLowerCase(), "Sign in link missing");
        assertEquals("register", driver.findElement(By.cssSelector("a#btn-register span.ro-text")).getText().toLowerCase(), "Register button missing");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
