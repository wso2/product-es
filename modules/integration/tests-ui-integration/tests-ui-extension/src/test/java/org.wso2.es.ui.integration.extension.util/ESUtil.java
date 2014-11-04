/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.es.ui.integration.extension.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;

import javax.xml.xpath.XPathExpressionException;
import java.util.concurrent.TimeUnit;

public class ESUtil extends ESIntegrationUITest {
    private static String publisherSuffix = "/publisher";
    private static String storeSuffix = "/store";
    private static String adminConsoleSuffix = "/carbon/admin/index.jsp";


    public static void login(WebDriver driver, String url, String webApp,
                             String userName, String password) throws XPathExpressionException {

        if (webApp.equalsIgnoreCase("store")) {
            url = url + storeSuffix;
            driver.get(url);
            driver.findElement(By.partialLinkText("Sign in")).click();

        } else if (webApp.equalsIgnoreCase("publisher")) {
            url = url + publisherSuffix;
            driver.get(url);
        }

        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(userName);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        driver.get(url);
    }

    public static void logout(WebDriver driver, String url, String webApp,
                              String userName) throws XPathExpressionException {
        if (webApp.equalsIgnoreCase("store")) {
            url = url + storeSuffix;
        } else if (webApp.equalsIgnoreCase("publisher")) {
            url = url + publisherSuffix;
        }
        driver.get(url);
        driver.findElement(By.linkText(userName)).click();
        driver.findElement(By.linkText("Sign out")).click();
    }

    public static void loginToAdminConsole(WebDriver driver, String url,
                                           String userName, String password) throws XPathExpressionException {
        driver.get(url+adminConsoleSuffix);
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(userName);
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(password);
        driver.findElement(By.cssSelector("input.button")).click();
    }
}
