/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.notes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.es.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class AssertNotesESTestCase extends ESTestBaseTest {
    private static final Log log = LogFactory.getLog(AssertNotesESTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String assetType;
    String cookieHeaderPublisher;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    String noteName;
    String noteOverviewHash;
    String replyOverviewHash;
    String noteAssetId;
    String replyAssetId;

    @Factory(dataProvider = "userModeProvider")
    public AssertNotesESTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        noteName = "testNote33";
        assetType = "restservice";
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator + "json" + File.separator;
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        SetTestEnvironment();
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, alwaysRun = true,description = "Add note to asset test")
    public void addNoteToAsset() throws JSONException, IOException {
        queryParamMap.put("type", "note");
        //  https://localhost:9443/publisher/apis/assets?type=note
        String dataBody = String.format(readFile(resourcePath + "publisherAddNoteRestResource.json")
                , assetType, "testservice1234", noteName);
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeaderPublisher);
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 OK ,Received " +
                          response.getStatusCode());
        JSONObject responseObj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(responseObj.getJSONObject("attributes").get("overview_note").toString().contains(noteName), "Does not create a note");
        Assert.assertTrue(responseObj.getJSONObject("attributes").get("overview_resourcepath").toString().contains(assetType),"Fault resource path for note");
        noteOverviewHash = responseObj.getJSONObject("attributes").get("overview_hash").toString();
        Assert.assertNotNull(noteOverviewHash);
        noteAssetId =responseObj.get("id").toString();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, dependsOnMethods = "addNoteToAsset", description = "Add Reply to note added for a Asset test")
    public void addReplyToNoteNote() throws JSONException, IOException {
        queryParamMap.put("type", "note");
        //  https://localhost:9443/publisher/apis/assets?type=note
        String dataBody = String.format(readFile(resourcePath + "publisherNoteReplyRestResource.json")
                , noteOverviewHash,"replyNote123",noteOverviewHash);
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeaderPublisher);
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());

        JSONObject responseObj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(responseObj.getJSONObject("attributes").get("overview_note").toString().contains("replyNote123"), "Does not create a note");
        Assert.assertTrue(responseObj.getJSONObject("attributes").get("overview_resourcepath").toString().contains(noteOverviewHash),"Fault resource path for note");
        replyOverviewHash= responseObj.getJSONObject("attributes").get("overview_hash").toString();
        Assert.assertNotNull(replyOverviewHash);
        replyAssetId =responseObj.get("id").toString();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, dependsOnMethods = "addReplyToNoteNote", description = "Delete Reply to note added for a Asset test")
    public void deleteReplyToNoteNote() throws JSONException, IOException {
        queryParamMap.put("type", "note");
        //  https://localhost:9443/publisher/apis/assets?type=note
        String dataBody = readFile(resourcePath + "publisherAddNoteRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + replyAssetId,
                                                           MediaType.APPLICATION_JSON,
                                                           MediaType.APPLICATION_JSON
                        , queryParamMap, headerMap, cookieHeaderPublisher);
        response.getEntity(String.class).contains("Asset Deleted Successfully");
        // isTagAvailablePublisher("testTag");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, dependsOnMethods = "deleteReplyToNoteNote", description = "Delete Note test")
    public void deleteNote() throws JSONException, IOException {
        queryParamMap.put("type", "note");
        //  https://localhost:9443/publisher/apis/assets?type=note
        String dataBody = readFile(resourcePath + "publisherAddNoteRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + noteAssetId,
                                                           MediaType.APPLICATION_JSON,
                                                           MediaType.APPLICATION_JSON
                        , queryParamMap, headerMap, cookieHeaderPublisher);

        // isTagAvailablePublisher("testTag");
        response.getEntity(String.class).contains("Asset Deleted Successfully");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        deleteAsset(assetId, publisherUrl, cookieHeaderPublisher, assetType, genericRestClient);
    }

    private void SetTestEnvironment() throws JSONException, XPathExpressionException,
                                             IOException {
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                                            automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                            automationContext.getSuperTenant().getTenantAdmin().getPassword())
                                       .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeaderPublisher = "JSESSIONID=" + jSessionId;
        JSONObject objSessionId = new JSONObject(createAsset(resourcePath + "publisherPublishRestResource.json"
                , publisherUrl,
                                                             cookieHeaderPublisher, assetType,
                                                             genericRestClient).getEntity(String.class));
        assetId = objSessionId.get("id").toString();
    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}
