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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.generic.stub.ManageGenericArtifactServiceRegistryExceptionException;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.es.integration.common.clients.LifeCycleManagementClient;
import org.wso2.es.integration.common.clients.ManageGenericArtifactAdminServiceClient;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class RestResourceLifeCycleManagementTestCase extends ESTestBaseTest {
    private static final Log log =
            LogFactory.getLog(RestResourceLifeCycleManagementTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourceRegistryPath;
    ManageGenericArtifactAdminServiceClient manageGenericArtifactAdminServiceClient;
    LifeCycleManagementClient lifeCycleAdminServiceClient;
    ResourceAdminServiceClient resourceAdminServiceClient;
    private ServerConfigurationManager serverConfigurationManager;
    String resourcePath;
    String lifeCycleName;
    String restServiceResourcePath;

    @Factory(dataProvider = "userModeProvider")
    public RestResourceLifeCycleManagementTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        publisherUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        manageGenericArtifactAdminServiceClient =
                new ManageGenericArtifactAdminServiceClient(backendURL, sessionCookie);
        lifeCycleAdminServiceClient = new LifeCycleManagementClient(backendURL, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        serverConfigurationManager = new ServerConfigurationManager(automationContext);
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator;
        resourceRegistryPath = "/_system/governance/repository/components" +
                               "/org.wso2.carbon.governance/types/updated-serviceLC.rxt";
        lifeCycleName = "ServiceLifeCycleLC2";
        restServiceResourcePath = "/_system/governance/trunk/restservices/1.0.0/testservice1234";
    }

    @BeforeMethod
    public void resetParameters() {
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "username=admin&password=admin"
                        , queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Add lifecycle for rest resources",
            dependsOnMethods = {"authenticatePublisher"})
    public void addLifecycleForRestServiceResources()
            throws IOException, LifeCycleManagementServiceExceptionException,
                   ManageGenericArtifactServiceRegistryExceptionException, InterruptedException,
                   ResourceAdminServiceExceptionException, JSONException,
                   AutomationFrameworkException {
        lifeCycleAdminServiceClient.addLifeCycle(readFile(resourcePath + "lifecycle"
                                                          + File.separator + "updated-serviceLC.xml"));
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath + "rxt"
                                                 + File.separator + "updated-serviceLC.rxt"));
        resourceAdminServiceClient.addResource(resourceRegistryPath,
                                               "application/vnd.wso2.registry-ext-type+xml",
                                               "TstDec", dh);
        Assert.assertTrue(getAllLifeCycles().get("data").toString().contains(lifeCycleName),
                          "LifeCycle not Added");

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test",
            dependsOnMethods = {"addLifecycleForRestServiceResources", "authenticatePublisher"})
    public void createRestServiceAssetWithLC()
            throws JSONException, InterruptedException,
                   IOException, ResourceAdminServiceResourceServiceExceptionException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator
                                   + "publisherPublishRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        assetId = obj.get("id").toString();
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        Assert.assertTrue(this.getAsset(assetId, "restservice").get("lifecycle")
                                  .equals(lifeCycleName), "LifeCycle not assigned to given assert");

        resourceAdminServiceClient.addResourcePermission(restServiceResourcePath, "manager", "3", "1");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "PromoteLifeCycle with fault user",
            dependsOnMethods = {"addLifecycleForRestServiceResources"
                    , "authenticatePublisher", "createRestServiceAssetWithLC"})
    public void promoteLifeCycleWithUserWithOutRole()
            throws JSONException, InterruptedException, IOException {
        queryParamMap.put("type", "restservice");
        queryParamMap.put("lifecycle", "ServiceLifeCycleLC2");
        JSONObject LCStateobj = getLifeCycleState(assetId, "restservice");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");

        //first 2 check list items should be available for non-manager role user
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(1)).getString("isVisible"), "true");
        //checklist item 2 should not be available to non-manager role user
        Assert.assertEquals(((JSONObject) checkItems.get(2)).getString("isVisible"), "null");

        //As check item 2 is not checked promote action is not available
        JSONObject approvedActionsObj = dataObj.getJSONObject("approvedActions");
        Assert.assertEquals(approvedActionsObj.length(), 0);
        
        Assert.assertTrue(checkLifeCycleCheckItem(cookieHeader, 0).getStatusCode()==200);
        checkLifeCycleCheckItem(cookieHeader, 1);
        ClientResponse responseCheck1 = checkLifeCycleCheckItem(cookieHeader,2);

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "nextState=Testing&comment=Completed"
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(response.getStatusCode() == 500, "Fault user accepted");
        Assert.assertTrue(obj.get("message").toString().contains("Failed to update asset lifecycle of asset")
                , "LifeCycle promoted for wrong user");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "PromoteLifeCycle with fault user",
            dependsOnMethods = {"addLifecycleForRestServiceResources"
                    , "authenticatePublisher", "createRestServiceAssetWithLC", "promoteLifeCycleWithUserWithOutRole"})
    public void promoteLifeCycleWithFCorrectUserWithRole()
            throws JSONException, InterruptedException, IOException {
        queryParamMap.put("type", "restservice");
        queryParamMap.put("lifecycle", lifeCycleName);
        ClientResponse responseUser =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "username=manager&password=manager"
                        , queryParamMap, headerMap, null);

        JSONObject obj = new JSONObject(responseUser.getEntity(String.class));
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;

        getLifeCycleData(lifeCycleName, cookieHeader);

        JSONObject LCStateobj = getLifeCycleState(assetId, "restservice");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");

        //all 3 check list items should be available for manager role user
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(1)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(2)).getString("isVisible"), "true");

        ClientResponse responseCheck1;
        responseCheck1 =
                checkLifeCycleCheckItem(cookieHeader, 0);
        responseCheck1 =
                checkLifeCycleCheckItem(cookieHeader,1);
        responseCheck1 =
                checkLifeCycleCheckItem(cookieHeader,2);

        //As user needs admin role to perform promote action, promote action is not available
        JSONObject approvedActionsObj = dataObj.getJSONObject("approvedActions");
        Assert.assertEquals(approvedActionsObj.length(), 0);

        JSONObject objCheck1 = new JSONObject(responseCheck1.getEntity(String.class));
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "nextState=Testing&comment=Completed"
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj2 = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(response.getStatusCode() == 500, "Fault user accepted");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "PromoteLifeCycle with correct user",
          dependsOnMethods = {"promoteLifeCycleWithFCorrectUserWithRole"})
    public void promoteLifeCycleWithUserWithcorrectRolePermission() throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "username=admin&password=admin"
                        , queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;

        getLifeCycleData(lifeCycleName, cookieHeader);

        queryParamMap.put("type", "restservice");
        queryParamMap.put("lifecycle", lifeCycleName);
        JSONObject LCStateobj = getLifeCycleState(assetId, "restservice");
        JSONObject dataObj = LCStateobj.getJSONObject("data");

        //As user has admin role and checkitem with index 2 is checked, promote action should be available
        JSONObject approvedActionsObj = dataObj.getJSONObject("approvedActions");
        Assert.assertEquals(approvedActionsObj.length(), 1);
        Assert.assertEquals(approvedActionsObj.getString("Promote"), "true");

        response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "nextState=Testing&comment=Completed"
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj2 = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(response.getStatusCode() == 200, "Correct user could not change LC state");

        LCStateobj = getLifeCycleState(assetId, "restservice");
        dataObj = LCStateobj.getJSONObject("data");

        Assert.assertEquals(dataObj.getString("id"), "Testing");

        approvedActionsObj = dataObj.getJSONObject("approvedActions");
        Assert.assertEquals(approvedActionsObj.length(), 1);
        Assert.assertEquals(approvedActionsObj.getString("Demote"), "true");

        JSONArray checkItems = dataObj.getJSONArray("checkItems");

        //first 2 check list items should be available for non-manager role user
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(1)).getString("isVisible"), "true");
        //checklist item 2 should not be available to non-manager role user
        Assert.assertEquals(((JSONObject) checkItems.get(2)).getString("isVisible"), "null");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException, ResourceAdminServiceExceptionException, JSONException, AutomationUtilException {
        genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                                                 MediaType.APPLICATION_FORM_URLENCODED,
                                                 MediaType.APPLICATION_JSON,
                                                 "username=admin&password=admin", queryParamMap, headerMap, null);
        queryParamMap.put("type", "restservice");
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId,
                                                   MediaType.APPLICATION_JSON,
                                                   MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
        resourceAdminServiceClient.deleteResource(resourceRegistryPath);
        lifeCycleAdminServiceClient.deleteLifeCycle(lifeCycleName);

        serverConfigurationManager.restartGracefully();
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

    private JSONObject getAllLifeCycles() throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/lifecycles"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    private JSONObject getAllAssetsByType(String assetType) throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/lifecycles"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    private JSONObject getAsset(String assetId, String assetType) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/assets/" + assetId
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    private JSONObject getLifeCycleState(String assetId, String assetType) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        assetTypeParamMap.put("lifecycle",lifeCycleName);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/asset/" + assetId + "/state"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    private JSONObject getLifeCycleData(String lifeCycleName, String cookie) throws JSONException {
        // https://localhost:10343/publisher/apis/lifecycles/ServiceLifeCycleLC2
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/lifecycles/" + lifeCycleName
                                , queryParamMap, headerMap, cookie);
        return new JSONObject(response.getEntity(String.class));

    }

    private ClientResponse checkLifeCycleCheckItem(String managerCookieHeader,int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":"+itemId+",\"checked\":true}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

//    private JSONObject getLifeCycleState(String lifeCycleName, String cookie) throws JSONException {
//        ClientResponse response =
//                genericRestClient.geneticRestRequestGet
//                        (publisherUrl + "/lifecycles/" + lifeCycleName
//                                , queryParamMap, headerMap, cookie);
//        return new JSONObject(response.getEntity(String.class));
//
//    }
//
   // https://localhost:9443/publisher/apis/asset/82c1b281-b9ee-4733-b28f-0bba3f954613/state?type=restservice&lifecycle=ServiceLifeCycle

}
