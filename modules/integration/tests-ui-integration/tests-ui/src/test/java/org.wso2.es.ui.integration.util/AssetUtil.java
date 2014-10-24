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

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;

public class AssetUtil extends ESIntegrationUITest {

    public static void addNewAsset(WebDriver driver, String baseUrl, String assetType, String provider,
                                   String assetName, String version,
                                   String createdTime) {
        driver.get(baseUrl + "/publisher/asts/" + assetType + "/list");
        driver.findElement(By.linkText("Add")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys(provider);
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(assetName);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(version);
        driver.findElement(By.name("overview_createdtime")).clear();
        driver.findElement(By.name("overview_createdtime")).sendKeys(createdTime);
        driver.findElement(By.id("btn-create-asset")).click();
    }

    public static void deleteAsset(WebDriver driver, String baseUrl, String assetName, String assetType) {
        driver.get(baseUrl + "/carbon/");
        driver.findElement(By.linkText(assetType)).click();
        if (!isElementPresent(driver, By.linkText(assetName))) {
            driver.findElement(By.linkText(assetType)).click();
        }
        String assetText = driver.findElement(By.xpath("//table[@id='customTable']/tbody/tr[14]/td[2]")).getText();
        if (assetName.equalsIgnoreCase(assetText)) {
            driver.findElement(By.xpath("(//a[contains(text(),'Delete')])[14]")).click();
            driver.findElement(By.cssSelector("button[type=\"button\"]")).click();
        }
    }

    public static String updateAsset(WebDriver driver, String baseUrl, String assetType, String assetName,
                                     String description) {
        driver.get(baseUrl + "/publisher/asts/" + assetType + "/list");
        driver.findElement(By.linkText(assetName)).click();
        driver.findElement(By.linkText("Edit")).click();
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(description);
        driver.findElement(By.id("editAssetButton")).click();
        return closeAlertAndGetItsText(driver, true);
    }

    public static void changeLCState(WebDriver driver, String baseUrl, String assetType, String assetName,
                                     String toState) {
        //TODO lc transition using API call
    }

    private static boolean isElementPresent(WebDriver driver, By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private static String closeAlertAndGetItsText(WebDriver driver, boolean acceptNextAlert) {
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
