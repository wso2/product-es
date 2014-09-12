package org.wso2.store.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import com.google.gson.Gson;

public class ArtifactPublisher {

	static CloseableHttpClient httpclient = null;
	static HttpPost httppost = null;
	static HttpEntity reqEntity = null;
	static Gson gson = null;

	static CloseableHttpResponse response = null;
	protected static final Logger log = Logger.getLogger(ArtifactPublisher.class);

	
	
	public static void main(String args[]) {

		String hostName = args[0].equals("") ? "localhost":args[0];
		String port = args[1].equals("") ? "9443" :args[1] ;

		String userName = args[2].equals("") ? "admin" :args[2] ;
		String pwd = args[3].equals("")? "admin" :args[3] ;

		File samplesDirectory = new File(args[4]) ;


		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());

			HttpContext httpContext = new BasicHttpContext();

			String sessionId = getSession(sslsf, httpContext, hostName, port, userName, pwd);
			readAssets(samplesDirectory);
		//	uploadArtificats(sslsf, httpContext, sessionId, samplesDirectory,hostName, port, userName, pwd);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static String getSession(SSLConnectionSocketFactory sslsf, HttpContext httpContext, String hostName, String port, String userName, String pwd) throws Exception {

		gson = new Gson();
		
		String authUrl = "https://" + hostName + ":" + port + IConstants.PUBLISHER_AUTHORIZATION_URL + "?username=" + userName + "&password=" + pwd;
		
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

	private static void uploadArtificats(SSLConnectionSocketFactory sslsf, HttpContext httpContext, String sessionId, File sampleDir, String hostName, String port, String userName, String pwd) throws IOException {

		//getAssets(sampleDir);
   
/*		StringBuffer publisherUrlBuff = null;
		File imageFile = null;
	
		MultipartEntityBuilder multiPartBuilder = null;
		JsonObject jsonObject = null;
		JsonParser parser = null;
		JsonElement jsonElement = null;

		JsonArray jarray = null;
		String assetType = null;
		String assetId = null;
		
		for (final File file : directory.listFiles()) {

			
			if (file.isFile() && (file.getName().substring(file.getName().lastIndexOf(".")+1).equals("json"))) {

				
				try {

					jsonObject = new JsonObject();

					parser = new JsonParser();
					jsonElement = parser.parse(new FileReader(file));
					jsonObject = jsonElement.getAsJsonObject();

					jarray = jsonObject.getAsJsonArray("assets");
					
					for (JsonElement newjsElement : jarray) {
						
						JsonObject newjsonObject = newjsElement.getAsJsonObject();	
						assetType = newjsonObject.getAsJsonObject("asset").get("type").getAsString();
						
						log.info("assetType:"+ assetType);
						
						JsonElement assetidelement = newjsonObject.getAsJsonObject("asset").get("id");
						publisherUrlBuff = new StringBuffer();
						
						if (assetidelement != null){
							
							publisherUrlBuff.append("https://" + hostName + ":" + port 	+ IConstants.PUBLISHER_URL +"/"+ assetidelement.getAsString() + "?type="+assetType);
						}else{
							
							publisherUrlBuff.append("https://" + hostName + ":" + port 	+ IConstants.PUBLISHER_URL + "?type="+assetType);
						}
						
						
						
						httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
						httppost = new HttpPost(publisherUrlBuff.toString());
						
						
						
						multiPartBuilder = MultipartEntityBuilder.create();
						multiPartBuilder.addTextBody("sessionId", sessionId);
						multiPartBuilder.addTextBody("asset", newjsonObject.getAsJsonObject("asset").toString());
						
	
						

						JsonElement newjsonElement =  newjsonObject.getAsJsonObject("asset").getAsJsonObject("attributes").get("images_thumbnail");
						imageFile = new File(imageDirectory + File.separator+ newjsonElement.getAsString());
						multiPartBuilder.addBinaryBody("images_thumbnail", imageFile);
						
						newjsonElement =  newjsonObject.getAsJsonObject("asset").getAsJsonObject("attributes").get("images_banner");
						imageFile = new File(imageDirectory + File.separator+ newjsonElement.getAsString());
						multiPartBuilder.addBinaryBody("images_banner", imageFile);
						
	    				httppost.setEntity(multiPartBuilder.build());
	    				HttpResponse response =  httpclient.execute(httppost,httpContext);
	    				String responseJson = EntityUtils.toString(response.getEntity());
	    				
	    				
	    				log.info(responseJson.toString());
	    				
	    				httpclient.close();
						
					}


				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					
					log.error("File Not found:",e);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("IO error", e);
					
				}finally{
					
					multiPartBuilder = null;
					
					try {
						httpclient.close();
					} catch (IOException e) {
						
					}
					
					httppost = null;
					httpclient = null;
				}
			}

		}*/
	}

	
	private static void readAssets(File dir) throws Exception {
		
		Asset[] assetArr = null;
		BufferedReader br  = null;
		String resourcePath = "";
		
		for(final File file:dir.listFiles()){
		
				if (file.isFile() && file.getName().substring(file.getName().lastIndexOf(".")+1).equals("json")){
					br = new BufferedReader(new FileReader(file));  
					assetArr = gson.fromJson(br, Asset[].class); 
					resourcePath = file.getParent();
				}
				
				if (file.list() !=null && file.list().length>0){
					readAssets(file);
				}
		}
		
	}
	
	
	
}
