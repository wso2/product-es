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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

/**
 * This class contains test related to posting and viewing rating from ES-front office
 */
public class ESStoreRatingsTestCase extends BaseUITestCase {
    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        wait = new WebDriverWait(driver, MAX_DRIVER_WAIT_TIME_SEC);
        baseUrl = getWebAppURL();
        driver.get(baseUrl + "/" + STORE_APP);
    }

    @Test(groups = "wso2.es.store.ratings", description = "Test Start Add Rating")
    public void testStoreBeforeAddRatings() throws Exception {
        driver.get(baseUrl + "/store/asts/gadget/list");
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("h4")).click();
        driver.findElement(By.linkText("User Reviews")).click();
        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        assertEquals("Please Sign in to add a Review",
                driver.findElement(By.cssSelector("div.com-guest")).getText());
        assertEquals("Be the first one to review!",
                driver.findElement(By.cssSelector("p.com-first-review")).getText());
        driver.switchTo().defaultContent();
    }

    @Test(groups = "wso2.es.store.ratings", description = "Test Submit Rating",
            dependsOnMethods = "testStoreBeforeAddRatings")
    public void testESAddRating() throws Exception {
        ESUtil.login(driver, baseUrl, STORE_APP, userInfo.getUserName(), userInfo.getPassword());
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("h4")).click();
        driver.findElement(By.linkText("User Reviews")).click();
        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        driver.findElement(By.id("com-body")).clear();
        driver.findElement(By.id("com-body")).sendKeys("cool!");
        driver.findElement(By.linkText("2")).click();
        driver.findElement(By.id("btn-post")).click();
        driver.switchTo().defaultContent();
        driver.get(driver.getCurrentUrl());
        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        assertTrue(isElementPresent(By.cssSelector("div.com-rating-2star")),
                "Rating is not added");
        assertEquals("cool!", driver.findElement(By.cssSelector("p")).getText(),
                "Review Comment not added");
    }

    @Test(groups = "wso2.es.store.ratings", description = "Test Submit Second Rating",
            dependsOnMethods = "testESAddRating")
    public void testESTryingToAddReviewWithoutRating() throws Exception {
        driver.findElement(By.id("com-body")).clear();
        driver.findElement(By.id("com-body")).sendKeys("Nice!");
        driver.findElement(By.id("btn-post")).click();
        assertEquals("Please add your Rating", driver.findElement(By.cssSelector("div.com-alert")
        ).getText(), "Alert Doesn't appear");
        driver.findElement(By.linkText("4")).click();
        driver.findElement(By.id("btn-post")).click();

    }

    @Test(groups = "wso2.es.store.ratings", description = "Test View My Rating",
            dependsOnMethods = "testESTryingToAddReviewWithoutRating")
    public void testESStoreViewMyRating() throws Exception {
        driver.switchTo().defaultContent();
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("h4")).click();
        driver.findElement(By.linkText("User Reviews")).click();
        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        assertTrue(isElementPresent(By.cssSelector("div.com-rating-2star")),
                "My First Rating Doesn't appear");
        assertEquals("cool!", driver.findElement(By.xpath("//div[@id='stream']/div[1]/div/div/p"))
                .getText(), "My First Review Doesn't appear");
        assertEquals("", driver.findElement(By.cssSelector("div.com-rating-4star")).getText());
        assertTrue(isElementPresent(By.cssSelector("div.com-rating-4star")),
                "My Second Rating Doesn't appear");
        assertEquals("Nice!", driver.findElement(By.xpath("//div[@id='stream']/div[2]/div/div/p"))
                .getText(), "My Second Review Doesn't appear");

    }

    @Test(groups = "wso2.es.store.ratings", description = "Test View Rating As Anon user",
            dependsOnMethods = "testStoreAddLogoutAndViewRatings")
    public void testESStoreAnonViewRating() throws Exception {
        driver.switchTo().defaultContent();

        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("h4")).click();
        assertEquals("", driver.findElement(By.cssSelector("span.asset-rating > div")).getText());
        driver.findElement(By.linkText("User Reviews")).click();

        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        assertTrue(isElementPresent(By.cssSelector("div.com-rating-2star")));
        assertEquals("cool!", driver.findElement(By.xpath("//div[@id='stream']/div[1]/div/div/p"))
                .getText(), "First Review doesn't appear to anonymous user");
        assertEquals("", driver.findElement(By.cssSelector("div.com-rating-4star")).getText(),
                "First Rating doesn't appear to anonymous user");
        assertTrue(isElementPresent(By.cssSelector("div.com-rating-4star")),
                "Second Rating doesn't appear to anonymous user");
        assertEquals("Nice!", driver.findElement(By.xpath("//div[@id='stream']/div[2]/div/div/p"))
                .getText(), "Second Review doesn't appear to anonymous user");

    }

    @Test(groups = "wso2.es.store", description = "Test Logout and view rating",
            dependsOnMethods = "testESStoreViewMyRating")
    public void testStoreAddLogoutAndViewRatings() throws Exception {
        driver.switchTo().defaultContent();
        driver.findElement(By.linkText("admin")).click();
        driver.findElement(By.linkText("Sign out")).click();
        driver.findElement(By.cssSelector("h4")).click();
        driver.findElement(By.linkText("User Reviews")).click();
        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        assertEquals("Please Sign in to add a Review",
                driver.findElement(By.cssSelector("div.com-guest")).getText(),
                "Sign in massage doesn't appear in anon user view");
        assertEquals("cool!", driver.findElement(By.cssSelector("p")).getText());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}