/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.es.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;

/**
 * Base class of all integration tests
 */
public class ESIntegrationUIBaseTest extends ESIntegrationBaseTest {
    public static final Log log = LogFactory.getLog(ESIntegrationUIBaseTest.class);
    public static final int WAIT_SECONDS = 20;
    public static final int LOGIN_WAIT_SECONDS = 60;

    protected String getLoginURL() throws XPathExpressionException {
        return UrlGenerationUtil.getLoginURL(automationContext.getInstance());
    }

    protected String getPublisherUrl() throws XPathExpressionException{
        return automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
    }

    protected String getStoreUrl() throws XPathExpressionException{
        return automationContext.getContextUrls().getSecureServiceUrl().replace("services", "store/apis");
    }

    protected String getStoreBaseUrl() throws XPathExpressionException {
        return automationContext.getContextUrls().getSecureServiceUrl().replace("services", "store");
    }

    protected String getPublisherBaseUrl() throws XPathExpressionException {
        return automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher");
    }

    /**
     * Can be used to add new rxt configuration
     *
     * @param fileName         name of the new rxt file
     * @param resourceFileName saving name for the rxt file
     * @return true on successful addition of rxt
     * @throws Exception
     */
    public boolean addNewRxtConfiguration(String fileName, String resourceFileName) throws Exception {

        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL,
                getSessionCookie());

        String filePath = getTestArtifactLocation() + "artifacts" + File.separator +
                "es" + File.separator + "rxt" + File.separator + fileName;
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        return resourceAdminServiceClient.addResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/" + resourceFileName,
                "application/vnd.wso2.registry-ext-type+xml", "desc", dh);
    }

    /**
     * This will return a unique String based on current time
     * 02/19/2016 10:45:55pm as 02192016104555
     * @return String unique name with current date and time
     */

    protected String getUniqueName() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM:dd:yyyy:h:mm:ss:SSSS");
        String formattedDate = dateFormat.format(date);
        return formattedDate.replace(":", "");
    }

    /**
     * This method will check given element is present in the document
     * @return boolean
     */
    protected static boolean isElementPresent (WebDriver driver, By by) {
        return (driver.findElements(by).size() > 0);
    }
}


