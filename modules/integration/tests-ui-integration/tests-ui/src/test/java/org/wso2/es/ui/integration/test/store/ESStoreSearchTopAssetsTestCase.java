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

import static org.testng.Assert.*;

import org.openqa.selenium.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.ESWebDriver;

public class ESStoreSearchTopAssetsTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String baseUrl;
    private String webApp = "store";
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver();
        baseUrl = getWebAppURL();
        driver.get(baseUrl + "/" + webApp);

    }

    @Test(groups = "wso2.es.store.annon", description = "Test Recent Gadgets")
    public void testESStoreSearchTopAssets() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets?null#");
        driver.findElement(By.id("search")).click();
        driver.findElement(By.id("search")).clear();
        driver.findElement(By.id("search")).sendKeys("wso2");
        driver.findElement(By.cssSelector("i.icon-search")).click();
        assertEquals("WSO2 Carbon Commits List Discussion", driver.findElement(By.cssSelector("h4")).getText());
        assertEquals("WSO2 Architecture List Discussion", driver.findElement(By.xpath("//div[@id='top-asset-slideshow-gadget']/div[2]/div[2]/div/a/h4")).getText());
        assertEquals("WSO2 Carbon Div List Discussion", driver.findElement(By.xpath("//div[@id='top-asset-slideshow-gadget']/div[3]/div[2]/div/a/h4")).getText());
        assertEquals("WSO2 Dev List Discussion", driver.findElement(By.xpath("//div[@id='top-asset-slideshow-gadget']/div[4]/div[2]/div/a/h4")).getText());
        assertEquals(6, driver.findElements(By.cssSelector("div.asset-details")).size());

    }

    @Test(groups = "wso2.es.store.annon", description = "Test Recently added right navigation's results", dependsOnMethods = "testESStoreSearchTopAssets")
    public void testESStoreSearchTopAssetsRecentlyAdded() throws Exception {
        assertEquals("WSO2 Carbon Commits List Discussion", driver.findElement(By.cssSelector("strong > a")).getText());
        assertEquals("WSO2 Architecture List Discussion", driver.findElement(By.xpath("//a[contains(text(),'WSO2 Architecture List Discussion')]")).getText());
        assertEquals("WSO2 Carbon Div List Discussion", driver.findElement(By.xpath("//a[contains(text(),'WSO2 Carbon Div List Discussion')]")).getText());
        assertEquals("WSO2 Dev List Discussion", driver.findElement(By.xpath("//a[contains(text(),'WSO2 Dev List Discussion')]")).getText());
        assertEquals("WSO2 Jira", driver.findElement(By.xpath("//a[contains(text(),'WSO2 Jira')]")).getText());
        assertEquals(6, driver.findElements(By.cssSelector("div.row-fluid.recently-added")).size());

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
