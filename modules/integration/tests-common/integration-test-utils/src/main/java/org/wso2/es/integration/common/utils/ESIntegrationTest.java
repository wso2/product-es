/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.es.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class ESIntegrationTest {

    protected Log log = LogFactory.getLog(ESIntegrationTest.class);

    protected AutomationContext esContext = null;
    protected Tenant tenantInfo;
    protected User userInfo;
    protected String sessionCookie;
    protected TestUserMode userMode;
    protected AutomationContext automationContext;
    protected String webAppURL;
    protected String backendURL;

    protected AutomationContext storeContext;
    protected AutomationContext publisherContext;


    protected void init() throws Exception {
        userMode =  TestUserMode.SUPER_TENANT_ADMIN;
        init(userMode);
    }

    protected void init(TestUserMode userType) throws Exception {

        esContext = new AutomationContext("ES", userType);
        automationContext = esContext;
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(esContext);
        sessionCookie = loginLogoutClient.login();
        //return the current tenant as the userType(TestUserMode)
        tenantInfo = esContext.getContextTenant();
        //return the user information initialized with the system
        userInfo = automationContext.getContextTenant().getContextUser();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        webAppURL = automationContext.getContextUrls().getWebAppURL();
        storeContext = new AutomationContext("ES", "store", userType);
        publisherContext = new AutomationContext("ES", "publisher", userType);
    }
    protected void init(String tenantKey, String userKey) throws Exception {

        esContext = new AutomationContext(ESIntegrationTestConstants.ES_PRODUCT_NAME, "es002",
                tenantKey, userKey);
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(esContext);
        sessionCookie = loginLogoutClient.login();
        //return the current tenant as the userType(TestUserMode)
        tenantInfo = esContext.getContextTenant();
        //return the user information initialized with the system
        userInfo = tenantInfo.getContextUser();

    }

    protected void cleanup() {
        userInfo = null;
        esContext = null;
    }

    protected String getServiceUrlHttp(String serviceName) throws XPathExpressionException {
        String serviceUrl = esContext.getContextUrls().getServiceUrl() + "/" + serviceName;
        validateServiceUrl(serviceUrl, tenantInfo);
        return serviceUrl;
    }

    protected String getServiceUrlHttps(String serviceName) throws XPathExpressionException {
        String serviceUrl = esContext.getContextUrls().getSecureServiceUrl() + "/" + serviceName;
        validateServiceUrl(serviceUrl, tenantInfo);
        return serviceUrl;
    }

    protected String getResourceLocation() throws XPathExpressionException {
        return TestConfigurationProvider.getResourceLocation(ESIntegrationTestConstants.ES_PRODUCT_NAME);
    }

    protected void initPublisher(String productGroupName, String instanceName, TestUserMode userMode, String userKey)
            throws XPathExpressionException {
        automationContext = new AutomationContext(productGroupName, instanceName, userMode);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
    }

    protected String getBackendURL() throws XPathExpressionException {
        return automationContext.getContextUrls().getBackEndUrl();
    }

    protected String getSessionCookie() throws Exception {
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        return loginLogoutClient.login();
    }

    protected String getServiceURL() throws XPathExpressionException {
        return automationContext.getContextUrls().getServiceUrl();
    }

    protected String getTestArtifactLocation() {
        return FrameworkPathUtil.getSystemResourceLocation();
    }


    protected boolean isTenant() throws Exception {
        if(userMode == null){
            throw new Exception("UserMode Not Initialized. Can not identify user type");
        }
        return (userMode == TestUserMode.TENANT_ADMIN || userMode == TestUserMode.TENANT_USER);
    }



    private void validateServiceUrl(String serviceUrl, Tenant tenant) {
        //if user mode is null can not validate the service url
        if (userMode != null) {
            if ((userMode == TestUserMode.TENANT_ADMIN || userMode == TestUserMode.TENANT_USER)) {
                Assert.assertTrue(serviceUrl.contains("/t/" + tenant.getDomain() + "/"), "invalid service url for tenant. " + serviceUrl);
            } else {
                Assert.assertFalse(serviceUrl.contains("/t/"), "Invalid service url for user. " + serviceUrl);
            }
        }
    }

    protected String readFile( String filePath ) throws IOException {
        String fileData=new String(Files.readAllBytes(Paths.get(filePath)));
        return fileData;
    }

}
