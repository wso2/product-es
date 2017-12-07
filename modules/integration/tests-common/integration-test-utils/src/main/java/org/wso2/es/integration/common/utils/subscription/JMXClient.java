/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.es.integration.common.utils.subscription;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.NetworkUtils;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

public class JMXClient implements NotificationListener {

    private MBeanServerConnection mbsc = null;
    private static ObjectName nodeAgent;
    private boolean success = false;

    private static final String RMIRegistryPort = "10899";
    private static final String RMIServerPort = "12011";
    private static final String CONNECTION_NAME =
            "org.wso2.carbon:Type=Registry,ConnectorName=Events";
    private String path = "";
    private JMXConnector jmxc;

    private static final Log log = LogFactory.getLog(JMXClient.class);

    /**
     * connect to org.wso2.carbon for JMX monitoring
     *
     * @param userName user name to connect to org.wso2.carbon
     * @param password password to connect to org.wso2.carbon
     * @throws Exception
     */
    public void connect(String userName, String password) throws Exception {
        try {
            JMXServiceURL url =
                    new JMXServiceURL("service:jmx:rmi://" +
                                      NetworkUtils.getLocalHostname() + ":" +
                                      RMIServerPort + "/jndi/rmi://" +
                                      NetworkUtils.getLocalHostname() + ":" +
                                      RMIRegistryPort + "/jmxrmi");
            Hashtable<String, String[]> hashT = new Hashtable<>();
            String[] credentials = new String[]{userName, password};
            hashT.put("jmx.remote.credentials", credentials);
            jmxc = JMXConnectorFactory.connect(url, hashT);
            mbsc = jmxc.getMBeanServerConnection();
            nodeAgent = new ObjectName(CONNECTION_NAME);
        } catch (Exception ex) {
            log.error("infoAdminServiceStub Initialization fail ");
            throw new Exception("infoAdminServiceStub Initialization fail " + ex.getMessage());
        }
    }

    /**
     * connect to org.wso2.carbon to invoke different operations
     *
     * @param userName       user name to connect to org.wso2.carbon
     * @param password       password to connect to org.wso2.carbon
     * @param connectionType
     * @param operation
     * @throws Exception
     */
    public void connect(String userName, String password, String connectionType, String operation)
            throws Exception {
        try {
            JMXServiceURL url =
                    new JMXServiceURL("service:jmx:rmi://" +
                                      NetworkUtils.getLocalHostname() + ":" +
                                      RMIServerPort + "/jndi/rmi://" +
                                      NetworkUtils.getLocalHostname() + ":" +
                                      RMIRegistryPort + "/jmxrmi");
            Hashtable<String, String[]> hashT = new Hashtable<>();
            String[] credentials = new String[]{userName, password};
            hashT.put("jmx.remote.credentials", credentials);
            jmxc = JMXConnectorFactory.connect(url, hashT);
            mbsc = jmxc.getMBeanServerConnection();
            nodeAgent = new ObjectName(connectionType);
            mbsc.invoke(nodeAgent, operation, null, null);
        } catch (Exception ex) {
            log.error("infoAdminServiceStub Initialization fail ");
            throw new Exception("infoAdminServiceStub Initialization fail " + ex.getMessage());
        }
    }

    /**
     * register to get JMX notifications
     *
     * @param pathName path of the resource or collection which the notification is
     *                 about
     * @throws Exception
     */
    public void registerNotificationListener(String pathName) throws Exception {
        path = pathName;
        try {
            mbsc.addNotificationListener(nodeAgent, this, null, null);
            log.info("Registered for event notifications");
        } catch (Exception e) {
            log.error("NotificationListener registration fail");
            throw new Exception("NotificationListener registration fail" + e.getMessage());
        }
    }

    public void removeNotificationListener()
            throws ListenerNotFoundException, InstanceNotFoundException, IOException {
        mbsc.removeNotificationListener(nodeAgent, this, null, null);
    }


    public void handleNotification(Notification ntfyObj, Object handback) {
        log.info("***************************************************");
        log.info("* Notification received at " + new Date().toString());
        log.info("* type      = " + ntfyObj.getType());
        log.info("* message   = " + ntfyObj.getMessage());

        if (ntfyObj.getMessage().contains(path)) {
            setSuccess(true);
        }

        log.info("* seqNum    = " + ntfyObj.getSequenceNumber());
        log.info("* source    = " + ntfyObj.getSource());
        log.info("* seqNum    = " + Long.toString(ntfyObj.getSequenceNumber()));
        log.info("* timeStamp = " + new Date(ntfyObj.getTimeStamp()));
        log.info("* userData  = " + ntfyObj.getUserData());
        log.info("***************************************************");
    }

    /**
     * Listen to all the jmx notifications till the required notification is
     * captured
     *
     * @throws InterruptedException
     */
    public boolean getNotifications() throws InterruptedException {
        Calendar startTime = Calendar.getInstance();
        try {
            while (!isSuccess()) {
                if (((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < 60000) {
                    Thread.sleep(1000);
                } else {
                    break;
                }
            }
            return isSuccess();
        } catch (InterruptedException e) {
            log.error("JMX notification listner interrupted");
            throw new InterruptedException("JMX notification listner Ninterrupted" + e.getMessage());
        }
    }

    /**
     * @return true if the required notification in captured, otherwise false
     */
    public boolean isSuccess() {
        return success;
    }

    private void setSuccess(boolean success) {
        this.success = success;
    }

    public void disconnect()
            throws ListenerNotFoundException, InstanceNotFoundException, IOException {
//        mbsc.removeNotificationListener(nodeAgent, this, null, null);

        nodeAgent = null;
        if (jmxc != null) {
            log.info("Closing jmx client connection ##################################################");
            jmxc.close();

        }
        if (mbsc != null) {
            mbsc = null;
        }
    }


}
