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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.openqa.selenium.*;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class ESWebDriver implements org.openqa.selenium.WebDriver {
    // private org.openqa.selenium.WebDriver driver;
    protected static final Logger log = Logger.getLogger(ESWebDriver.class);
    private int maxWaitTime;
    private EventFiringWebDriver driver;

    private WebDriverEventListener errorListener = new AbstractWebDriverEventListener() {
        @Override
        public void onException(Throwable throwable, WebDriver driver) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar
                    .getInstance().getTime());
            String snapshotName = timeStamp
                    + " : " + throwable.getCause().getMessage().toString().split("\n")[0];
            captureScreenShot(snapshotName);
        }
    };

    public ESWebDriver() throws Exception {
        // driver = BrowserManager.getWebDriver(); // get the default webdriver to the class
        driver = new EventFiringWebDriver(BrowserManager.getWebDriver());
        maxWaitTime = 30;
        driver.register(errorListener);
    }

    /**
     * This method takes a screen-shot of current web-driver instance     *
     * @param snapShotName String indicating name of the screen-shot
     */
    public void captureScreenShot(String snapShotName) {
        try {
            String filename = snapShotName + ".png";
            String pathName = FrameworkPathUtil.getReportLocation() + File.separator +
                    "surefire-reports" + File.separator + "screen-shot";
            log.error("OnException - Saving Screen-shot : " + filename + " to location " + pathName);
            File screenShot = this.driver.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenShot, new File(pathName  + File.separator + filename));
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * This method will keep refreshing/reloading the current url for a given number of poll-count
     * until a given element is available
     *
     * @param by        Element that is expected to be present
     * @param pollCount Number of time page need to be reloaded into webdriver
     */
    public void findElementPoll(By by, int pollCount) {
        int count = 0;
        while (!isElementPresent(by) && count < pollCount) {
            String url = driver.getCurrentUrl();
            driver.get(url);
            count++;
        }
    }

    /**
     * This method checks whether a given element is present in the page
     *
     * @param by Element to be present in the page
     * @return true if element is present false otherwise
     */
    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * This method will wait untill a given element is present in the page for a given amount of time
     *
     * @param by          Element to be present in the current page
     * @param waitTimeSec Time to wait in seconds
     */
    private void waitTillElementPresent(By by, int waitTimeSec) {
        WebDriverWait wait;
        wait = new WebDriverWait(driver, waitTimeSec);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    @Override
    public void get(String s) {
        driver.get(s);
    }

    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return driver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    /**
     * This method has override the findElement method in a way it will wait for maximum of 30 seconds
     *
     * @param by By element for findElement method
     * @return return the result of default WebDriver.findElement(By by) subjected to 30sec of max wait time
     */
    @Override
    public WebElement findElement(By by) {
        waitTillElementPresent(by, this.maxWaitTime);
        return driver.findElement(by);
    }

    @Override
    public String getPageSource() {
        return driver.getPageSource();
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public void quit() {
        driver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return driver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return driver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return driver.navigate();
    }

    @Override
    public Options manage() {
        return driver.manage();
    }
}
