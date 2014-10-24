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

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

import org.openqa.selenium.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;


public class ESStoreAnonCategorySortingTestCase extends ESIntegrationUITest {
    private WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        baseUrl = getWebAppURL();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test(groups = "wso2.es", description = "Testing sorting")
    public void testStoreSort() throws Exception {
        driver.get(baseUrl + "/store/asts/gadget/list");
        driver.findElement(By.cssSelector("i.icon-star")).click();
        assertEquals("Line Plus Bar Chart", driver.findElement(By.cssSelector("h4")).getText(),
                "Popularity Sort failed");
        assertEquals("Line Chart", driver.findElement(By.xpath
                ("//div[@id='assets-container']/div/div[2]/div/div/a/h4")).getText(), "Popularity Sort failed");

        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.cssSelector("i.icon-sort-alphabetical")).click();
        assertEquals("Line Plus Bar Chart", driver.findElement(By.cssSelector("h4")).getText(),
                "Alphabetical Sort failed");
        assertEquals("Line Chart", driver.findElement(By.xpath
                ("//div[@id='assets-container']/div/div[2]/div/div/a/h4")).getText(), "Alphabetical Sort failed");

        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.cssSelector("i.icon-calendar")).click();
        assertEquals("Line Plus Bar Chart", driver.findElement(By.cssSelector("h4")).getText(),
                "Recent Sort failed");
        assertEquals("Line Chart", driver.findElement(By.xpath
                ("//div[@id='assets-container']/div/div[2]/div/div/a/h4")).getText(), "Recent Sort failed");
    }

    @Test(groups = "wso2.es", description = "Testing categories", dependsOnMethods = "testStoreSort")
    public void testCategories() throws Exception {
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.cssSelector("i.icon-caret-down")).click();
        driver.findElement(By.linkText("Google")).click();
        assertEquals(2, driver.findElements(By.cssSelector("div.asset-details")).size(), "Google Category wrong");

        driver.findElement(By.cssSelector("i.icon-caret-down")).click();
        driver.findElement(By.linkText("WSO2")).click();
        assertEquals(5, driver.findElements(By.cssSelector("div.asset-details")).size(), "WSO2 Category wrong");

        driver.findElement(By.cssSelector("i.icon-caret-down")).click();
        driver.findElement(By.linkText("Templates")).click();
        assertEquals(6, driver.findElements(By.cssSelector("div.span3.asset")).size(),
                "Templates Category wrong");
    }

    @Test(groups = "wso2.es", description = "Testing category drop down", dependsOnMethods = "testCategories")
    public void testCategoryMenu() throws Exception {
        assertEquals("Templates", driver.findElement(By.cssSelector("div.breadcrumb-head")).getText(),
                "Category drop down not showing selected category ");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
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
