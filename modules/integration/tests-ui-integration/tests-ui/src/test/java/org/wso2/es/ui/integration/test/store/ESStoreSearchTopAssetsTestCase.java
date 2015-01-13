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
import org.wso2.es.ui.integration.util.ESWebDriver;
import static org.testng.Assert.assertEquals;

public class ESStoreSearchTopAssetsTestCase extends BaseUITestCase {

    public static final String WSO2_CARBON_COMMITS_LIST_DISCUSSION = "WSO2 Carbon Commits List " +
            "Discussion";
    public static final String WSO2_ARCHITECTURE_LIST_DISCUSSION = "WSO2 Architecture List " +
            "Discussion";
    public static final String WSO2_CARBON_DIV_LIST_DISCUSSION = "WSO2 Carbon Div List Discussion";
    public static final String WSO2_DEV_LIST_DISCUSSION = "WSO2 Dev List Discussion";

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        driver.get(baseUrl + "/" + STORE_APP);
    }

    @Test(groups = "wso2.es.store.anon", description = "Test Recent Gadgets")
    public void testESStoreSearchTopAssets() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets?null#");
        driver.findElement(By.id("search")).click();
        driver.findElement(By.id("search")).clear();
        driver.findElement(By.id("search")).sendKeys("list");
        driver.findElement(By.cssSelector("i.icon-search")).click();
        assertEquals(WSO2_CARBON_COMMITS_LIST_DISCUSSION, driver.findElement(By.xpath("//h4" +
                "[contains(.,'" + WSO2_CARBON_COMMITS_LIST_DISCUSSION + "')]")).getText());
        assertEquals(WSO2_ARCHITECTURE_LIST_DISCUSSION, driver.findElement(By.xpath("//h4" +
                "[contains(.,'" + WSO2_ARCHITECTURE_LIST_DISCUSSION + "')]")).getText());
        assertEquals(WSO2_CARBON_DIV_LIST_DISCUSSION, driver.findElement(By.xpath("//h4" +
                "[contains(.,'" + WSO2_CARBON_DIV_LIST_DISCUSSION + "')]")).getText());
        assertEquals(WSO2_DEV_LIST_DISCUSSION, driver.findElement(By.xpath("//h4" +
                "[contains(.,'" + WSO2_DEV_LIST_DISCUSSION + "')]")).getText());
        assertEquals(4, driver.findElements(By.cssSelector("div.asset-details")).size(), "Top " +
                "Assets search result count incorrect");

    }

    @Test(groups = "wso2.es.store.anon", description = "Test Recently added right navigation's " +
            "results", dependsOnMethods = "testESStoreSearchTopAssets")
    public void testESStoreSearchTopAssetsRecentlyAdded() throws Exception {
        assertEquals(WSO2_CARBON_COMMITS_LIST_DISCUSSION, driver.findElement(
                By.xpath("//h4[contains(.,'" + WSO2_CARBON_COMMITS_LIST_DISCUSSION + "')]")).getText());
        assertEquals(WSO2_ARCHITECTURE_LIST_DISCUSSION, driver.findElement(
                By.xpath("//a[contains(text(),'WSO2 Architecture List Discussion')]")).getText());
        assertEquals(WSO2_CARBON_DIV_LIST_DISCUSSION, driver.findElement(
                By.xpath("//a[contains(text(),'WSO2 Carbon Div List Discussion')]")).getText());
        assertEquals(WSO2_DEV_LIST_DISCUSSION, driver.findElement(
                By.xpath("//a[contains(text(),'WSO2 Dev List Discussion')]")).getText());
        assertEquals(4, driver.findElements(By.cssSelector("div.row-fluid.recently-added")).size
                (), "Top assets search result recently added count incorrect");

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
