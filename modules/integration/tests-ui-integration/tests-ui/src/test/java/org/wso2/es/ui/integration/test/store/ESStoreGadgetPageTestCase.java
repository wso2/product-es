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
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.ESUtil;


public class ESStoreGadgetPageTestCase extends ESIntegrationUITest {
    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private String webApp = "store";

    private String currentUserName = "admin";
    private String currentUserPwd = "admin";

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        wait = new WebDriverWait(driver, 30);
        baseUrl = getWebAppURL();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        ESUtil.login(driver, baseUrl, webApp, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.store", description = "Test Gadgets Page")
    public void testGadgetPage() throws Exception {
        driver.get(baseUrl + "/store/asts/gadget/list");
        assertEquals("Gadget", driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li"))
                .getText(), "Gadget Menu missing");
        assertEquals("Line Plus Bar Chart", driver.findElement(By.cssSelector("h4")).getText(), "Gadgets missing");
        assertEquals("Recently Added", driver.findElement(By.xpath
                ("//div[@id='container-assets']/div/div[2]/div[1]/div/h4")).getText(),
                "Recently Added section missing");
        assertEquals("Line Plus Bar Chart", driver.findElement(By.cssSelector("div.span3 > div.row-fluid" +
                ".recently-added > div.span9 > strong > a")).getText(), "Recently added Gadgets missing");
        assertEquals("Tags", driver.findElement(By.xpath
                ("//div[@id='container-assets']/div/div[2]/div[2]/div/h4")).getText(), "Tags section missing");
        assertTrue(isElementPresent(By.linkText("charts")), "Tags missing (charts tag)");
        assertEquals("All Categories", driver.findElement(By.cssSelector("div.breadcrumb-head > span")).getText()
                , "Category drop down missing");
        assertTrue(isElementPresent(By.cssSelector("i.icon-star")), "Popularity sort missing");
        assertTrue(isElementPresent(By.cssSelector("i.icon-sort-alphabetical")), "Alphabetical sort missing");
        assertTrue(isElementPresent(By.cssSelector("i.icon-calendar")), "Recent sort missing");
        assertTrue(isElementPresent(By.id("search")), "Search tray missing");
    }

    @Test(groups = "wso2.es.store", description = "Test Gadgets Page Links")
    public void testLinksFromPage() throws Exception {
        driver.get(baseUrl + "/store/asts/gadget/list");
        driver.findElement(By.cssSelector("h4")).click();
        assertEquals("Line Plus Bar Chart", driver.findElement(By.cssSelector("h3")).getText(),
                "Cannot view selected Gadget's page through Gadget list");

        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.xpath("//a[contains(text(),'Line Chart')]")).click();
        assertEquals("Line Chart", driver.findElement(By.cssSelector("h3")).getText(),
                "Cannot view selected Gadget's page through Recently added list");

        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.linkText("pie")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), "Pie Chart"));
        assertEquals(1, driver.findElements(By.cssSelector("div.span3.asset")).size(), "Tags not working");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + "/store/logout");
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    private String closeAlertAndGetItsText() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (acceptNextAlert) {
                alert.accept();
            } else {
                alert.dismiss();
            }
            return alertText;
        } finally {
            acceptNextAlert = true;
        }
    }

}
