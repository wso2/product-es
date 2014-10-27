/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.es.ui.integration.extension.test.publisher;

import org.openqa.selenium.Alert;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;
import org.openqa.selenium.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.Select;
import org.wso2.es.ui.integration.extension.util.ESUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;


public class ESPublisherNewGlobalPageTestCase extends ESIntegrationUITest {
    private WebDriver driver;
    private String baseUrl;
    private String webApp = "publisher";
    private boolean acceptNextAlert = true;
 
  @BeforeClass(alwaysRun = true)
  public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, webApp);
  }

  @Test(groups = "wso2.es", description = "")
  public void testESPublisherNewGlobalPageTestCase() throws Exception {
    driver.get(baseUrl + "/publisher/pages/servicex_global");
    assertTrue(isElementPresent(By.id("assetNewGlobalPage")));
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
