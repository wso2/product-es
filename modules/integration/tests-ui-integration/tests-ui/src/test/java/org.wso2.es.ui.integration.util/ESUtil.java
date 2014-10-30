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

package org.wso2.es.ui.integration.util;

import org.openqa.selenium.By;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;

import javax.mail.*;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ESUtil extends ESIntegrationUITest {
    private static String publisherSuffix = "/publisher";
    private static String storeSuffix = "/store";
    private static String adminConsoleSuffix = "/carbon/admin/index.jsp";

    public static void login(ESWebDriver driver, String url, String webApp,
                             String userName, String pwd) throws XPathExpressionException {

        if (webApp.equalsIgnoreCase("store")) {
            url = url + storeSuffix;
            driver.get(url);
            driver.findElement(By.partialLinkText("Sign in")).click();

        } else if (webApp.equalsIgnoreCase("publisher")) {
            url = url + publisherSuffix;
            driver.get(url);
        }
        driver.findElementPoll(By.id("username"), 30);
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(userName);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(pwd);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        driver.get(url);
    }

    public static void logout(ESWebDriver driver, String url, String webApp,
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

    public static void loginToAdminConsole(ESWebDriver driver, String url, String userName, String pwd
    ) throws XPathExpressionException {
        driver.get(url+adminConsoleSuffix);
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(userName);
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(pwd);
        driver.findElement(By.cssSelector("input.button")).click();
    }

    public static void logoutFromAdminConsole(ESWebDriver driver, String url){
        driver.get(url + adminConsoleSuffix);
        driver.findElement(By.linkText("Sign-out")).click();
    }

    public static void setupUserProfile(ESWebDriver driver, String url, String userName, String firstName,
                                        String lastName,
                                        String email){
        String userProfileElement;
        if(userName.equals("admin")){
            userProfileElement = "//a[contains(text(),'User\n                                                                                            Profile')]";
        }else {
            userProfileElement = "(//a[contains(text(),'User\n                                                                                            Profile')])[2]";
        }
        driver.get(url + adminConsoleSuffix);
        driver.findElement(By.cssSelector("#menu-panel-button3 > span")).click();
        driver.findElement(By.linkText("Users and Roles")).click();
        driver.findElement(By.linkText("Users")).click();
        driver.findElement(By.xpath(userProfileElement)).click();
        driver.findElement(By.id("http://wso2.org/claims/givenname")).clear();
        driver.findElement(By.id("http://wso2.org/claims/givenname")).sendKeys(firstName);
        driver.findElement(By.id("http://wso2.org/claims/lastname")).clear();
        driver.findElement(By.id("http://wso2.org/claims/lastname")).sendKeys(lastName);
        driver.findElement(By.id("http://wso2.org/claims/emailaddress")).clear();
        driver.findElement(By.id("http://wso2.org/claims/emailaddress")).sendKeys(email);
        driver.findElement(By.name("updateprofile")).click();
        driver.findElement(By.cssSelector("button[type=\"button\"]")).click();
        driver.findElement(By.cssSelector("#menu-panel-button1 > span")).click();
    }

    public static boolean containsEmail(String smtpPropertyFile, String password, String email, String subject){
        Properties props = new Properties();
        boolean hasEmail = false;
        try {
            props.load(new FileInputStream(new File(smtpPropertyFile)));
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("smtp.gmail.com", email, password);
            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (Message msg : messages){
                if(subject.equals(msg.getSubject())){
                    hasEmail = true;
                    break;
                }
            }
            inbox.close(true);
            store.close();
            return hasEmail;

        } catch (Exception e) {
            e.printStackTrace();
            return hasEmail;
        }
    }

    public static void deleteAllEmail(String smtpPropertyFile, String password, String email){
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File(smtpPropertyFile)));
            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore("imaps");
            store.connect("smtp.gmail.com", email, password);

            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            int messageCount = inbox.getMessageCount();

            Message[] messages = inbox.getMessages();

            for(int i=0; i<messageCount; i++){
                messages[i].setFlag(Flags.Flag.DELETED, true);
            }
            inbox.close(true);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}