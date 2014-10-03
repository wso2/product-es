package org.wso2.stroe.bamclient;


import org.apache.log4j.Logger;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import javax.security.sasl.AuthenticationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;
import java.lang.String;



/**
 * Created by manoj on 9/30/14.
 */
public class EventPublisher {

    private static Logger logger = Logger.getLogger(EventPublisher.class);

    private static EventPublisher instance = null;
    private String trustStoreFilePath = null;
    private String trustStorePwd = null;
    private String bamHost = null;
    private String thriftPort = null;
    private String userName = null;
    private String password = null;

    private DataPublisher dataPublisher = null;


    private EventPublisher() throws Exception {

        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();

       try {
            InputStream inputStream = new FileInputStream(new File(System.getProperty("CARBON_HOME") + File.separator + "repository" + File.separator+"bam"+ File.separator +"bam.xml"));
            serverConfiguration.init(inputStream);

            trustStoreFilePath = serverConfiguration.getFirstProperty("clientStorePath");
            trustStorePwd = serverConfiguration.getFirstProperty("trustStorePassword");
            bamHost = serverConfiguration.getFirstProperty("bamhost");
            thriftPort = serverConfiguration.getFirstProperty("thriftPort");

            userName = serverConfiguration.getFirstProperty("userName");
            password = serverConfiguration.getFirstProperty("password");

            AgentConfiguration agentConfiguration = new AgentConfiguration();
            System.setProperty("javax.net.ssl.trustStore", trustStoreFilePath);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePwd);
            Agent agent = new Agent(agentConfiguration);

            String url = "tcp://" + bamHost + ":" + thriftPort;

            dataPublisher = new DataPublisher(url, userName, password, agent);

        }catch (FileNotFoundException fileNotFoundEx){
            logger.error("bam conf file not found", fileNotFoundEx);
            throw new Exception(fileNotFoundEx);

        }catch (ServerConfigurationException e) {
             logger.error("bam conf reading error", e);
             throw new Exception(e);
        }
    }

    public static EventPublisher getInstance() throws Exception {
        if (instance == null){
            instance = new EventPublisher();
        }
        return instance;
    }

    public void publishEvents(String streamName , String streamVersion, String streamDefinition, Object[] metaData,Object[] data) throws Exception {

        String streamId = null;
        logger.debug(streamDefinition);

        try {
            streamId = dataPublisher.findStreamId(streamName, streamVersion);

        } catch (AgentException e) {
            e.printStackTrace();
        }

        if (streamId == null){
            try{
                streamId = dataPublisher.defineStream(streamDefinition);

            } catch (AgentException e) {
                logger.error("Agent exception creation of stream", e);
                throw new Exception(e);

            } catch (MalformedStreamDefinitionException e) {
                logger.error("stream definition malformed:"+streamDefinition, e);
                throw new Exception(e);

            } catch (StreamDefinitionException e) {
                logger.error("stream definition error", e);
                throw new Exception(e);

            } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
                logger.error("different stream definition already defined", e);
                throw new Exception(e);
            }
        }

        logger.info("stream created:");

        Event event = new Event(streamId, System.currentTimeMillis(), metaData, new Object[]{"es"}, data);

        try {
            dataPublisher.publish(event);

        } catch (AgentException e) {
            e.printStackTrace();
        }


    }

}
