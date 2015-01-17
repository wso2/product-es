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

import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class AssetsRESTClient extends ESIntegrationTest {

    private static final String PUBLISHER_APIS_AUTHENTICATE_ENDPOINT = "/publisher/apis/authenticate?";
    private static final String PUBLISHER_APIS_LIST_GADGETS_ENDPOINT = "/publisher/apis/assets?type=gadget&count=12";
    private static final String PUBLISHER_APIS_LOGOUT_ENDPOINT = "/publisher/apis/logout";

    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String COOKIE = "Cookie";

    private static final String JSESSIONID = "JSESSIONID";

    private static final String UTF_8 = "UTF-8";
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
    private String login() throws IOException {
        String sessionID = null;
        Reader input = null;
        BufferedWriter writer = null;
        String authenticationEndpoint = getBaseUrl() + PUBLISHER_APIS_AUTHENTICATE_ENDPOINT;
        //construct full authenticate endpoint
        try {
            //authenticate endpoint URL
            URL endpointUrl = new URL(authenticationEndpoint);
            URLConnection urlConn = endpointUrl.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            // Specify the content type.
            urlConn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE);
            // Send POST output.
            writer = new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream()));
            String content = USERNAME + "=" + URLEncoder.encode(USERNAME_VAL, UTF_8) + "&" + PASSWORD + "=" +
                    URLEncoder.encode(PASSWORD_VAl, UTF_8);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Send Login Information : " + content);
            }
            writer.write(content);
            writer.flush();

            // Get response data.
            input = new InputStreamReader(urlConn.getInputStream());
            JsonElement elem = parser.parse(input);
            sessionID = elem.getAsJsonObject().getAsJsonObject(DATA).get(SESSIONID).toString();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received SessionID : " + sessionID);
            }

        } catch (MalformedURLException e) {
            LOG.error(getLoginErrorMassage(authenticationEndpoint), e);
            throw e;
        } catch (IOException e) {
            LOG.error(getLoginErrorMassage(authenticationEndpoint), e);
            throw e;
        } finally {
            if (input != null) {
                try {
                    input.close();// will close the URL connection as well
                } catch (IOException e) {
                    LOG.error("Failed to close input stream ", e);
                }
            }
            if (writer != null) {
                try {
                    writer.close();// will close the URL connection as well
                } catch (IOException e) {
                    LOG.error("Failed to close output stream ", e);
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
    private JsonArray getAssets(String sessionId) throws IOException {
        BufferedReader input = null;
        String listAssetsEndpoint = getBaseUrl() + PUBLISHER_APIS_LIST_GADGETS_ENDPOINT;
        //construct endpoint which retrieves list of gadgets
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get Assets via REST endpoint using sessionID: " + sessionId);
            }
            URL endpointUrl = new URL(listAssetsEndpoint);
            URLConnection urlConn = endpointUrl.openConnection();
            urlConn.setRequestProperty(COOKIE, JSESSIONID + "=" + sessionId + ";");
            // SessionId Cookie
            urlConn.connect();
            //GET response data
            input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            JsonElement elem = parser.parse(input);

            // parse response to a JasonArray
            return elem.getAsJsonObject().getAsJsonArray(DATA);
        } catch (MalformedURLException e) {
            LOG.error(getAssetRetrievingErrorMassage(listAssetsEndpoint), e);
            throw e;
        } catch (IOException e) {
            LOG.error(getAssetRetrievingErrorMassage(listAssetsEndpoint), e);
            throw e;
        } finally {
            if (input != null) {
                try {
                    input.close();// will close the URL connection as well
                } catch (IOException e) {
                    LOG.error("Failed to close the connection", e);
                }
            }
        }
    }

    /**
     * This method sends a request to publisher logout api to invalidate the given sessionID
     *
     * @param sessionId String of valid session ID
     */
    private void logOut(String sessionId) throws IOException {
        URLConnection urlConn = null;
        String logoutEndpoint = getBaseUrl() + PUBLISHER_APIS_LOGOUT_ENDPOINT;
        //construct APIs session invalidate endpoint
        try {
            URL endpointUrl = new URL(logoutEndpoint);
            urlConn = endpointUrl.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Invalidating session: " + sessionId);
            }
            urlConn.setRequestProperty(COOKIE, JSESSIONID + "=" + sessionId + ";");
            //send SessionId Cookie
            //send POST output.
            urlConn.getOutputStream().flush();
        } catch (MalformedURLException e) {
            LOG.error(getLogoutErrorMassage(logoutEndpoint), e);
            throw e;
        } catch (IOException e) {
            LOG.error(getLogoutErrorMassage(logoutEndpoint), e);
            throw e;
        } finally {
            if (urlConn != null) {
                try {
                    urlConn.getOutputStream().close();//will close the connection as well
                } catch (IOException e) {
                    LOG.error("Failed to close OutPutStream", e);
                }
            }
        }
    }

    /**
     * Public method to check whether the registry indexing is completed
     * this checks if ES publisher assets are completely indexed by the registry
     *
     * @return true if completed false otherwise
     */
    public boolean isIndexCompleted() throws IOException {
        boolean completed = false;
        String sessionId = login();

        if (sessionId != null) {
            JsonArray assets = getAssets(sessionId);
            if (assets != null && assets.size() == DEFAULT_PAGE_SIZE) {
                LOG.info("Completed Indexing");
                completed = true;
            } else {
                LOG.info("Indexing is not completed yet");
                completed = false;
            }
            logOut(sessionId);
        }
        return completed;
    }

    /**
     * This method obtains base URl (ex: https://localhost:9443) required for the full endpoint creation
     *
     * @return String as a context URL
     */
    private String getBaseUrl() {
        if (esContext == null) {
            throw new IllegalStateException("init() has not been successfully called");
        }
        try {
            return esContext.getContextUrls().getWebAppURL();
        } catch (XPathExpressionException e) {
            LOG.error("Couldn't obtain WebAppUrl", e);
            throw new RuntimeException("Error while obtaining WebAppUrl ", e);
        }
    }

    /**
     * Error massage when asset retrieval fails
     *
     * @return Error massage as a String
     */
    private String getAssetRetrievingErrorMassage(String listAssetsEndpoint) {
        return "Error while retrieving assets via  " + listAssetsEndpoint;
    }

    /**
     * Error massage when login fails
     *
     * @return Error massage as a String
     */
    private String getLoginErrorMassage(String authenticationEndpoint) {
        return "Error while authenticating to publisher apis via " + authenticationEndpoint;
    }

    /**
     * Error massage when logout fails
     *
     * @return Error massage as a String
     */
    private String getLogoutErrorMassage(String logoutEndpoint) {
        return "Error while log-out via " + logoutEndpoint;
    }
}
