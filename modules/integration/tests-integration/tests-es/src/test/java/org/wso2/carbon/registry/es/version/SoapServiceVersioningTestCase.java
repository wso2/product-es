/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.es.version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.GenericRestClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

/**
 * This class is responsible for testing the versioning of a soapservice.
 */
public class SoapServiceVersioningTestCase extends ESTestBaseTest {

    private static final Log log = LogFactory.getLog(SoapServiceVersioningTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String versionedAssetId;
    String wsdlAssetId;
    String assetName;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> headerMap;
    String publisherUrl;
    String publisherUrlForVersion;
    String resourcePath;
    private static final String NEW_VERSION = "5.0.0";
    Map<String, String> assocUUIDMap;
    public static final String RXT_STORAGE_PATH =
            "/_system/governance/repository/components/org.wso2.carbon.governance/types/endpoint.rxt";
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @Factory(dataProvider = "userModeProvider")
    public SoapServiceVersioningTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        headerMap = new HashMap<>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        publisherUrlForVersion = automationContext.getContextUrls().getSecureServiceUrl().replace("services",
                                                                                                  "publisher/assets");
        String session = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        updateEndpointRxt();
        setTestEnvironment();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Version a soap service")
    public void versionSoapService()
            throws IOException, JSONException,
                   InterruptedException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wsdl");
        createWSDLAsset();
        assocUUIDMap = getAssociationsFromPages(publisherUrl, genericRestClient, cookieHeader, wsdlAssetId, queryParamMap);
        queryParamMap.clear();
        if (assocUUIDMap != null) {
            for (String uuid : assocUUIDMap.keySet()) {
                if ("soapservice".equals(getType(assocUUIDMap.get(uuid)))) {
                    assetId = uuid;
                    queryParamMap.put("type", getType(assocUUIDMap.get(uuid)));
                    getSoapServiceAsset(assetId);
                }

            }
        }
        //Call the version api
        queryParamMap.clear();
        ClientResponse response = genericRestClient.geneticRestRequestGet(publisherUrlForVersion + "/soapservice/copy/"
                                                                          + assetId,
                                                                          queryParamMap,
                                                                          headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 Ok ,Received " +
                          response.getStatusCode());
        //Create the versioned soapservice.
        queryParamMap.put("type", "soapservice");
        String soapVersionTemplate = readFile(resourcePath + "json" + File.separator + "soapservice-version.json");
        String dataBody = String.format(soapVersionTemplate, NEW_VERSION);
        ClientResponse createVersionResponse =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/create-version",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);

        JSONObject obj = new JSONObject(createVersionResponse.getEntity(String.class));

        Assert.assertTrue((createVersionResponse.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 Ok ,Received " +
                          response.getStatusCode());
        versionedAssetId = (String) obj.get("data");
        Assert.assertNotNull(versionedAssetId, "Empty id of the versioned soap service" +
                                               createVersionResponse.getEntity(String.class));
        if (versionedAssetId != null) {
            getSoapServiceAsset(versionedAssetId);
        }

    }

    /**
     * Method used to update endpoint RXT
     */
    private void updateEndpointRxt()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException, InterruptedException {
        StringBuilder builder = new StringBuilder();
        builder.append(getTestArtifactLocation()).append("artifacts").append(File.separator).append("GREG").
                append(File.separator).append("rxt").append(File.separator).append("endpoint.rxt");
        String filePath = builder.toString();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        StringBuilder sb = new StringBuilder();
        try {
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
        } catch (IOException e) {
            log.error("Error while reading endpoint.rxt file",e);
        } finally {
            br.close();
        }
        resourceAdminServiceClient
                .updateTextContent(RXT_STORAGE_PATH, sb.toString());
    }

    /**
     * This method creates a wsdl from a url
     *
     * @throws JSONException
     * @throws InterruptedException
     * @throws IOException
     */
    private void createWSDLAsset()
            throws JSONException, InterruptedException, IOException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wsdl");
        String wsdlTemplate = readFile(resourcePath + "json" + File.separator + "wsdl-sample.json");
        assetName = "echo.wsdl";
        String dataBody = String.format(wsdlTemplate,
                                        "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wsdl/echo.wsdl",
                                        assetName,
                                        "1.0.0");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        String resultName = obj.get("overview_name").toString();
        Assert.assertEquals(resultName, assetName);
        searchWsdlAsset();
    }

    /**
     * This method retrieves a soapservice when provide its uuid
     *
     * @param assetId the uuid of the created soapservice
     * @throws JSONException
     */
    public void getSoapServiceAsset(String assetId) throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "soapservice");
        ClientResponse clientResponse = getAssetById(publisherUrl, genericRestClient, cookieHeader, assetId,
                                                     queryParamMap);
        Assert.assertTrue((clientResponse.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK " +
                          clientResponse.getStatusCode());
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        Assert.assertEquals(obj.get("id"), assetId);
    }

    private void setTestEnvironment() throws JSONException, XPathExpressionException {
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                                            automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                            automationContext.getSuperTenant().getTenantAdmin().getPassword())
                                       .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    /**
     * This method get all the wsdls in publisher and select the one created by createWSDLAsset method.
     *
     * @throws JSONException
     */
    public void searchWsdlAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wsdl");
        ClientResponse clientResponse = searchAssetByQuery(publisherUrl, genericRestClient, cookieHeader, queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = (String) jsonArray.getJSONObject(i).get("name");
            if (assetName.equals(name)) {
                wsdlAssetId = (String) jsonArray.getJSONObject(i).get("id");
                //path = (String) jsonArray.getJSONObject(i).get("path");
                break;
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException {

        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wsdl");
        assocUUIDMap = getAssociationsFromPages(publisherUrl, genericRestClient, cookieHeader, wsdlAssetId, queryParamMap);
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, wsdlAssetId, queryParamMap);
        //deleteAllAssociationsById(publisherUrl, genericRestClient, cookieHeader, wsdlAssetId, queryParamMap);
        queryParamMap.clear();
        for (String uuid : assocUUIDMap.keySet()) {
            queryParamMap.put("type", getType(assocUUIDMap.get(uuid)));
            deleteAssetById(publisherUrl, genericRestClient, cookieHeader, uuid, queryParamMap);
        }
        queryParamMap.put("type", "soapservice");
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, versionedAssetId, queryParamMap);
        queryParamMap.clear();
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}
