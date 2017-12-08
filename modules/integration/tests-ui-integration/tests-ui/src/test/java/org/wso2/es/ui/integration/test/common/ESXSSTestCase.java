/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
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

package org.wso2.es.ui.integration.test.common;

import org.openqa.selenium.NoAlertPresentException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESWebDriver;

import javax.xml.xpath.XPathExpressionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import static org.testng.Assert.assertFalse;

/**
 * Test script injection in user user registration, login, and gadgets page
 */
public class ESXSSTestCase extends BaseUITestCase {

    private static final String STORE_LOGIN_URL = "/store/login";
    private static final String SSO_LOGIN_URL = "/sso/login";

    private String baseHttpsUrl;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        baseHttpsUrl = getWebAppURLHttps();
    }

    @Test(groups = "wso2.es.common", description = "Testing XSS in user registration")
    public void testRegisterUserXSSTestCase() throws Exception {

        String url = baseHttpsUrl + "/sso/register_new_user?relyingParty=";

        String alertMessage = "XSS";
        driver.get(url + "\"><script>alert(\"" + alertMessage + "\")</script>");

        boolean scriptInjected = false;

        try {
            String actualAlertMessage = closeAlertAndGetItsText(driver, false);

            if (actualAlertMessage.equals(alertMessage))
                scriptInjected = true;

        } catch (NoAlertPresentException ex) {
        }

        assertFalse(scriptInjected, "Script injected via the query string");
    }

    @Test(groups = "wso2.es.common", description = "Test XSS in Gadgets Page")
    public void testListGadgetsXSSTestCase() throws Exception {

        String alertMessage = "XSS";

        String url = baseUrl + "/store/assets/gadget/list?" +
                "sortBy=overview_name&sort=}</script><script>alert('" + alertMessage + "')</script>";
        driver.get(url);

        boolean scriptInjected = false;

        try {
            String actualAlertMessage = closeAlertAndGetItsText(driver, false);

            if (actualAlertMessage.equals(alertMessage))
                scriptInjected = true;
        } catch (NoAlertPresentException ex) {
        }

        assertFalse(scriptInjected, "Script injected via the query string");
    }

    @Test(groups = "wso2.es.common", description = "Test XSS in SSO Login")
    public void testSSOLoginXSSTestCase() throws MalformedURLException, XPathExpressionException {

        String alertMessage = "XSS";

        String malformedRelayStateQueryString = "?";
        String malformedSessionDataKeyQueryString = "?";

        boolean scriptInjected;

        driver.get(baseHttpsUrl + STORE_LOGIN_URL);
        URL url = new URL(driver.getCurrentUrl());


        // Get the current URL, fetch the query string and slice it by '&' and '=' signs to access
        // the each parameter key-value pair.
        StringTokenizer queryStringTokenizer = new StringTokenizer(url.getQuery(), "&", false);
        while (queryStringTokenizer.hasMoreElements()) {

            String param = queryStringTokenizer.nextToken();

            StringTokenizer parameterTokenizer = new StringTokenizer(param, "=", false);

            String paramName = "";
            if (parameterTokenizer.hasMoreElements())
                paramName = parameterTokenizer.nextToken();

            // If relayState and the SessionDataKey parameters, modify them accordingly and
            // create separate query strings
            if (paramName.toLowerCase().equals("relaystate") ||
                    paramName.toLowerCase().equals("sessiondatakey")) {

                String paramValue = "";
                if (parameterTokenizer.hasMoreElements())
                    paramValue = parameterTokenizer.nextToken();

                String modifiedParam = paramName + "=" + paramValue +
                        "\"><script>alert('" + alertMessage + "');</script>";

                if (paramName.toLowerCase().equals("relaystate")) {

                    malformedRelayStateQueryString += modifiedParam + "&";
                    malformedSessionDataKeyQueryString += param + "&";

                } else {

                    malformedRelayStateQueryString += param + "&";
                    malformedSessionDataKeyQueryString += modifiedParam + "&";

                }

            } else {

                // Append the all other parameters as they are in the new query strings
                malformedRelayStateQueryString += param + "&";
                malformedSessionDataKeyQueryString += param + "&";

            }
        }

        // Test the relay state parameter
        scriptInjected = false;

        driver.get(baseHttpsUrl + SSO_LOGIN_URL + malformedRelayStateQueryString);

        try {
            String actualAlertMessage = closeAlertAndGetItsText(driver, false);

            if (actualAlertMessage.equals(alertMessage))
                scriptInjected = true;

        } catch (NoAlertPresentException ex) {
        }

        assertFalse(scriptInjected, "Script injected to relay state via the query string");

        // Test the session data key parameter
        scriptInjected = false;

        driver.get(baseHttpsUrl + SSO_LOGIN_URL + malformedSessionDataKeyQueryString);

        try {
            String actualAlertMessage = closeAlertAndGetItsText(driver, false);

            if (actualAlertMessage.equals(alertMessage))
                scriptInjected = true;

        } catch (NoAlertPresentException ex) {
        }

        assertFalse(scriptInjected, "Script injected to session data key via the query string");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
