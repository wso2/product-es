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

    private static final int MAX_POLL_COUNT = 30;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        wait = new WebDriverWait(driver,MAX_POLL_COUNT);
        currentUserName = userInfo.getUserName().split("@")[0];
        currentUserPwd = userInfo.getPassword();
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, STORE_APP, currentUserName, currentUserPwd);


    }

    @Test(groups = "wso2.es.store", description = "Test Bookmarking")
    public void testESStoreBookmarkTestCase() throws Exception {

        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        //get the first element from the gadget list
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector
                (".assets-container section div.ctrl-wr-asset:first-child a.ast-name")));
        String bookmarkedAsset = driver.findElement(By.cssSelector
                (".assets-container section div.ctrl-wr-asset:first-child a.ast-name")).getText();
        driver.findElement(By.cssSelector(".assets-container section div.ctrl-wr-asset:first-child a.ast-name")).click();

        //bookmark the asset
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("btn-add-gadget")));
        driver.findElement(By.id("btn-add-gadget")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("btn-add-gadget"), "Bookmarked"));
        assertEquals("Bookmarked", driver.findElement(By.id("btn-add-gadget")).getText(), "Bookmarking failed");

        //check if shown in My Items page
/*
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.ast-title a.ast-name")));
        assertEquals(bookmarkedAsset, driver.findElement(By.cssSelector("div.ast-title a.ast-name")).getText(),
                "Bookmarked asset not shown in My Items page");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".bookmark-link")));
        driver.findElement(By.cssSelector(".bookmark-link")).click();
        */

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + STORE_LOGOUT_URL);
        driver.quit();
    }

}
