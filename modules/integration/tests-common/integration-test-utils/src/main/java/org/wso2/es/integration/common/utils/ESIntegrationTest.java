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

import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

import javax.xml.xpath.XPathExpressionException;

public abstract class ESIntegrationTest {
    protected final static String PRODUCT_NAME = "ES";
    protected AutomationContext esContext = null;
    protected Tenant tenantInfo;
    protected User userInfo;
    protected String sessionCookie;
    protected TestUserMode userMode;

    protected void init() throws Exception {
        userMode =  TestUserMode.SUPER_TENANT_ADMIN;
        init(userMode);
    }

    protected void init(TestUserMode userType) throws Exception {

        esContext = new AutomationContext(PRODUCT_NAME, userType);
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
        return TestConfigurationProvider.getResourceLocation(PRODUCT_NAME);
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

}
