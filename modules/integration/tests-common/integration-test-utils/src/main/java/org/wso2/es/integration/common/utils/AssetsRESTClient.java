/*
 * Copyright (c) 2014, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.es.integration.common.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class AssetsRESTClient extends ESIntegrationTest {
    private static final String BASE_URL = "https://localhost:9443";
    private static final String PUBLISHER_APIS_AUTHENTICATE = "/publisher/apis/authenticate?";
    private static final String PUBLISHER_APIS_LIST_GADGETS_ENDPOINT = "/publisher/apis/assets?type=gadget&count=12";
    private static final String PUBLISHER_APIS_LOGOUT_ENDPOINT = "/publisher/apis/logout";

    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String COOKIE = "Cookie";
    private static final String JSESSIONID = "JSESSIONID";

    private static final String DATA = "data";
    private static final String SESSIONID = "sessionId";

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String USERNAME_VAL = "admin";
    private static final String PASSWORD_VAl = "admin";

    private static final int DEFAULT_PAGE_SIZE = 12;

    private JsonParser parser = new JsonParser();
    private static final Log LOG = LogFactory.getLog(AssetsRESTClient.class);

    /**
     * This methods make a call to ES-Publisher REST API and obtain a valid sessionID
     *
     * @return SessionId for the authenticated user
     */
    private String login() {
        String sessionID = null;
        DataInputStream input = null;
        String authenticateEndpoint = BASE_URL + PUBLISHER_APIS_AUTHENTICATE;
        //construct full authenticate endpoint
        try {
            //authenticate endpoint URL
            URL endpointUrl = new URL(authenticateEndpoint);
            URLConnection urlConn = endpointUrl.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            // Specify the content type.
            urlConn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE);
            // Send POST output.
            DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());
            String content = USERNAME + "=" + URLEncoder.encode(USERNAME_VAL) + "&" + PASSWORD + "=" +
                    URLEncoder.encode(PASSWORD_VAl);
            printout.writeBytes(content);
            printout.flush();
            printout.close();
            // Get response data.
            input = new DataInputStream(urlConn.getInputStream());
            String str;
            StringBuilder response = new StringBuilder();
            while ((str = input.readLine()) != null) {
                response.append(str);
            }
            JsonElement elem = parser.parse(response.toString());
            sessionID = elem.getAsJsonObject().getAsJsonObject(DATA).get(SESSIONID).toString();

        } catch (MalformedURLException e) {
            LOG.error(getLoginErrorMassage(authenticateEndpoint), e);
        } catch (IOException e) {
            LOG.error(getLoginErrorMassage(authenticateEndpoint), e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return sessionID;
    }

    /**
     * This method is used to retrieve assets via publisher assets list endpoint
     *
     * @param sessionId String of valid session ID
     * @return JSON ARRAY of gadgets
     */
    private JsonArray getData(String sessionId) {
        DataInputStream input = null;
        String listAssetsEndpoint = BASE_URL + PUBLISHER_APIS_LIST_GADGETS_ENDPOINT;
        //endpoint which retrieves list of gadgets
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("###### Get Assets via REST endpoint ######");
            }
            URL endpointUrl = new URL(listAssetsEndpoint);
            URLConnection urlConn = endpointUrl.openConnection();
            urlConn.setRequestProperty(COOKIE, JSESSIONID + "=" + sessionId + ";");
            // SessionId Cookie
            urlConn.connect();
            //GET response data
            input = new DataInputStream(urlConn.getInputStream());
            StringBuilder response = new StringBuilder();
            String str;
            while ((str = input.readLine()) != null) {
                response.append(str);
            }
            input.close();
            parser = new JsonParser();
            JsonElement elem = parser.parse(response.toString());
            // parse response to a JasonArray
            return elem.getAsJsonObject().getAsJsonArray(DATA);

        } catch (MalformedURLException e) {
            LOG.error(getAssetRetrievingErrorMassage(listAssetsEndpoint), e);
        } catch (IOException e) {
            LOG.error(getAssetRetrievingErrorMassage(listAssetsEndpoint), e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * This method send a request to publisher logout api to invalidate the given sessionID
     *
     * @param sessionId String of valid session ID
     */
    private void logOut(String sessionId) {
        DataOutputStream printout = null;
        String logoutEndpoint = BASE_URL + PUBLISHER_APIS_LOGOUT_ENDPOINT;
        try {
            //authenticate endpoint
            URL endpointUrl = new URL(logoutEndpoint);
            URLConnection urlConn = endpointUrl.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            // Specify the content type.
            urlConn.setRequestProperty(COOKIE, JSESSIONID + "=" + sessionId + ";");
            //send SessionId Cookie
            //send POST output.
            printout = new DataOutputStream(urlConn.getOutputStream());
            printout.flush();
        } catch (MalformedURLException e) {
            LOG.error(getLogoutErrorMassage(logoutEndpoint), e);
        } catch (IOException e) {
            LOG.error(getLogoutErrorMassage(logoutEndpoint), e);
        } finally {
            if (printout != null) {
                try {
                    printout.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Public method to check whether the indexing is completed
     *
     * @return true if completed false otherwise
     */
    public boolean isIndexCompleted() {
        String sessionId = null;
        JsonArray assets;
        try {
            sessionId = login();
            assets = getData(sessionId);
            if (assets.size() == DEFAULT_PAGE_SIZE) {
                LOG.info("###### Completed Indexing ######");
                return true;
            } else {
                LOG.info("###### Indexing is not yet completed ######");
                return false;
            }
        } catch (Exception e) {
            //ignoring since code will re-attempt
        } finally {
            if (sessionId != null) {
                try {
                    logOut(sessionId);
                } catch (Exception e) {
                    //ignoring since code will re-attempt
                }
            }
        }
        return false;
    }

    /**
     * Error massage when asset retrieval fails
     *
     * @param endpoint is the endpoint URL exposed for Asset Listing
     * @return Error massage as a String
     */
    private String getAssetRetrievingErrorMassage(String endpoint) {
        return "Error while retrieving gadgets via  " + endpoint;
    }

    /**
     * Error massage when login fails
     *
     * @param endpoint is the endpoint URL exposed for authenticating to access publisher apis
     * @return Error massage as a String
     */
    private String getLoginErrorMassage(String endpoint) {
        return "Error while authenticating to publisher apis via " + endpoint;
    }

    /**
     * Error massage when logout fails
     *
     * @param endpoint is the endpoint URL exposed for logout from apis
     * @return Error massage as a String
     */
    private String getLogoutErrorMassage(String endpoint) {
        return "Error while log-out via " + endpoint;
    }
}
