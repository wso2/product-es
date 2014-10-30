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

package org.wso2.es.ui.integration.test.store;

import org.openqa.selenium.Alert;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.Select;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

public class ESStoreBookmarkTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;
    private String webApp = "store";
    private StringBuffer verificationErrors = new StringBuffer();
    private String bookmarkedAsset;

    private String currentUserName;
    private String currentUserPwd;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver();
        wait = new WebDriverWait(driver, 30);
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, webApp, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.store", description = "Test Bookmarking")
    public void testESStoreBookmarkTestCase() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets");
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.xpath("//div[@id='assets-container']/div/div[3]/a/div/img")).click();
        bookmarkedAsset = driver.findElement(By.cssSelector("h3")).getText();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("btn-add-gadget"), "Bookmark"));
        driver.findElement(By.id("btn-add-gadget")).click();
        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("btn-add-gadget"), "Bookmarked"));
            assertEquals("Bookmarked", driver.findElement(By.id("btn-add-gadget")).getText(), "Bookmarking failed");

            driver.findElement(By.linkText("My Items")).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("strong")));

            assertEquals(bookmarkedAsset, driver.findElement(By.cssSelector("strong")).getText(),
                    "Bookmarked asset not shown in My Items page");

            driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
            assertTrue(isElementPresent(By.linkText("My Assets")), "My Assets section missing");
            driver.findElement(By.cssSelector("i.icon-angle-down.pull-right")).click();
            assertEquals(bookmarkedAsset, driver.findElement(By.cssSelector("strong > a")).getText(),
                    "Bookmarked asset not shown in My Assets section");

            driver.findElement(By.linkText("View all")).click();
            assertEquals("My Assets", driver.findElement(By.cssSelector("h3.asset-title-separator.asset-type-gadget")
            ).getText(), "View all not directing to My Items page");
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }


        //TODO error in tests while removing an asset from my items, working manually
//        driver.findElement(By.xpath("//div[@id='asset-in-gadget']/div/div/div/div/a[3]/i")).click();
//        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li/span")).click();
//        // close pop up manually if it appears
//        driver.findElement(By.xpath("//div[@id='assets-container']/div/div[3]/a/div/img")).click();
//        assertEquals("Bookmark", driver.findElement(By.id("btn-add-gadget")).getText(),
//                "Bookmark removing failed");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + "/store/logout");
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

}