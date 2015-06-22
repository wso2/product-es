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
        baseUrl = getWebAppURL();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(currentUserName);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(currentUserPwd);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
    }

    @Test(groups = "wso2.es.store", description = "Test Bookmarking")
    public void testESStoreBookmarkTestCase() throws Exception {
        driver.get(baseUrl + STORE_TOP_ASSETS_PAGE);
        //select an asset to bookmark and open it
        driver.findElement(By.xpath("/html/body/div/div[3]/div/div[2]/section/div[1]/div/div/div[1]/a")).click();
        String bookmarkedAsset = driver.findElement(By.xpath("/html/body/div/div[3]/div/div[2]/section/div[1]/div/div/div[1]/a")).getText();


        //bookmark the asset
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("btn-add-gadget")));
        driver.findElement(By.id("btn-add-gadget")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("btn-add-gadget"), "Bookmarked"));
        assertEquals("Bookmarked", driver.findElement(By.id("btn-add-gadget")).getText(), "Bookmarking failed");

        //check if shown in My Items page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("myItemsToggle")));
        driver.findElement(By.id("myItemsToggle")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("My bookmarks")));
        assertTrue(isElementPresent(driver, By.linkText("My bookmarks")), "My bookmarks link missing");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.ast-title a.ast-name")));
        assertEquals(bookmarkedAsset, driver.findElement(By.cssSelector("div.ast-title a.ast-name")).getText(),
                "Bookmarked asset not shown in My Items page");

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + STORE_LOGOUT_URL);
        driver.quit();
    }

}
