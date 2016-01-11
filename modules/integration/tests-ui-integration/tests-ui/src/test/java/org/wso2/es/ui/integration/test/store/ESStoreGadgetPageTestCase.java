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

package org.wso2.es.ui.integration.test.store;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.*;

/**
 * Test appearance and behaviour of Gadget page
 */
public class ESStoreGadgetPageTestCase extends BaseUITestCase {

    private String firstAsset;
    private static final String LINE_PLUS_BAR_CHART = "Line Plus Bar Chart";
    private static final String LINE_CHART = "Line Chart";
    private static final int MAX_POLL_COUNT = 30;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        wait = new WebDriverWait(driver, MAX_DRIVER_WAIT_TIME_SEC);
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, STORE_APP, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.store", description = "Test Gadgets Page")
    public void testGadgetPage() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        WebDriverWait wait = new WebDriverWait(driver, MAX_POLL_COUNT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Gadgets")));
        assertTrue(isElementPresent(driver, By.linkText("Gadgets")), "Gadgets heading missing");
        assertTrue(isElementPresent(driver,By.linkText(LINE_PLUS_BAR_CHART)), LINE_PLUS_BAR_CHART + " Gadgets missing");
        assertTrue(isElementPresent(driver,By.linkText(LINE_CHART)),LINE_CHART + " Gadgets missing");
        assertTrue(isElementPresent(driver,By.cssSelector("span.advanced-search-text")), "Advanced search missing");
        assertTrue(isElementPresent(driver,By.cssSelector("#sortDropdown > img")), "Sorting dropdown missing");
        assertEquals(driver.findElement(By.cssSelector("span.sort-asset-info")).getText(), "( Date/Time Created )");
        driver.findElement(By.cssSelector("#sortDropdown > img")).click();
        assertTrue(isElementPresent(driver,By.linkText("POPULAR")), "Popular sort missing");
        assertTrue(isElementPresent(driver,By.linkText("NAME")), "Sort by name missing");
        assertTrue(isElementPresent(driver,By.linkText("DATE/TIME CREATED")),"Sort by created date/time missing");
    }

    @Test(groups = "wso2.es.store", description = "Test Gadgets Page Links",
            dependsOnMethods = "testGadgetPage")
    public void testSearchFromPage() throws Exception {
        driver.findElement(By.id("search")).click();
        driver.findElement(By.id("search")).clear();
        driver.findElement(By.id("search")).sendKeys("wso2");
        assertTrue(isElementPresent(driver,By.linkText("WSO2 Carbon Commits List Discussion")));
        assertTrue(isElementPresent(driver,By.linkText("WSO2 Architecture List Discussion")));
        assertTrue(isElementPresent(driver,By.linkText("WSO2 Carbon Dev List Discussion")));
        assertEquals(driver.findElement(By.linkText("WSO2 Dev List Discussion")).getText(), "WSO2 Dev List Discussion");
        assertTrue(isElementPresent(driver,By.linkText("WSO2 Dev List Discussion")));
        assertTrue(isElementPresent(driver,By.linkText("WSO2 Jira")));
        driver.findElement(By.id("search")).clear();
        driver.findElement(By.id("search")).sendKeys("pie");
        driver.findElement(By.id("search-button")).click();
        assertTrue(isElementPresent(driver,By.linkText("Pie Chart")));
        assertTrue(isElementPresent(driver,By.id("advanced-search-btn")));
        driver.findElement(By.id("advanced-search-btn")).click();
        assertTrue(isElementPresent(driver,By.cssSelector("div.search-title")));
        assertEquals(driver.findElement(By.cssSelector("div.search-title")).getText(), "Search Gadgets");
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys("chart");
        driver.findElement(By.id("search-button2")).click();
        assertTrue(isElementPresent(driver,By.linkText("Line Plus Bar Chart")));
        assertTrue(isElementPresent(driver,By.linkText("Line Chart")));
        assertTrue(isElementPresent(driver,By.linkText("Stacked Bar Chart")));
        assertTrue(isElementPresent(driver,By.linkText("Pie Chart")));
    }

    @Test(groups = "wso2.es.store", description = "Test Gadgets Page Links",
            dependsOnMethods = "testSearchFromPage")
    public void testLinksFromPage() throws Exception {
        //test links
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        driver.findElement(By.linkText("WSO2 Carbon Commits List Discussion")).click();
        assertEquals(driver.findElement(By.cssSelector("h3")).getText(), "Gadget Details",
                "Cannot reach asset details page from gadget list page");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + STORE_LOGOUT_URL);
        driver.quit();
    }

}