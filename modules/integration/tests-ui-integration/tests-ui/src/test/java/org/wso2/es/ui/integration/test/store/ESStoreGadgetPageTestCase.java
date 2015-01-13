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

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, STORE_APP, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.store", description = "Test Gadgets Page")
    public void testGadgetPage() throws Exception {
        //test appearance of gadget page
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        assertEquals("Gadget", driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li"))
                .getText(), "Gadget Menu missing");
        assertEquals(LINE_PLUS_BAR_CHART, driver.findElement(By.xpath("//a[contains(text()," +
                "'Line Plus Bar Chart')]")).getText(), "Gadgets missing");
        firstAsset = driver.findElement(By.cssSelector("h4")).getText();
        assertEquals("Recently Added", driver.findElement(By.xpath
                ("//div[@id='container-assets']/div/div[2]/div[1]/div/h4")).getText(),
                "Recently Added section missing");
        assertEquals(LINE_PLUS_BAR_CHART, driver.findElement(By.xpath("//a[contains(.,'Line Plus Bar Chart')]"))
                .getText(), "Recently added Gadgets missing");
        assertEquals("Tags", driver.findElement(By.xpath("//div[@id='container-assets']/div/div[2]/div[2]/div/h4"))
                .getText(), "Tags section missing");
        assertTrue(isElementPresent(By.linkText("charts")), "Tags missing (charts tag)");
        assertEquals("All Categories", driver.findElement(By.cssSelector("div.breadcrumb-head")).getText(),
                "Category drop down missing");
        assertTrue(isElementPresent(By.cssSelector("i.icon-star")), "Popularity sort missing");
        assertTrue(isElementPresent(By.cssSelector("i.icon-sort-alphabetical")), "Alphabetical sort missing");
        assertTrue(isElementPresent(By.cssSelector("i.icon-calendar")), "Recent sort missing");
        assertTrue(isElementPresent(By.id("search")), "Search tray missing");
    }

    @Test(groups = "wso2.es.store", description = "Test Gadgets Page Links",
            dependsOnMethods = "testGadgetPage")
    public void testLinksFromPage() throws Exception {
        //test links
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        assertEquals(firstAsset, driver.findElement(By.cssSelector("h4")).getText(),
                "Cannot view selected Gadget's page through Gadget list");
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.xpath("//a[contains(text(),'Line Chart')]")).click();
        assertEquals(LINE_CHART, driver.findElement(By.cssSelector("h3")).getText(),
                "Cannot view selected Gadget's page through Recently added list");

        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li"))
                .click();
        driver.findElement(By.linkText("pie")).click();
        assertEquals(1, driver.findElements(By.cssSelector("div.span3.asset")).size(),
                "Tags not working");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + STORE_LOGOUT_URL);
        driver.quit();
    }

}
