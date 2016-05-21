/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.es.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.es.integration.common.clients.UserProfileMgtServiceClient;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This util class contains all support methods to run email notification test cases.
 */
public class EmailUtil {

    private static final Log log = LogFactory.getLog(EmailUtil.class);
    private static String emailAddress = "gregtestes@gmail.com";
    private static char[] emailPassword = new char[] { 'g', 'r', 'e', 'g', '1', '2', '3', '4' };;
    private static HttpClient httpClient;
    private String pointBrowserURL;
    private static List<NameValuePair> urlParameters = new ArrayList<>();
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";

    /**
     * Initializes the httpClient.
     */
    public static void initialize() throws XPathExpressionException {

        DefaultHttpClient client = new DefaultHttpClient();

        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        Scheme sch = new Scheme("https", 443, socketFactory);
        ClientConnectionManager mgr = client.getConnectionManager();
        mgr.getSchemeRegistry().register(sch);
        httpClient = new DefaultHttpClient(mgr, client.getParams());

        // Set verifier
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    /**
     * Update user profile for particular user in order to enable e-mail subscription.
     *
     * @param automationContext
     * @param backendURL        URL of the server.
     * @param session           session cookie obtained after logging in.
     * @throws UserProfileMgtServiceClient
     * @throws IOException
     * @throws XPathExpressionException
     * @throws AutomationUtilException
     */
    public static void updateProfileAndEnableEmailConfiguration(AutomationContext automationContext, String backendURL,
            String session)
            throws UserProfileMgtServiceUserProfileExceptionException, IOException, XPathExpressionException,
            AutomationUtilException {
        UserProfileMgtServiceClient userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, session);
        File axis2File = new File(
                TestConfigurationProvider.getResourceLocation("GREG") + File.separator + "axis2" + File.separator
                        + "axis2.xml");
        UserProfileDTO profile = new UserProfileDTO();
        profile.setProfileName("default");

        UserFieldDTO lastName = new UserFieldDTO();
        lastName.setClaimUri("http://wso2.org/claims/lastname");
        lastName.setFieldValue("GregUserFirstName");

        UserFieldDTO givenName = new UserFieldDTO();
        givenName.setClaimUri("http://wso2.org/claims/givenname");
        givenName.setFieldValue("GregUserLastName");

        UserFieldDTO email = new UserFieldDTO();
        email.setClaimUri("http://wso2.org/claims/emailaddress");
        email.setFieldValue(emailAddress);

        UserFieldDTO[] fields = new UserFieldDTO[3];
        fields[0] = lastName;
        fields[1] = givenName;
        fields[2] = email;

        profile.setFieldValues(fields);

        userProfileMgtClient
                .setUserProfile(automationContext.getContextTenant().getContextUser().getUserName(), profile);

        // apply new axis2.xml configuration
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(automationContext);
        serverConfigurationManager.applyConfiguration(axis2File);
    }

    /**
     * This method read verification e-mail from Gmail inbox and returns the verification URL.
     *
     * @return  verification redirection URL.
     * @throws  Exception
     */
    public static String readGmailInboxForVerification() throws Exception {
        boolean isEmailVerified = false;
        long waitTime = 10000;
        String pointBrowserURL = "";
        Properties props = new Properties();
        props.load(new FileInputStream(new File(
                TestConfigurationProvider.getResourceLocation("GREG") + File.separator + "axis2" + File.separator
                        + "smtp.properties")));
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("smtp.gmail.com", emailAddress, java.nio.CharBuffer.wrap(emailPassword).toString());

        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        Thread.sleep(waitTime);
        long startTime = System.currentTimeMillis();
        long endTime = 0;
        int count = 1;
        while (endTime - startTime < 180000 && !isEmailVerified) {
            Message[] messages = inbox.getMessages();

            for (Message message : messages) {
                if (!message.isExpunged()) {
                    try {
                        log.info("Mail Subject:- " + message.getSubject());
                        if (message.getSubject().contains("EmailVerification")) {
                            pointBrowserURL = getBodyFromMessage(message);
                            isEmailVerified = true;
                        }

                        // Optional : deleting the mail
                        message.setFlag(Flags.Flag.DELETED, true);
                    } catch (MessageRemovedException e){
                        log.error("Could not read the message subject. Message is removed from inbox");
                    }
                }
            }
            endTime = System.currentTimeMillis();
            Thread.sleep(waitTime);
            endTime += count*waitTime;
            count++;
        }
        inbox.close(true);
        store.close();
        return pointBrowserURL;
    }

    /**
     * This method read e-mails from Gmail inbox and find whether the notification of particular type is found.
     *
     * @param   notificationType    Notification types supported by publisher and store.
     * @return  whether email is found for particular type.
     * @throws  Exception
     */
    public static boolean readGmailInboxForNotification(String notificationType) throws Exception {
        boolean isNotificationMailAvailable = false;
        long waitTime = 10000;
        Properties props = new Properties();
        props.load(new FileInputStream(new File(
                TestConfigurationProvider.getResourceLocation("GREG") + File.separator + "axis2" + File.separator
                        + "smtp.properties")));
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("smtp.gmail.com", emailAddress, java.nio.CharBuffer.wrap(emailPassword).toString());

        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        Thread.sleep(waitTime);

        long startTime = System.currentTimeMillis();
        long endTime = 0;
        int count = 1;
        while (endTime - startTime < 180000 && !isNotificationMailAvailable) {
            Message[] messages = inbox.getMessages();

            for (Message message : messages) {
                if(!message.isExpunged()) {
                    try {
                        log.info("Mail Subject:- " + message.getSubject());

                        if (message.getSubject().contains(notificationType)) {
                            isNotificationMailAvailable = true;

                        }
                        // Optional : deleting the  mail
                        message.setFlag(Flags.Flag.DELETED, true);
                    } catch (MessageRemovedException e) {
                        log.error("Could not read the message subject. Message is removed from inbox");
                    }
                }

            }
            endTime = System.currentTimeMillis();
            Thread.sleep(waitTime);
            endTime += count*waitTime;
            count++;
        }
        inbox.close(true);
        store.close();
        return isNotificationMailAvailable;
    }

    /**
     * This method delete all the sent mails. Can be used after a particular test class
     *
     * @throws  MessagingException
     * @throws  IOException
     */
    public static void deleteSentMails() throws MessagingException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(new File(
                TestConfigurationProvider.getResourceLocation("GREG") + File.separator + "axis2" + File.separator
                        + "smtp.properties")));
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("smtp.gmail.com", emailAddress, java.nio.CharBuffer.wrap(emailPassword).toString());

        Folder sentMail = store.getFolder("[Gmail]/Sent Mail");
        sentMail.open(Folder.READ_WRITE);
        Message[] messages =sentMail.getMessages();
        Flags deleted = new Flags(Flags.Flag.DELETED);
        sentMail.setFlags(messages,deleted,true);
        sentMail.close(true);
        store.close();
    }

    /**
     * This method is used to access the verification link provided by verification mail.
     * This method automates the browser redirection process in order to receive notifications
     *
     * @param   pointBrowserURL    redirection URL to management console.
     * @param   loginURL           login URL of the console.
     * @param   userName           user which is used to log in.
     * @param   password           password for the user.
     * @throws  Exception
     */
    public static void browserRedirectionOnVerification(String pointBrowserURL, String loginURL, String userName,
            String password) throws Exception {

        initialize();
        pointBrowserURL = replaceIP(pointBrowserURL);
        HttpResponse verificationUrlResponse = sendGetRequest(String.format(pointBrowserURL));

        EntityUtils.consume(verificationUrlResponse.getEntity());

        urlParameters.clear();
        urlParameters.add(new BasicNameValuePair("username", userName));
        urlParameters.add(new BasicNameValuePair("password", password));

        HttpResponse loginResponse = sendPOSTMessage(loginURL + "admin/login.jsp", urlParameters);
        EntityUtils.consume(loginResponse.getEntity());

        HttpResponse reDirectionResponse = sendPOSTMessage(loginURL + "admin/login_action.jsp", urlParameters);
        String redirectionUrl = locationHeader(reDirectionResponse);
        EntityUtils.consume(reDirectionResponse.getEntity());

        HttpResponse newReDirectionResponse = sendGetRequest(String.format(redirectionUrl));
        EntityUtils.consume(newReDirectionResponse.getEntity());

        HttpResponse verificationConfirmationResponse = sendGetRequest(
                String.format(loginURL + "email-verification/validator_ajaxprocessor.jsp?confirmation=" +
                        pointBrowserURL.split("confirmation=")[1].split("&")[0]));
        EntityUtils.consume(verificationConfirmationResponse.getEntity());

        String newRedirectionUrl = locationHeader(reDirectionResponse);

        HttpResponse confirmationSuccessResponse = sendGetRequest(String.format(newRedirectionUrl));
        EntityUtils.consume(confirmationSuccessResponse.getEntity());

        log.info("Your email has been confirmed successfully");

    }

    /**
     * This method is used to replace the IP address in the point browseURL with localhost.
     *
     * @param   pointBrowserURL    redirection URL to management console.
     * @return  URL replaced with localhost removing IP address.
     */
    private static String replaceIP(String pointBrowserURL) {
        String IPAddressPattern =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        pointBrowserURL = pointBrowserURL.replaceAll(IPAddressPattern, "localhost");
        return pointBrowserURL;
    }

    private static String locationHeader(HttpResponse response) {
        org.apache.http.Header[] headers = response.getAllHeaders();
        String url = null;
        for (org.apache.http.Header header : headers) {
            if ("Location".equals(header.getName())) {
                url = header.getValue();
                break;
            }
        }
        return url;
    }

    /**
     * This method is used to send a HTTP get request.
     *
     * @param   url    destination url
     * @return         response of the get request.
     */
    private static HttpResponse sendGetRequest(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return httpClient.execute(request);
    }

    /**
     * This method is used to send a HTTP post request.
     *
     * @param   url             destination url.
     * @param  urlParameters    list of parameters for post request (username , password etc)
     * @return                  response of the post request.
     */
    private static HttpResponse sendPOSTMessage(String url, List<NameValuePair> urlParameters) throws Exception {
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        post.addHeader("Referer", url);
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    /**
     * This method is used to extract verification URL from the e-mail message.
     *
     * @param   message    re-mail message.
     * @return  verification URL to be redirect to.
     */
    private static String getBodyFromMessage(Message message) throws IOException, MessagingException {
        if (message.isMimeType("text/plain")) {
            String[] arr = message.getContent().toString().split("\\r?\\n");
            for (int x = 0; x <= arr.length; x++) {
                if (arr[x].contains("https://")) {
                    return arr[x];
                }
            }

        }
        return "";
    }
}
