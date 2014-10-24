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

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.Select;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;


public class ESStoreRatings extends ESIntegrationUITest {
    private WebDriver driver;
    private String baseUrl;
    private String webApp = "store";
    private boolean acceptNextAlert = true;
    WebDriverWait wait;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        wait = new WebDriverWait(driver, 30);
        baseUrl = getWebAppURL();
        driver.get(baseUrl + "/" + webApp);

    }

    @Test(groups = "wso2.es.store.ratings", description = "Test Start Add Rating")
    public void testStoreAddRatings() throws Exception {
        driver.get(baseUrl + "/store/asts/gadget/list");
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("img")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h3"), "WSO2 Carbon Commits List Discussion"));
        driver.findElement(By.linkText("User Reviews")).click();

    }

    @Test(groups = "wso2.es.store.ratings", description = "Test Login to Add Rating", dependsOnMethods = "testStoreAddRatings")
    public void testESloginToAddRating() throws Exception {
        driver.findElement(By.linkText("Sign in")).click();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("admin");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        driver.findElement(By.linkText("User Reviews")).click();
    }

    @Test(groups = "wso2.es.store.ratings", description = "Test Submit Rating", dependsOnMethods = "testESloginToAddRating")
    public void testESAddRating() throws Exception {
        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));

        driver.findElement(By.id("com-body")).clear();
        driver.findElement(By.id("com-body")).sendKeys("cool!");
        driver.findElement(By.linkText("2")).click();
        driver.findElement(By.id("btn-post")).click();
        driver.switchTo().defaultContent();
        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        assertEquals("", driver.findElement(By.cssSelector("div.com-rating-2star")).getText());
    }

    @Test(groups = "wso2.es.store.ratings", description = "Test View My Rating", dependsOnMethods = "testESAddRating")
    public void testESStoreViewMyRating() throws Exception {
        driver.switchTo().defaultContent();

        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("h4")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("span.asset-rating > div"), ""));

        assertEquals("", driver.findElement(By.cssSelector("span.asset-rating > div")).getText());
        driver.findElement(By.linkText("User Reviews")).click();

        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        assertEquals("cool!", driver.findElement(By.cssSelector("p")).getText());

    }

    @Test(groups = "wso2.es.store.ratings", description = "Test View Rating As Annon", dependsOnMethods = "testESAddRating")
    public void testESStoreViewRating() throws Exception {
        driver.switchTo().defaultContent();

        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("h4")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("span.asset-rating > div"), ""));

        assertEquals("", driver.findElement(By.cssSelector("span.asset-rating > div")).getText());
        driver.findElement(By.linkText("User Reviews")).click();

        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        assertEquals("cool!", driver.findElement(By.cssSelector("p")).getText());

    }

    @Test(groups = "wso2.es.store", description = "Test Logout and view rating", dependsOnMethods = "testESStoreViewRating")
    public void testStoreAddLogoutAndViewRatings() throws Exception {
        driver.switchTo().defaultContent();
        driver.findElement(By.linkText("admin")).click();
        driver.findElement(By.linkText("Sign out")).click();
        //driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("h4")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h3"), "WSO2 Carbon Commits List Discussion"));

        // driver.findElement(By.cssSelector("li.dropdown. > span")).click();
        //driver.findElement(By.cssSelector("h4")).click();
        driver.findElement(By.linkText("User Reviews")).click();

        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("div.com-guest"), "Please Sign in to add a Review"));

        assertEquals("Please Sign in to add a Review", driver.findElement(By.cssSelector("div.com-guest")).getText());
        assertEquals("cool!", driver.findElement(By.cssSelector("p")).getText());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
