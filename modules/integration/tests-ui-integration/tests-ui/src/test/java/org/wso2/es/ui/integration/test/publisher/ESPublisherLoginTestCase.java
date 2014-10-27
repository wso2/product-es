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

package org.wso2.es.ui.integration.test.publisher;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.ui.page.LoginPage;
import org.wso2.es.integration.common.ui.page.main.HomePage;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;

import com.thoughtworks.selenium.*;
import org.testng.annotations.*;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.*;

import java.util.regex.Pattern;

public class ESPublisherLoginTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String webAppURL;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver();
        webAppURL = getWebAppURL();
        driver.get(webAppURL);
    }

    @Test(groups = "wso2.es", description = "verify login to ES Publisher")
    public void testLogintoPublisher() throws Exception {
        driver.get(webAppURL + "/publisher/asts/gadget");
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("admin");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        assertEquals("Asset | WSO2 Enterprise Store back-office", driver.getTitle());
        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
