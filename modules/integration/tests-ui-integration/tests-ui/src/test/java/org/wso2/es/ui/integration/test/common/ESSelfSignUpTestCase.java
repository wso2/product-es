/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.es.ui.integration.test.common;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Verify fields in self sign upp form based on enabled claims
 */
public class ESSelfSignUpTestCase extends BaseUITestCase{
    private static final int MAX_WAIT_TIME = 30;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        wait = new WebDriverWait(driver, MAX_WAIT_TIME);
        driver.get(baseUrl + MANAGEMENT_CONSOLE_URL);
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(currentUserName);
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(currentUserPwd);
        driver.findElement(By.xpath("//input[@value='Sign-in']")).click();

    }

    @Test(groups = "wso2.es.common", description = "Testing correctness of self sign up form")
    public void testSelfSignUpForm() throws Exception {
        driver.get(baseUrl + MANAGEMENT_CONSOLE_URL);
        driver.findElement(By.xpath("//span[contains(.,'Configure')]")).click();
        driver.findElement(By.linkText("Claim Management")).click();
        driver.findElement(By.linkText("http://wso2.org/claims")).click();
        driver.findElement(By.xpath("//span[contains(.,'Country')]")).click();
        driver.findElement(By.xpath("//a[@href='update-claim.jsp?dialect=http://wso2.org/claims&claimUri=http://wso2.org/claims/country']")).click();
        driver.findElement(By.xpath("//input[@id='required']")).click();
        driver.findElement(By.xpath("//input[@value='Update']")).click();
        driver.get(baseUrl + PUBLISHER_URL);
        assertTrue(isElementPresent(driver, By.name("reg-country")), "New claim is not shown up in sign up form");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + MANAGEMENT_CONSOLE_URL);
        driver.findElement(By.linkText("Claim Management")).click();
        driver.findElement(By.linkText("http://wso2.org/claims")).click();
        driver.findElement(By.xpath("//span[contains(.,'Country')]")).click();
        driver.findElement(By.xpath("//a[@href='update-claim.jsp?dialect=http://wso2.org/claims&claimUri=http://wso2.org/claims/country']")).click();
        driver.findElement(By.id("required")).click();
        driver.findElement(By.xpath("//input[@value='Update']")).click();
        driver.findElement(By.linkText("Sign-out")).click();
        driver.quit();
    }

}
