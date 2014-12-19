/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.es.ui.integration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;

import javax.mail.*;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Contains utility methods for ES operations
 */
public class ESUtil extends ESIntegrationUITest {

    private static final Log LOG = LogFactory.getLog(ESUtil.class);
    private static final String PUBLISHER_SUFFIX = "/publisher";
    private static final String STORE_SUFFIX = "/store";
    private static final String ADMIN_CONSOLE_SUFFIX = "/carbon/admin/index.jsp";
    private static final int MAIL_WAIT_TIME = 2000;
    private static final int MAX_POLL_COUNT = 30;
    private static final int MAX_MAIL_POLL = 20;
    private static final String IMAPS = "imaps";
    public static final String SMTP_GMAIL_COM = "smtp.gmail.com";
    public static final String INBOX = "inbox";

    /**
     * To login to store or publisher
     *
     * @param driver   WebDriver instance
     * @param url      base url of the server
     * @param webApp   store or publisher
     * @param userName user name
     * @param pwd      password
     * @throws javax.xml.xpath.XPathExpressionException
     *
     */
    public static void login(ESWebDriver driver, String url, String webApp, String userName, String pwd)
            throws InterruptedException, XPathExpressionException {
        String fullUrl = "";
        if ("store".equalsIgnoreCase(webApp)) {
            fullUrl = url + STORE_SUFFIX;
            driver.get(fullUrl);
            driver.findElement(By.partialLinkText("Sign in")).click();

        } else if ("publisher".equalsIgnoreCase(webApp)) {
            fullUrl = url + PUBLISHER_SUFFIX;
            driver.get(fullUrl);
        }
        driver.findElementPoll(By.id("username"), MAX_POLL_COUNT);
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(userName);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(pwd);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        driver.get(fullUrl);
    }

    /**
     * To logout from store and publisher
     *
     * @param driver   WebDriver instance
     * @param url      base url of the server
     * @param webApp   store or publisher
     * @param userName user name
     * @throws javax.xml.xpath.XPathExpressionException
     *
     */
    public static void logout(ESWebDriver driver, String url, String webApp, String userName) throws
            XPathExpressionException {
        String fullUrl = "";
        if ("store".equalsIgnoreCase(webApp)) {
            fullUrl = url + STORE_SUFFIX;
        } else if ("publisher".equalsIgnoreCase(webApp)) {
            fullUrl = url + PUBLISHER_SUFFIX;
        }
        driver.get(fullUrl);
        driver.findElement(By.linkText(userName)).click();
        driver.findElement(By.linkText("Sign out")).click();
    }

    /**
     * To login to admin console
     *
     * @param driver   WebDriver instance
     * @param url      base url of the server
     * @param userName user name
     * @param pwd      password
     * @throws javax.xml.xpath.XPathExpressionException
     *
     */
    public static void loginToAdminConsole(ESWebDriver driver, String url, String userName, String pwd) throws
            XPathExpressionException {
        driver.get(url + ADMIN_CONSOLE_SUFFIX);
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys(userName);
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys(pwd);
        driver.findElement(By.cssSelector("input.button")).click();
    }

    /**
     * To logout from admin console
     *
     * @param driver WebDriver instance
     * @param url    base url of the server
     */
    public static void logoutFromAdminConsole(ESWebDriver driver, String url) {
        driver.get(url + ADMIN_CONSOLE_SUFFIX);
        driver.findElement(By.linkText("Sign-out")).click();
    }

    /**
     * Set up user profile
     *
     * @param driver    WebDriver instance
     * @param url       base url of the server
     * @param userName  user name
     * @param firstName first name
     * @param lastName  last name
     * @param email     email
     */
    public static void setupUserProfile(ESWebDriver driver, String url, String userName,
                                        String firstName, String lastName, String email) {
        String userProfileElement;
        if ("admin".equals(userName)) {
            userProfileElement = "//a[contains(text(),'User\n                                    " +
                    "                                                        Profile')]";
        } else {
            userProfileElement = "(//a[contains(text(),'User\n                                   " +
                    "                                                         Profile')])[2]";
        }
        driver.get(url + ADMIN_CONSOLE_SUFFIX);
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

    /**
     * To check if a e-mail exists with a given subject
     *
     * @param smtpPropertyFile smtp property file path
     * @param password         password
     * @param email            email address
     * @param subject          email subject
     * @return if the mail exist
     * @throws java.io.IOException
     * @throws MessagingException
     * @throws InterruptedException
     */
    public static boolean containsEmail(String smtpPropertyFile, String password, String email,
                                        String subject) throws MessagingException, InterruptedException, IOException {
        Properties props = new Properties();
        boolean hasEmail = false;
        Folder inbox = null;
        Store store = null;
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(new File(smtpPropertyFile));
            props.load(inputStream);
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore(IMAPS);
            store.connect(SMTP_GMAIL_COM, email, password);
            inbox = store.getFolder(INBOX);
            inbox.open(Folder.READ_ONLY);
            hasEmail = hasMailWithSubject(inbox, subject);
        } catch (MessagingException e) {
            LOG.error(getErrorMessage(email), e);
            throw e;
        } catch (InterruptedException e) {
            LOG.error(getErrorMessage(email), e);
            throw e;
        } catch (FileNotFoundException e) {
            LOG.error(getErrorMessage(email), e);
            throw e;
        } catch (IOException e) {
            LOG.error(getErrorMessage(email), e);
            throw e;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOG.error("Input stream closing failed");
                }
            }
            if (inbox != null) {
                try {
                    inbox.close(true);
                } catch (MessagingException e) {
                    LOG.error("Inbox closing failed");
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    LOG.error("Message store closing failed");
                }
            }
        }
        return hasEmail;
    }

    private static boolean hasMailWithSubject(Folder inbox, String subject) throws MessagingException,
            InterruptedException {
        int pollCount = 0;
        while ((pollCount <= MAX_MAIL_POLL)) {
            Message[] messages = inbox.getMessages();
            for (Message msg : messages) {
                if (subject.equals(msg.getSubject())) {
                    return true;
                }
            }
            Thread.sleep(MAIL_WAIT_TIME);
        }
        return false;
    }

    private static String getErrorMessage(String emailAddress) {
        return "Retrieving mails for: " + emailAddress + "failed";
    }
}