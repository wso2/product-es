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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Bookmark functionality test
 */
public class ESStoreBookmarkTestCase extends BaseUITestCase {

    private static final int WAIT_TIME = 30;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        wait = new WebDriverWait(driver, WAIT_TIME);
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, STORE_APP, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.store", description = "Test Bookmarking")
    public void testESStoreBookmarkTestCase() throws Exception {
        driver.get(baseUrl + STORE_TOP_ASSETS_PAGE);
        driver.findElement(By.xpath("//i[@class='icon-cog']")).click();
        //select an asset to bookmark and open it
        String bookmarkedAsset = driver.findElement(By.xpath("//div[@id='assets-container']/div/div[1]/div/div/a/h4"))
                .getText();
        driver.findElement(By.xpath("//div[@id='assets-container']/div/div[1]/div/div/a/h4")).click();
        if (isElementPresent(By.linkText("Sign in"))) {
            driver.findElement(By.linkText("Sign in")).click();
        }
        //bookmark the asset
        driver.findElement(By.id("btn-add-gadget")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("btn-add-gadget"), "Bookmarked"));
        assertEquals("Bookmarked", driver.findElement(By.id("btn-add-gadget")).getText(), "Bookmarking failed");

        //check if shown in My Items page
        driver.findElement(By.linkText("My Items")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("strong")));
        assertEquals(bookmarkedAsset, driver.findElement(By.cssSelector("strong")).getText(),
                "Bookmarked asset not shown in My Items page");

        //check if shown in My assets section
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        assertTrue(isElementPresent(By.linkText("My Assets")), "My Assets section missing");
        driver.findElement(By.cssSelector("i.icon-angle-down.pull-right")).click();
        assertEquals(bookmarkedAsset, driver.findElement(By.cssSelector("strong > a")).getText(),
                "Bookmarked asset not shown in My Assets section");

        driver.findElement(By.linkText("View all")).click();
        assertEquals("My Assets", driver.findElement(By.cssSelector("h3.asset-title-separator.asset-type-gadget"))
                .getText(), "View all not directing to My Items page");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + STORE_LOGOUT_URL);
        driver.quit();
    }

}