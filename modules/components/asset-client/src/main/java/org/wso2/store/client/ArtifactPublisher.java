package org.wso2.store.client;

import com.google.code.commons.cli.annotations.CliParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.commons.cli.BasicParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ArtifactPublisher {

    protected static final Logger log = Logger.getLogger(ArtifactPublisher.class);
    static CloseableHttpClient httpClient = null;
    static HttpPost httppost = null;
    static HttpGet httpGet = null;
    static HttpEntity reqEntity = null;
    static Gson gson = null;
    static CloseableHttpResponse response = null;
    static SSLConnectionSocketFactory sslConnectionSocketFactory = null;
    static HttpContext httpContext = null;
    static HashMap<String, List<String>> rxtFileAttributesMap = null;

    static String hostName = "";
    static String port = "";

    static String userName = "";
    static String pwd = "";
    static String sessionId = "";
    static String context = "";
    static String location;

    public static void main(String args[]) throws Exception {


        CliParser parser = new CliParser(new BasicParser());


        CliOptions cliOptions = parser.parse(CliOptions.class, args);

        hostName = cliOptions.getHostName();
        port = cliOptions.getPort();
        userName = cliOptions.getUserName();
        pwd = cliOptions.getPwd();
        context = cliOptions.getContext();
        location = cliOptions.getLocation();

        File samplesDirectory = new File(location);


        try {

            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build());

            httpContext = new BasicHttpContext();

            sessionId = getSession(sslConnectionSocketFactory, httpContext, hostName, port, userName, pwd);

            String[] rxtArr = getRxtTypes();

            rxtFileAttributesMap = new HashMap<String, List<String>>();

            for (String rxtType : rxtArr) {
                rxtFileAttributesMap.put(rxtType, getAttributesForType(rxtType, "file"));
            }

            readAssets(samplesDirectory);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private static String getSession(SSLConnectionSocketFactory sslsf, HttpContext httpContext, String hostName, String port, String userName, String pwd) throws Exception {

        gson = new Gson();

        String authUrl = "https://" + hostName + ":" + port + "/" + context + IConstants.PUBLISHER_AUTHORIZATION_URL + "?username=" + userName + "&password=" + pwd;

        httppost = new HttpPost(authUrl);

        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        String sessionid = "";

        response = null;


        try {

            response = httpclient.execute(httppost, httpContext);

        } catch (ClientProtocolException clientProtocolException) {

            clientProtocolException.printStackTrace();
            throw new Exception(clientProtocolException.getMessage(),
                    clientProtocolException);

        } catch (IOException ioException) {
            ioException.printStackTrace();
            throw new Exception(ioException.getMessage(), ioException);

        }

        try {
            String responseJson = EntityUtils.toString(response.getEntity());
            Authorize authorize = gson.fromJson(responseJson, Authorize.class);

            if (authorize.getData() != null) {

                sessionid = authorize.getData().getSessionId();
                log.info("Logged:" + sessionid);
            } else {
                log.info("login failure!!!" + responseJson);
            }

        } catch (IOException ioException) {
            // TODO Auto-generated catch block
            ioException.printStackTrace();
            throw new Exception(ioException.getMessage(), ioException);

        } finally {
            httpclient.close();
        }

        return sessionid;
    }


    private static String[] getRxtTypes() {


        String apiUrl = "https://" + hostName + ":" + port + "/" + context + IConstants.RXT_URL;
        httpGet = new HttpGet(apiUrl);

        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
        response = null;


        try {

            response = httpclient.execute(httpGet, httpContext);

        } catch (ClientProtocolException clientProtocolException) {

            clientProtocolException.printStackTrace();

        } catch (IOException ioException) {

        }

        String responseJson = null;
        String[] arrRxt = null;
        try {
            responseJson = EntityUtils.toString(response.getEntity());
            arrRxt = gson.fromJson(responseJson, String[].class);

        } catch (Exception ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrRxt;


    }

    private static List<String> getAttributesForType(String rxtType, String type) {

        String apiUrl = "https://" + hostName + ":" + port + "/" + context + IConstants.RXT_ATTRIBUTES_FOR_GIVEN_TYPE + "/" + rxtType + "/" + type;
        httpGet = new HttpGet(apiUrl);

        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();

        response = null;
        gson = new Gson();

        try {

            response = httpclient.execute(httpGet, httpContext);

        } catch (ClientProtocolException clientProtocolException) {

            clientProtocolException.printStackTrace();


        } catch (IOException ioException) {

        }

        String responseJson = null;
        String[] attrArr = null;

        try {

            responseJson = EntityUtils.toString(response.getEntity());
            attrArr = gson.fromJson(responseJson, String[].class);

        } catch (Exception ioException) {
            // TODO Auto-generated catch block
            ioException.printStackTrace();
            //throw new Exception(ioException.getMessage(), ioException);

        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        return Arrays.asList(attrArr);

    }


    private static void readAssets(File dir) throws Exception {

        Asset[] assetArr = null;
        BufferedReader br = null;


        for (final File file : dir.listFiles()) {

            if (file.isFile() && file.getName().substring(file.getName().lastIndexOf(".") + 1).equals("json")) {
                br = new BufferedReader(new FileReader(file));
                JsonParser parser = new JsonParser();
                JsonArray Jarray = (JsonArray) parser.parse(br).getAsJsonObject().get("assets");

                assetArr = gson.fromJson(Jarray, Asset[].class);
                uploadAssets(assetArr, dir);
            }

            if (file.list() != null && file.list().length > 0) {
                readAssets(file);
            }
        }

    }

    private static void uploadAssets(Asset[] assetArr, File dir) {

        HashMap<String, String> attrMap = null;
        MultipartEntityBuilder multiPartBuilder = null;
        List<String> fileAttributes = null;

        File imageFile = null;
        String responseJson = null;

        for (Asset asset : assetArr) {

            StringBuffer publisherUrlBuff = new StringBuffer();

            if (asset.getId() != null) {

                publisherUrlBuff.append("https://" + hostName + ":" + port + "/" + context + IConstants.PUBLISHER_URL + "/" + asset.getId() + "?type=" + asset.getType());
            } else {

                publisherUrlBuff.append("https://" + hostName + ":" + port + "/" + context + IConstants.PUBLISHER_URL + "?type=" + asset.getType());
            }


            multiPartBuilder = MultipartEntityBuilder.create();
            multiPartBuilder.addTextBody("sessionId", sessionId);
            multiPartBuilder.addTextBody("asset", gson.toJson(asset));


            attrMap = asset.getAttributes();

            for (String attrKey : attrMap.keySet()) {

                httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
                httppost = new HttpPost(publisherUrlBuff.toString());

                fileAttributes = rxtFileAttributesMap.get(asset.getType());
                if (fileAttributes != null && fileAttributes.contains(attrKey)) {
                    imageFile = new File(dir + File.separator + IConstants.RESOURCE_DIR_NAME + File.separator + attrMap.get(attrKey));
                    multiPartBuilder.addBinaryBody(attrKey, imageFile);

                    httppost.setEntity(multiPartBuilder.build());

                }
            }


            try {
                response = httpClient.execute(httppost, httpContext);
                responseJson = EntityUtils.toString(response.getEntity());
                System.out.println(responseJson.toString());
                log.info(responseJson.toString());
            } catch (Exception ex) {
                log.error("error in asssetUpload", ex);
            } finally {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }


}
