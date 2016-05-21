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
import org.json.JSONArray;
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

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GRegPublisherLifecycleHistoryTest extends ESTestBaseTest {

    private TestUserMode userMode;
    private GenericRestClient genericRestClient;
    private String publisherUrl;
    private String storeUrl;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String cookieHeader;
    private String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                                  + "artifacts" + File.separator + "GREG" + File.separator;
    private String jSessionId;
    private String lifeCycleName = "ServiceLifeCycle";
    private String assetId;
    private String cookieHeaderStore;

    @Factory(dataProvider = "userModeProvider")
    public GRegPublisherLifecycleHistoryTest(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        storeUrl = storeContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "store/apis");
    }

    @BeforeMethod(alwaysRun = true)
    public void resetParameters() {
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    protected void testPrepareForTestRun()
            throws Exception {
        queryParamMap.put("type", "restservice");
        queryParamMap.put("lifecycle", lifeCycleName);
        genericRestClient = new GenericRestClient();

        super.init(userMode);

        authenticatePublisher("admin", "admin");

        //Add a rest service
        ClientResponse response = createAsset(resourcePath + "json" + File.separator +
                                              "RESTserviceforcheckingLChistory.json", publisherUrl,
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

        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", "restservice");

        response = genericRestClient.geneticRestRequestGet(storeUrl + "/assets/" + assetId , assetTypeParamMap ,
                                                           MediaType.APPLICATION_JSON, headerMap, cookieHeaderStore);
        obj = new JSONObject(response.getEntity(String.class));
        String state = obj.getJSONObject("data").getString("lifecycleState");

        Assert.assertTrue(state.equals("Development"), "LifeCycle not assigned to given assert");

        //promote rest service to testing
        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                                                            MediaType.APPLICATION_FORM_URLENCODED,
                                                            MediaType.APPLICATION_JSON,
                                                            "nextState=Testing&comment=Development Completed",
                                                            queryParamMap, headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());

        response = genericRestClient.geneticRestRequestGet(storeUrl + "/assets/" + assetId , assetTypeParamMap ,
                                                           MediaType.APPLICATION_JSON, headerMap, cookieHeaderStore);
        obj = new JSONObject(response.getEntity(String.class));
        state = obj.getJSONObject("data").getString("lifecycleState");

        Assert.assertTrue(state.equals("Testing"), "LifeCycle not assigned to given assert");

        //promote rest service to production
        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                                                            MediaType.APPLICATION_FORM_URLENCODED,
                                                            MediaType.APPLICATION_JSON,
                                                            "nextState=production&comment=Testing Completed",
                                                            queryParamMap, headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());

        response = genericRestClient.geneticRestRequestGet(storeUrl + "/assets/" + assetId , assetTypeParamMap ,
                                                           MediaType.APPLICATION_JSON, headerMap, cookieHeaderStore);
        obj = new JSONObject(response.getEntity(String.class));
        state = obj.getJSONObject("data").getString("lifecycleState");

        Assert.assertTrue(state.equals("Production"), "LifeCycle not assigned to given assert");

        //delete rest service
        deleteAsset(assetId, publisherUrl, cookieHeader, "restservice", genericRestClient);

        //add a new rest service with same resource path
        response = createAsset(resourcePath + "json" + File.separator +
                               "RESTserviceforcheckingLChistory.json", publisherUrl,
                               cookieHeader, "restservice", genericRestClient);

        obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        assetId = obj.get("id").toString();
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        response = getAsset(assetId, "restservice", publisherUrl, cookieHeader, genericRestClient);
        obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(obj.get("lifecycle").equals(lifeCycleName), "LifeCycle not assigned to given assert");

        assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", "restservice");

        response = genericRestClient.geneticRestRequestGet(storeUrl + "/assets/" + assetId , assetTypeParamMap ,
                                                           MediaType.APPLICATION_JSON, headerMap, cookieHeaderStore);
        obj = new JSONObject(response.getEntity(String.class));
        state = obj.getJSONObject("data").getString("lifecycleState");

        Assert.assertTrue(state.equals("Development"), "LifeCycle not assigned to given assert");

        //promote asset to testing
        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                                                            MediaType.APPLICATION_FORM_URLENCODED,
                                                            MediaType.APPLICATION_JSON,
                                                            "nextState=Testing&comment=Development Completed",
                                                            queryParamMap, headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());

        response = genericRestClient.geneticRestRequestGet(storeUrl + "/assets/" + assetId , assetTypeParamMap ,
                                                           MediaType.APPLICATION_JSON, headerMap, cookieHeaderStore);
        obj = new JSONObject(response.getEntity(String.class));
        state = obj.getJSONObject("data").getString("lifecycleState");

        Assert.assertTrue(state.equals("Testing"), "LifeCycle not assigned to given assert");

        //promote asset to production
        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                                                            MediaType.APPLICATION_FORM_URLENCODED,
                                                            MediaType.APPLICATION_JSON,
                                                            "nextState=production&comment=Testing Completed",
                                                            queryParamMap, headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());

        response = genericRestClient.geneticRestRequestGet(storeUrl + "/assets/" + assetId , assetTypeParamMap ,
                                                           MediaType.APPLICATION_JSON, headerMap, cookieHeaderStore);
        obj = new JSONObject(response.getEntity(String.class));
        state = obj.getJSONObject("data").getString("lifecycleState");

        Assert.assertTrue(state.equals("Production"), "LifeCycle not assigned to given assert");

        //demote asset to testing
        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                                                            MediaType.APPLICATION_FORM_URLENCODED,
                                                            MediaType.APPLICATION_JSON,
                                                            "nextState=Testing&comment=Not Production Ready",
                                                            queryParamMap, headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
        response = genericRestClient.geneticRestRequestGet(storeUrl + "/assets/" + assetId , assetTypeParamMap ,
                                                           MediaType.APPLICATION_JSON, headerMap, cookieHeaderStore);
        obj = new JSONObject(response.getEntity(String.class));
        state = obj.getJSONObject("data").getString("lifecycleState");

        Assert.assertTrue(state.equals("Testing"), "LifeCycle not assigned to given assert");

        //demote asset to development
        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                                                            MediaType.APPLICATION_FORM_URLENCODED,
                                                            MediaType.APPLICATION_JSON,
                                                            "nextState=development&comment=Testing failed",
                                                            queryParamMap, headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());

        response = genericRestClient.geneticRestRequestGet(storeUrl + "/assets/" + assetId , assetTypeParamMap ,
                                                           MediaType.APPLICATION_JSON, headerMap, cookieHeaderStore);
        obj = new JSONObject(response.getEntity(String.class));
        state = obj.getJSONObject("data").getString("lifecycleState");

        Assert.assertTrue(state.equals("Development"), "LifeCycle not assigned to given assert");

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "PromoteLifeCycle with fault user",
          dependsOnMethods = {"testPrepareForTestRun"})
    public void CheckLCHistory() throws LogViewerLogViewerException, RemoteException,
                                                                      JSONException {
        ClientResponse response = getLifeCycleHistory(assetId, "restservice", lifeCycleName);

        Assert.assertTrue(response.getStatusCode() == 200, "Fault user accepted");

        JSONObject historyObj = new JSONObject(response.getEntity(String.class));
        JSONArray dataObj = historyObj.getJSONArray("data");

        Assert.assertEquals(((JSONObject) ((JSONObject) dataObj.get(0)).getJSONArray("action").
                get(0)).getString("name"), "Demote");
        Assert.assertEquals(((JSONObject) ((JSONObject) dataObj.get(1)).getJSONArray("action").
                get(0)).getString("name"), "Demote");
        Assert.assertEquals(((JSONObject) ((JSONObject) dataObj.get(2)).getJSONArray("action").
                get(0)).getString("name"), "Promote");
        Assert.assertEquals(((JSONObject) ((JSONObject) dataObj.get(3)).getJSONArray("action").
                get(0)).getString("name"), "Promote");

        Assert.assertEquals(dataObj.length(), 8);
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

        JSONObject objSessionStore =
                new JSONObject(authenticate(storeUrl, genericRestClient, username, password)
                                       .getEntity(String.class));
        jSessionId = objSessionStore.getJSONObject("data").getString("sessionId");
        cookieHeaderStore = "JSESSIONID=" + jSessionId;
    }

    private ClientResponse getLifeCycleHistory(String assetId, String assetType, String requestLCname)
            throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        assetTypeParamMap.put("lifecycle", requestLCname);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/asset/" + assetId + "/lifecycle-history"
                                , assetTypeParamMap, headerMap, cookieHeader);
        return response;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        deleteAsset(assetId, publisherUrl, cookieHeader, "restservice", genericRestClient);
    }
}
