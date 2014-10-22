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

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

import org.openqa.selenium.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.Select;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.ESUtil;


public class ESPublisherListPageSortByLinksTestCase extends ESIntegrationUITest {
    private WebDriver driver;
    private String baseUrl;
    private String webApp = "publisher";


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, webApp);
    }

    @Test(groups = "wso2.es", description = "")
    public void testESPublisherListPageTestSortByLinks() throws Exception {
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.findElement(By.cssSelector("span.publisherTitle")).click();
        driver.findElement(By.partialLinkText("NAME")).click();
        driver.findElement(By.linkText("VERSION")).click();
        driver.findElement(By.linkText("OWNER")).click();
        driver.findElement(By.linkText("CREATED")).click();
        assertEquals("Asset | WSO2 Enterprise Store back-office", driver.getTitle());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
       ESUtil.logout(driver, baseUrl,webApp);
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

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

}

