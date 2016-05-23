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
package org.wso2.carbon.registry.es.utils;

import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationBaseTest;
import org.wso2.es.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ESTestBaseTest extends ESIntegrationBaseTest {
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;

    public ESTestBaseTest() {
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
    }

    /**
     * Authenticate and return jSessionId
     *
     * @param url
     * @param genericRestClient
     * @param username
     * @param password
     * @return ClientResponse
     * @throws JSONException
     */
    public ClientResponse authenticate(String url,
                                       GenericRestClient genericRestClient,
                                       String username,
                                       String password) throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(url + "/authenticate/",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "username=" + username + "&password=" + password
                        , queryParamMap, headerMap, null);
        return response;
    }

    /**
     * Create Asset
     *
     * @param resourcePath
     * @param publisherUrl
     * @param cookieHeader
     * @param genericRestClient
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ClientResponse createAsset(String resourcePath,
                                      String publisherUrl,
                                      String cookieHeader,
                                      String assetType,
                                      GenericRestClient genericRestClient)
            throws JSONException, IOException {
        queryParamMap.put("type", assetType);
        String dataBody = readFile(resourcePath);
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);

        return response;
    }

    public ClientResponse deleteAsset(String assetId,
                                      String url,
                                      String cookieHeader,
                                      String assetType,
                                      GenericRestClient genericRestClient) {
        queryParamMap.put("type", assetType);
        return genericRestClient.geneticRestRequestDelete(url + "/assets/" + assetId,
                                                          MediaType.APPLICATION_JSON,
                                                          MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
    }

    public JSONObject getAllLifeCycles(String publisherUrl,
                                       String cookieHeader,
                                       GenericRestClient genericRestClient) throws JSONException {

        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/lifecycles"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    public JSONObject getAllLifeCycleByType(String publisherUrl,
                                            String cookieHeader,
                                            GenericRestClient genericRestClient)
            throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/lifecycles"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    public ClientResponse getAsset(String assetId,
                                   String assetType,
                                   String publisherUrl,
                                   String cookieHeader,
                                   GenericRestClient genericRestClient) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        return genericRestClient.geneticRestRequestGet
                (publisherUrl + "/assets/" + assetId
                        , queryParamMap, headerMap, cookieHeader);
    }

    public JSONObject getLifeCycleState(String assetId, String assetType,
                                        String publisherUrl,
                                        String cookieHeader,
                                        GenericRestClient genericRestClient) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/assets/" + assetId + "/state"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    public JSONObject getLifeCycleData(String lifeCycleName, String publisherUrl,
                                       String cookieHeader,
                                       GenericRestClient genericRestClient) throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/lifecycles/" + lifeCycleName
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));

    }

    /**
     * Need to refresh the landing page to deploy the new rxt in publisher
     *
     * @param publisherUrl      publisher url
     * @param genericRestClient generic rest client object
     * @param cookieHeader      session cookies header
     */
    public void refreshPublisherLandingPage(String publisherUrl,
                                            GenericRestClient genericRestClient,
                                            String cookieHeader) {
        Map<String, String> queryParamMap = new HashMap<>();
        String landingUrl = publisherUrl.replace("apis", "pages/gc-landing");
        genericRestClient.geneticRestRequestGet(landingUrl, queryParamMap, headerMap, cookieHeader);
    }

    /**

     * Can be used to add new rxt configuration
     * @param fileName name of the new rxt file
     * @param resourceFileName saving name for the rxt file
     * @return true on successful addition of rxt
     * @throws Exception
     */
    public boolean addNewRxtConfiguration(String fileName, String resourceFileName)
            throws Exception {

        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL,
                getSessionCookie());

        String filePath = getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "rxt" + File.separator + fileName;
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        return resourceAdminServiceClient.addResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/"+resourceFileName,
                "application/vnd.wso2.registry-ext-type+xml", "desc", dh);
    }

    /**
     * Can be used to add default rxt configuration
     * @param defaultFileName name of the default rxt file
     * @param defaultResourceFileName saving name for the default rxt file
     * @return true on successful addition of default rxt
     * @throws Exception
     */
    public boolean defaultCustomRxtConfiguration(String defaultFileName, String defaultResourceFileName)
            throws Exception {

        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL,
                getSessionCookie());

        String filePath = getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "rxt" + File.separator + defaultFileName;
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        return resourceAdminServiceClient.addResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/"+defaultResourceFileName,
                "application/vnd.wso2.registry-ext-type+xml", "desc", dh);
    }

    /**
     * Deletion of added custom rxt file from /_system/governance/repository/components/org.wso2.carbon.governance/types
     * @param fileName name of the rxt file added
     * @throws Exception
     */
    public boolean deleteCustomRxtConfiguration(String fileName) throws Exception {
        String session = getSessionCookie();
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        return resourceAdminServiceClient.deleteResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/"+fileName);
    }


    public ClientResponse searchAssetByQuery(String publisherUrl, GenericRestClient genericRestClient, String cookieHeader, Map<String, String> queryParamMap) throws JSONException {

        ClientResponse clientResponse;
        JSONObject obj;
        double time1 = System.currentTimeMillis();
        int count = 0;
        do {
            clientResponse = genericRestClient.geneticRestRequestGet
                    (publisherUrl + "/assets", queryParamMap, headerMap, cookieHeader);
//            clientResponse = genericRestClient.geneticRestRequestGet(publisherUrl + "/assets", queryParamMap,
//                    headerMap, cookieHeader);
            Assert.assertNotNull(clientResponse, "Client Response for search rest service cannot be null");
            Assert.assertTrue((clientResponse.getStatusCode() == 200), "Wrong status code ,Expected 200 OK " +
                                                                       clientResponse.getStatusCode());
            String response = clientResponse.getEntity(String.class);
            obj = new JSONObject(response);
            double time2 = System.currentTimeMillis();
            if ((time2 - time1) > 240000) {
                log.error("Timeout while searching for assets | time waited: " + (time2 - time1));
                break;
            }
            count = count + 1;
        } while ((Double) obj.get("count") <= 0);
        double time3 = System.currentTimeMillis();
        System.out.println("Time for query the results: " + (time3 - time1));
        System.out.println("search for the rest service...." + count);
        return clientResponse;
    }

    /**
     * Get Asset By ID
     *
     * @param publisherUrl      publisher url
     * @param genericRestClient generic rest client object
     * @param cookieHeader      session cookies header
     * @param id                asset ID
     * @param queryParamMap     query ParamMap
     * @return response
     */
    public ClientResponse getAssetById(String publisherUrl, GenericRestClient genericRestClient,
                                       String cookieHeader, String id,
                                       Map<String, String> queryParamMap) {
        return genericRestClient.geneticRestRequestGet(publisherUrl + "/assets/" + id, queryParamMap,
                                                       headerMap, cookieHeader);
    }

    /**
     * Delete Assets By ID
     *
     * @param publisherUrl      publisher url
     * @param genericRestClient generic rest client object
     * @param cookieHeader      session cookies header
     * @param id                asset ID
     * @param queryParamMap     query ParamMap
     * @return response
     */
    public boolean deleteAssetById(String publisherUrl, GenericRestClient genericRestClient,
                                   String cookieHeader, String id,
                                   Map<String, String> queryParamMap) {
        ClientResponse clientResponse = this.getAssetById(publisherUrl, genericRestClient, cookieHeader, id, queryParamMap);
        if (clientResponse.getStatusCode() != 404) {
            genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + id,
                                                       MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeader);
        }
        return true;
    }

    /**
     * Get Associations By ID
     *
     * @param publisherUrl      publisher url
     * @param genericRestClient generic rest client object
     * @param cookieHeader      session cookies header
     * @param id                asset ID
     * @param queryParamMap     query ParamMap
     * @return response
     */
    public ClientResponse getAssociationsById(String publisherUrl,
                                              GenericRestClient genericRestClient,
                                              String cookieHeader, String id,
                                              Map<String, String> queryParamMap) {
        return genericRestClient.geneticRestRequestGet(publisherUrl + "/association/" + queryParamMap.get("type") + "/dependancies/" + id,
                                                       queryParamMap, headerMap, cookieHeader);
    }

    /**
     * Delete all associations by ID
     *
     * @param publisherUrl      publisher url
     * @param genericRestClient generic rest client object
     * @param cookieHeader      session cookies header
     * @param id                asset ID
     * @param queryParamMap     query ParamMap
     * @return response
     * @throws JSONException
     */
    public boolean deleteAllAssociationsById(String publisherUrl,
                                             GenericRestClient genericRestClient,
                                             String cookieHeader, String id,
                                             Map<String, String> queryParamMap)
            throws JSONException {
        boolean result = false;
        if (id != null) {
            ClientResponse clientResponse = this.getAssociationsById(publisherUrl, genericRestClient, cookieHeader, id, queryParamMap);
            JSONObject jsonObject = new JSONObject(clientResponse.getEntity(String.class));
            JSONArray assocArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < assocArray.length(); i++) {
                String assocId = (String) assocArray.getJSONObject(i).get("uuid");
                String assocShortName = (String) assocArray.getJSONObject(i).get("shortName");
                Map<String, String> assocQueryMap = new HashMap<>();
                assocQueryMap.put("type", assocShortName);
                this.deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assocId, assocQueryMap);
                this.deleteAllAssociationsById(publisherUrl, genericRestClient, cookieHeader, assocId, assocQueryMap);
            }
            result = true;
        }
        return result;
    }

    /**
     * Read Associations From the pages
     *
     * @param publisherUrl      publisher url
     * @param genericRestClient generic rest client object
     * @param cookieHeader      session cookies header
     * @param id                asset ID
     * @param queryParamMap     query ParamMap
     * @return response
     */
    public Map<String, String> getAssociationsFromPages(String publisherUrl,
                                                        GenericRestClient genericRestClient,
                                                        String cookieHeader, String id,
                                                        Map<String, String> queryParamMap) {
        String requestUrl = publisherUrl.replace("apis", "pages") + "/associations/" + queryParamMap.get("type") + "/" + id;
        System.out.println("get Association by ID: request url : " + requestUrl);
        ClientResponse clientResponse = genericRestClient.geneticRestRequestGet(requestUrl,
                                                                                queryParamMap, "text/html", headerMap, cookieHeader);
        String response = clientResponse.getEntity(String.class);
        String[] dataArray = response.split("data-uuid=");
        Map<String, String> assocMap = new HashMap<String, String>();
        for (int i = 1; i < dataArray.length; i++) {
            String mediaType = null;
            if (dataArray[i].contains("resource-type")) {
                int startIndex = dataArray[i].indexOf("resource-type");
                mediaType = dataArray[i].substring(startIndex, dataArray[i].indexOf("<", startIndex));
            }
            String uuid = dataArray[i].substring(1, dataArray[1].indexOf('\"', 1));
            if (mediaType != null) {
                assocMap.put(uuid, mediaType);
            }
        }
        return assocMap;
    }

    public ClientResponse getAssetsById(GenericRestClient genericRestClient,
                                        String publisherUrl,
                                        String assetId, String cookieHeader,
                                        String assetType) {
        queryParamMap.put("type", assetType);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/assets/"
                         + assetId, queryParamMap, headerMap, cookieHeader);
        return response;
    }

    public ClientResponse getAllAvailableAssets(GenericRestClient genericRestClient,
                                                String publisherUrl, String assetId,
                                                String cookieHeader, String assetType) {
        queryParamMap.put("type", assetType);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/assets/"
                         + assetId, queryParamMap, headerMap, cookieHeader);
        return response;
    }

    public void cleanupAsset(GenericRestClient genericRestClient, String publisherUrl,
                             String assetId, String cookieHeader, String assetType) {
        if (this.getAssetsById(genericRestClient, publisherUrl,
                               assetId, cookieHeader, assetType)
                    .getStatusCode() != 404) {
            queryParamMap.put("type", assetType);
            genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId,
                                                       MediaType.APPLICATION_JSON,
                                                       MediaType.APPLICATION_JSON
                    , queryParamMap, headerMap, cookieHeader);
        }
    }

    public String getType(String mediaType) {
        String type;
        switch (mediaType) {
            case "application/x-xsd+xml":
                type = "schema";
                break;
            case "application/vnd.wso2-service+xml":
                type = "service";
                break;
            case "application/vnd.wso2-soap-service+xml":
                type = "soapservice";
                break;
            case "application/vnd.wso2-restservice+xml":
                type = "restservice";
                break;
            case "application/policy+xml":
                type = "policy";
                break;
            case "application/vnd.wso2-endpoint+xml":
                type = "endpoint";
                break;
            case "application/vnd.wso2-notes+xml":
                type = "note";
                break;
            case "application/vnd.wso2-server+xml":
                type = "server";
                break;
            case "application/swagger+json":
                type = "swagger";
                break;
            case "application/wadl+xml":
                type = "wadl";
                break;
            case "application/wsdl+xml":
                type = "wsdl";
                break;
            default:
                type = null;
                break;
        }
        return type;
    }

}
