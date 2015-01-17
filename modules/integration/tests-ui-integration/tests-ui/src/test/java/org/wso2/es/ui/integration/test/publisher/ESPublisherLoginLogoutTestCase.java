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

package org.wso2.es.ui.integration.test.publisher;

import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Login Logout test for publisher
 * check if the logged in user is shown properly
 */
public class ESPublisherLoginLogoutTestCase extends BaseUITestCase {

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
    }

    @Test(groups = "wso2.es.publisher", description = "verify login to ES Publisher")
    public void testLoginToPublisher() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(currentUserName);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(currentUserPwd);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        assertEquals("Asset | WSO2 Enterprise Store back-office", driver.getTitle());
        assertTrue(isElementPresent(By.linkText(currentUserName)), "Logged in user not shown");
    }

    @Test(groups = "wso2.es.publisher", description = "verify login to ES Publisher",
            dependsOnMethods = "testLoginToPublisher")
    public void testLogoutFromPublisher() throws Exception {
        driver.findElement(By.linkText(currentUserName)).click();
        driver.findElement(By.linkText("Sign out")).click();
        assertTrue(isElementPresent(By.id("username")), "Not redirected to login view");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
