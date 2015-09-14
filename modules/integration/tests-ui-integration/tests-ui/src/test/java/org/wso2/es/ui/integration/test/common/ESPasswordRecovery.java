/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import java.io.File;

import static org.testng.Assert.assertEquals;

public class ESPasswordRecovery extends BaseUITestCase{
    private static final int MAX_WAIT_TIME = 30;
    private static final String EMAIL_PWD = "automation.test1";
    private static final String USER = "recoveryuser";
    private static final String PASSWORD = "qwe123Q!";
    private static final String EMAIL = "test.automation.dummy@gmail.com";
    private static final String FIRST_NAME = "recovery";
    private static final String LAST_NAME = "user";
    private static final String QUESTION = "Favorite food ?";
    private static final String ANSWER = "Ice cream";
    private static final String NEW_PASSWORD = "asd123A!";
    private static final String SMTP_PROPERTY_FILE = File.separator + "notifications" + File.separator + "smtp.properties";
    public static final String RESET_MAIL_TITLE = "WSO2 Enterprise Store - Password Reset";

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        wait = new WebDriverWait(driver, MAX_WAIT_TIME);

        driver.get(baseUrl + STORE_URL);
        driver.findElement(By.id("btn-register")).click();
        driver.findElement(By.id("reg-username")).clear();
        driver.findElement(By.id("reg-username")).sendKeys(USER);
        driver.findElement(By.id("reg-password")).clear();
        driver.findElement(By.id("reg-password")).sendKeys(PASSWORD);
        driver.findElement(By.id("reg-password2")).clear();
        driver.findElement(By.id("reg-password2")).sendKeys(PASSWORD);
        driver.findElement(By.name("reg-email")).clear();
        driver.findElement(By.name("reg-email")).sendKeys(EMAIL);
        driver.findElement(By.name("reg-first-name")).clear();
        driver.findElement(By.name("reg-first-name")).sendKeys(FIRST_NAME);
        driver.findElement(By.name("reg-last-name")).clear();
        driver.findElement(By.name("reg-last-name")).sendKeys(LAST_NAME);
        driver.findElement(By.id("registrationSubmit")).click();
    }

    @Test(groups = "wso2.es.common", description = "Testing password recovery through email")
    public void testPasswordRecoveryEmail() throws Exception {
        driver.get(baseUrl + STORE_URL);
        driver.findElement(By.id("btn-signin")).click();
        driver.findElement(By.linkText("Forgot your password?")).click();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(USER);
        driver.findElement(By.id("validationSubmit")).click();
        String resourceLocation = getResourceLocation();
        smtpPropertyLocation = resourceLocation + SMTP_PROPERTY_FILE;
        // TODO:avoid waiting
        // waiting for email to receive
        Thread.sleep(10000);
        String message = ESUtil.readEmail(smtpPropertyLocation, EMAIL_PWD, EMAIL, RESET_MAIL_TITLE);
        String resetUrl = message.split("\r")[5].trim().substring("https://localhost:9443".length());
        driver.get(baseUrl + resetUrl);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(NEW_PASSWORD);
        driver.findElement(By.id("password2")).clear();
        driver.findElement(By.id("password2")).sendKeys(NEW_PASSWORD);
        driver.findElement(By.id("resetPassword")).click();
        ESUtil.login(driver, baseUrl, STORE_APP, USER, NEW_PASSWORD);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        driver.quit();
    }

}