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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.AssetUtil;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

public class ESStoreAnonCategorySortingTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String baseUrl;
    private StringBuffer verificationErrors = new StringBuffer();
    private WebDriverWait wait;

    private String popularAsset1;
    private String popularAsset2;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver();
        baseUrl = getWebAppURL();
        wait = new WebDriverWait(driver, 30);

        //Rating two assets to check sorting on popularity
        ESUtil.login(driver, baseUrl, "store", userInfo.getUserName(), userInfo.getPassword());
        driver.get(baseUrl + "/store/asts/gadget/list");
        popularAsset1 = driver.findElement(By.xpath("//div[@id='assets-container']/div/div[4]/div/div/a/h4")).getText();
        driver.findElement(By.xpath("//div[@id='assets-container']/div/div[4]/div/div/a/h4")).click();
        AssetUtil.addRatingsAndReviews(driver, "cool!", "2");
        driver.get(baseUrl + "/store/asts/gadget/list");
        popularAsset2 = driver.findElement(By.xpath("//div[@id='assets-container']/div[2]/div[3]/div/div/a/h4"))
                .getText();
        driver.findElement(By.xpath("//div[@id='assets-container']/div[2]/div[3]/div/div/a/h4")).click();
        AssetUtil.addRatingsAndReviews(driver, "awesome!", "4");
        driver.get(baseUrl + "/store/logout");
    }

    @Test(groups = "wso2.es", description = "Testing sorting")
    public void testStoreSort() throws Exception {
        driver.get(baseUrl + "/store/asts/gadget/list");
        driver.findElement(By.cssSelector("i.icon-star")).click();
        try {
            assertEquals(driver.findElement(By.cssSelector("h4")).getText(), popularAsset1, "Popularity Sort failed");
            assertEquals(driver.findElement(By.xpath("//div[@id='assets-container']/div/div[2]/div/div/a/h4"))
                    .getText(),
                    popularAsset2, "Popularity Sort failed");

            driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
            driver.findElement(By.cssSelector("i.icon-sort-alphabetical")).click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), "Bar Chart"));

            assertEquals("Bar Chart", driver.findElement(By.cssSelector("h4")).getText(),
                    "Alphabetical Sort failed");
            assertEquals("Bubble Chart", driver.findElement(By.xpath
                    ("//div[@id='assets-container']/div/div[2]/div/div/a/h4")).getText(), "Alphabetical Sort failed");

            driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
            driver.findElement(By.cssSelector("i.icon-calendar")).click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"),
                    "WSO2 Carbon Commits List Discussion"));

            //TODO add an asset to sort from created time
//        assertEquals("WSO2 Carbon Commits List Discussion", driver.findElement(By.cssSelector("h4")).getText(),
//                "Recent Sort failed");
//        assertEquals("Line Plus Bar Chart", driver.findElement(By.xpath
//                ("//div[@id='assets-container']/div/div[2]/div/div/a/h4")).getText(), "Recent Sort failed");
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
    }

    @Test(groups = "wso2.es", description = "Testing categories", dependsOnMethods = "testStoreSort")
    public void testCategories() throws Exception {
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.cssSelector("i.icon-caret-down")).click();
        driver.findElement(By.linkText("Google")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), "Bar Chart"));
        try {
            assertEquals(2, driver.findElements(By.cssSelector("div.asset-details")).size(), "Google Category wrong");

            driver.findElement(By.cssSelector("i.icon-caret-down")).click();
            driver.findElement(By.linkText("WSO2")).click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"),
                    "WSO2 Carbon Commits List Discussion"));
            assertEquals(5, driver.findElements(By.cssSelector("div.asset-details")).size(), "WSO2 Category wrong");

            driver.findElement(By.cssSelector("i.icon-caret-down")).click();
            driver.findElement(By.linkText("Templates")).click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), "Line Plus Bar Chart"));
            assertEquals(6, driver.findElements(By.cssSelector("div.span3.asset")).size(),
                    "Templates Category wrong");
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
    }

    @Test(groups = "wso2.es", description = "Testing category drop down", dependsOnMethods = "testCategories")
    public void testCategoryMenu() throws Exception {
        assertEquals("All Categories", driver.findElement(By.cssSelector("div.breadcrumb-head")).getText(),
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
}
