package org.wso2.store.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.code.commons.cli.annotations.CliOption;
import com.google.code.commons.cli.annotations.CliParser;
import com.google.code.commons.cli.annotations.ParserException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class TestClient {

	static CloseableHttpClient httpclient = null;
	static HttpPost httppost = null;
	static HttpGet httpGet = null;
	static HttpEntity reqEntity = null;
	static Gson gson = new Gson();

	static CloseableHttpResponse response = null;
	protected static final Logger log = Logger.getLogger(ArtifactPublisher.class);

	private static SSLContextBuilder builder = null;
	private static SSLConnectionSocketFactory sslsf = null;
	private static HttpContext httpContext = null;
	
	@CliOption(opt="test")
	private static String test;
	
	
	public static void main(String args[]) throws ParseException, ParserException, FileNotFoundException {

/*		String hostName = "localhost";
		String port = "9443" ;

		String userName = "admin" ;
		String pwd = "admin" ;

	//	File samplesDirectory = new File(args[4]) ;

         System.out.println(args[0]);
		try {
			builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			sslsf = new SSLConnectionSocketFactory(builder.build());

			httpContext = new BasicHttpContext();

			String sessionId = getSession(sslsf, httpContext, hostName, port, userName, pwd);
			testAPI(sessionId);
		//	uploadArtificats(sslsf, httpContext, sessionId, samplesDirectory,hostName, port, userName, pwd);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	    final CommandLineParser parser = new BasicParser();
	    final Options options = new Options();
	   

	    Option option = new Option("a", "c desc");
	    // Set option c to take maximum of 10 arguments
	    option.setArgs(Option.UNLIMITED_VALUES);
	    options.addOption(option);
	    

		CliParser parser = new CliParser(new BasicParser());
        CliOptions cliOptions = parser.parse(CliOptions.class, args);


	    System.out.println(cliOptions.getHostName());*/
	  //  System.out.println(Arrays.toString(commandLine.getArgs()));
		
		BufferedReader br = new BufferedReader(new FileReader(new File("/home/manoj/wso2es-1.2.0-SNAPSHOT/samples/sample1/gadgets.json")));  
/*		Asset[] asset = gson.fromJson(br, Asset[].class); 
		System.out.println(asset);*/
	    JsonParser parser = new JsonParser();
	    JsonArray Jarray = (JsonArray) parser.parse(br).getAsJsonObject().get("assets");
	   
	    Asset[] asset = gson.fromJson( Jarray, Asset[].class);
	    System.out.println(asset[0]);
	}

	private static void testAPI(String sessionId) throws Exception{

		
		String apiUrl = "https://localhost:9443/publisher/apis/rxt/ebook/file";
		httpGet = new HttpGet(apiUrl);

		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		response = null;

		try {

			response = httpclient.execute(httpGet, httpContext);

		} catch (ClientProtocolException clientProtocolException) {

			clientProtocolException.printStackTrace();
			throw new Exception(clientProtocolException.getMessage(),
					clientProtocolException);

		} catch (IOException ioException) {

			throw new Exception(ioException.getMessage(), ioException);

		}

		try {
			String responseJson = EntityUtils.toString(response.getEntity());
			System.out.println(responseJson);
			String[] arr =  gson.fromJson(responseJson, String[].class);
			
			System.out.println(arr[0]);
			
/*			Authorize authorize = gson.fromJson(responseJson, Authorize.class);

			if (authorize.getData() != null) {

				sessionid = authorize.getData().getSessionId();
				log.info("Logged:"+ sessionid);
			}else{
				log.info("login failure!!!"+responseJson);
			}*/

		} catch (Exception ioException) {
			// TODO Auto-generated catch block
			ioException.printStackTrace();
			throw new Exception(ioException.getMessage(), ioException);

		} finally {
			httpclient.close();
		}
	}

	private static String getSession(SSLConnectionSocketFactory sslsf, HttpContext httpContext, String hostName, String port, String userName, String pwd) throws Exception {

		gson = new Gson();
		
		String authUrl = "https://" + hostName + ":" + port + "/"+"publisher"+IConstants.PUBLISHER_AUTHORIZATION_URL + "?username=" + userName + "&password=" + pwd;
		
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

			throw new Exception(ioException.getMessage(), ioException);

		}

		try {
			String responseJson = EntityUtils.toString(response.getEntity());
			System.out.println(responseJson);
			Authorize authorize = gson.fromJson(responseJson, Authorize.class);

			if (authorize.getData() != null) {

				sessionid = authorize.getData().getSessionId();
				log.info("Logged:"+ sessionid);
			}else{
				log.info("login failure!!!"+responseJson);
			}

		} catch (IOException ioException) {
			// TODO Auto-generated catch block
			throw new Exception(ioException.getMessage(), ioException);

		} finally {
			httpclient.close();
		}

		return sessionid;
	}

}
