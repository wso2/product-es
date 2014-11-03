/*
 * Copyright (c) 2014, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.*;

/**
 * Test appearance and behavior of Store Home page
 */
public class ESStoreAnonHomePageTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String baseUrl;
    private StringBuffer verificationErrors = new StringBuffer();

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver();
        baseUrl = getWebAppURL();
    }

    @Test(groups = "wso2.es.store", description = "Test Anonymous User Home Page")
    public void testAnonHomePage() throws Exception {
        //test appearance
        driver.get(baseUrl + "/store");
        try {
            assertTrue(isElementPresent(By.cssSelector("a.brand")),
                    "Home Page error: Logo missing");
            assertEquals("Sign in", driver.findElement(By.linkText("Sign in")).getText(),
                    "Home Page error: Sign in button missing");
            assertTrue(isElementPresent(By.id("btn-register")), "Home Page error: Register button" +
                    " missing");
            assertEquals("Gadget", driver.findElement(By.xpath
                    ("//div[@id='container-search']/div/div/div/div/a[1]/li")).getText(),
                    "Home Page error: Gadget menu missing");
            assertEquals("Site", driver.findElement(By.xpath
                    ("//div[@id='container-search']/div/div/div/div/a[2]/li")).getText(),
                    "Home Page error: Site menu missing");
            assertEquals("Recent Gadgets", driver.findElement(By.linkText("Recent Gadgets"))
                    .getText(), "Home Page error: Recent Gadgets links missing");
            assertEquals("Recent Sites", driver.findElement(By.linkText("Recent Sites")).getText(),
                    "Home Page error: Recent Sites links missing");
            assertTrue(isElementPresent(By.id("search")), "Home Page error: Search missing");
            assertTrue(isElementPresent(By.cssSelector("div.span3.store-right > div.row > div" +
                    ".span3")), "Home Page error: Recent Added side list missing");
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
        failOnError();
    }

    @Test(groups = "wso2.es.store", description = "Test Anonymous User Recent sliding",
            enabled = false)
    public void testSliding() throws Exception {
        //TODO how to detect sliding is successful?
    }

    @Test(groups = "wso2.es.store", description = "Test Anonymous Navigation from top menu")
    public void testAnonNavigationTop() throws Exception {
        //test menu navigation
        driver.get(baseUrl + "/store");
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a[1]/li"))
                .click();
        assertEquals("Line Plus Bar Chart", driver.findElement(By.cssSelector("h4")).getText(),
                "Gadgets Menu not working");
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a[2]/li"))
                .click();
        assertEquals("Amazon", driver.findElement(By.xpath
                ("//div[@id='assets-container']/div/div[1]/div/div/a/h4")).getText(),
                "Sites Menu not working");
        driver.findElement(By.cssSelector("a.brand")).click();
    }

    @Test(groups = "wso2.es.store", description = "Test Anonymous Navigation page links")
    public void testAnonNavigationLinks() throws Exception {
        //test link navigation
        driver.get(baseUrl + "/store");
        driver.findElement(By.cssSelector("a.brand")).click();
        driver.findElement(By.linkText("Recent Gadgets")).click();
        assertEquals("Line Chart", driver.findElement(By
                .xpath("//h4[contains(.,'Line Chart')]")).getText(),
                "Recent Gadgets link not working");
        driver.findElement(By.cssSelector("a.brand")).click();
        driver.findElement(By.linkText("Recent Sites")).click();
        assertEquals("Amazon", driver.findElement(By.xpath("//h4[contains(.,'Amazon')]"))
                .getText(), "Recent Sites link not working");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private void failOnError() {
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            verificationErrors = new StringBuffer();
            fail(verificationErrorString);
        }
    }

}