/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.utils;


import org.apache.wink.client.ClientResponse;
import org.wso2.es.integration.common.utils.GenericRestClient;

import java.io.IOException;
import java.util.Map;
import javax.ws.rs.core.MediaType;

/**
 * This class contains utility methods for governance REST api.
 */
public class GovernanceRestApiUtil {

    /**
     * @param genericRestClient generic rest client instance
     * @param dataBody          data body for REST request
     * @param queryParamMap     query parameters
     * @param headerMap         header parameters
     * @return response for asset POST request
     * @throws IOException
     */
    public static ClientResponse createAsset(GenericRestClient genericRestClient, String dataBody,
                                             Map<String, String> queryParamMap, Map<String, String> headerMap,
                                             String governaceAPIUrl)
            throws IOException {

        return genericRestClient.geneticRestRequestPost(governaceAPIUrl, MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap,
                                                        null);
    }

    /**
     * @param genericRestClient generic rest client instance
     * @param dataBody          data body for REST request
     * @param queryParamMap     query parameters
     * @param headerMap         header parameters
     * @param governaceAPIUrl   rest api url for the request
     * @return response for asset PUT request
     * @throws IOException
     */
    public static ClientResponse updateAsset(GenericRestClient genericRestClient, String dataBody,
                                             Map<String, String> queryParamMap, Map<String, String> headerMap,
                                             String governaceAPIUrl)
            throws IOException {

        return genericRestClient.genericRestRequestPut(governaceAPIUrl, MediaType.APPLICATION_JSON,
                                                       MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap,
                                                       null);
    }

    /**
     * @param genericRestClient generic rest client instance
     * @param queryParamMap     query parameters
     * @param headerMap         header parameters
     * @param governaceAPIUrl   rest api url for the request
     * @return response for asset GET request
     */
    public static ClientResponse getAssetById(GenericRestClient genericRestClient, Map<String, String> queryParamMap,
                                              Map<String, String> headerMap, String governaceAPIUrl) {

        return genericRestClient.geneticRestRequestGet(governaceAPIUrl, queryParamMap, headerMap, null);
    }

    /**
     *
     * @param genericRestClient generic rest client instance
     * @param queryParamMap query parameters
     * @param headerMap header parameters
     * @param governaceAPIUrl rest api url for the request
     * @return response for the endpoint ID get request
     */
    public static ClientResponse getEndpointByID(GenericRestClient genericRestClient, Map<String, String> queryParamMap,
                                           Map<String, String> headerMap,String governaceAPIUrl) {
        String governanceRestApiUrl = governaceAPIUrl;
        return GovernanceRestApiUtil.getAssetById(genericRestClient, queryParamMap, headerMap,
                                                  governanceRestApiUrl);
    }

    /**
     * This method creates the endpoint data body.
     *
     * @return formatted databody of endpoint
     */
    public static String createEndpointDataBody(String template, String endpointName, String enviornment) {
        return String.format(template, endpointName, enviornment);
    }

}
