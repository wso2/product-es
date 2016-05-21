/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.publisher.lifecycle;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.es.integration.common.utils.GenericRestClient;

import java.io.File;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GRegPublisherAPILCPermissionCheck extends ESTestBaseTest {

    private TestUserMode userMode;
    private GenericRestClient genericRestClient;
    private String publisherUrl;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String cookieHeader;
    private String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                                  + "artifacts" + File.separator + "GREG" + File.separator;
    private String jSessionId;
    private String lifeCycleName = "ServiceLifeCycle";
    private String assetId;

    @Factory(dataProvider = "userModeProvider")
    public GRegPublisherAPILCPermissionCheck(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
    }

    @BeforeMethod(alwaysRun = true)
    public void resetParameters() {
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    protected void testAddResources()
            throws Exception {
        genericRestClient = new GenericRestClient();

        super.init(userMode);

        authenticatePublisher("admin", "admin");

        ClientResponse response = createAsset(resourcePath + "json" + File.separator +
                                              "publisherPublishRestResource.json", publisherUrl,
                                              cookieHeader, "restservice", genericRestClient);

        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        assetId = obj.get("id").toString();
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        response = getAsset(assetId, "restservice", publisherUrl, cookieHeader, genericRestClient);
        obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(obj.get("lifecycle").equals(lifeCycleName), "LifeCycle not assigned to given assert");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "PromoteLifeCycle with fault user",
          dependsOnMethods = {"testAddResources"})
    public void CheckLCPermissionForAUserWithoutLCPermission() throws LogViewerLogViewerException, RemoteException, JSONException {
        authenticatePublisher("withoutLCAccessuser", "withoutLCAccessuser");
        ClientResponse response = getLifeCycleState(assetId, "restservice", lifeCycleName);

        //TODO: Following assertion fails due to bug reported at
        // https://wso2.org/jira/browse/STORE-1138
        Assert.assertTrue(response.getStatusCode() == 401, "Fault user accepted");

        JSONObject errorResponse = new JSONObject(response.getEntity(String.class));
        JSONObject dataObj = errorResponse.getJSONObject("error");
        String exception = dataObj.getString("exception");
        String message = dataObj.getString("message");

        Assert.assertTrue("Unauthorized Action - does not have permissions to view lifecycle state".equals(exception));
        Assert.assertTrue("User does not have permission to view lifecycle state".equals(message));
    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

    private void authenticatePublisher(String username, String password) throws JSONException {
        ClientResponse response = authenticate(publisherUrl, genericRestClient, username, password);

        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    private ClientResponse getLifeCycleState(String assetId, String assetType, String requestLCname) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        assetTypeParamMap.put("lifecycle", requestLCname);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/asset/" + assetId + "/state"
                                , assetTypeParamMap, headerMap, cookieHeader);
        return response;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        authenticatePublisher("admin", "admin");
        deleteAsset(assetId, publisherUrl, cookieHeader, "restservice", genericRestClient);
    }
}
