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
import org.testng.annotations.*;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Create a new asset in publisher and publish it to store
 * Check if it can be seen store side and verify details
 */
public class ESRBACAsStoreUserTestCase extends BaseUITestCase {

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init("superTenant", "storeUser1");

        currentUserName = userInfo.getUserName().split("@")[0];
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
    }

    @Test(groups = "wso2.es.store", description = "verify login to ES Publisher")
    public void testLoginToStore() throws Exception {
        driver.get(baseUrl + STORE_TOP_ASSETS_PAGE);
        driver.findElement(By.cssSelector("span.ro-text")).click();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(currentUserName);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(currentUserPwd);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        assertEquals(currentUserName, driver.findElement(By.xpath
                ("/html/body/div/div[1]/header/div/div[2]/div/a/div/span")).getText().trim(), "Logged in user not shown");
        assertTrue(isElementPresent(driver, By.linkText("My bookmarks")), "Login failed for Store");

    }

    @Test(groups = "wso2.es.publisher", description = "verify not being able to login to publisher")
    public void testRestrictLoginToPublisherAsStoreOnlyUser() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        assertEquals(driver.findElement(By.cssSelector("h3")).getText(), "You do not have permission to login to this" +
                " application.Please contact your administrator and request permission.");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
