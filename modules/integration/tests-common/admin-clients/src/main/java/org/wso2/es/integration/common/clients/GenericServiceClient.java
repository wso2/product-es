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
package org.wso2.es.integration.common.clients;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.generic.stub.ManageGenericArtifactServiceRegistryExceptionException;
import org.wso2.carbon.governance.generic.stub.ManageGenericArtifactServiceStub;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;

public class GenericServiceClient {
    private static final Log log = LogFactory.getLog(GenericServiceClient.class);

    private final String serviceName = "ManageGenericArtifactService";
    private ManageGenericArtifactServiceStub manageGenericArtifactServiceStub;
    private String endPoint;

    public GenericServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        manageGenericArtifactServiceStub = new ManageGenericArtifactServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, manageGenericArtifactServiceStub);
    }

    public GenericServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        manageGenericArtifactServiceStub = new ManageGenericArtifactServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, manageGenericArtifactServiceStub);

    }

    public String addArtifact(OMElement artifact, String key, String lifecycleAttribute)
            throws IOException, XMLStreamException,
            ManageGenericArtifactServiceRegistryExceptionException {
        return manageGenericArtifactServiceStub.addArtifact(key, artifact.toString(),
                lifecycleAttribute);
    }

    public String editArtifact(OMElement artifact, String key, String lifecycleAttribute, String path)
            throws IOException, XMLStreamException,
            ManageGenericArtifactServiceRegistryExceptionException {
        return manageGenericArtifactServiceStub.editArtifact(path, key, artifact.toString(), lifecycleAttribute);
    }


    /**
     * This will take the ui xml configuration for artifacts as a string input and will save it.
     *
     * @param artifactContent - artifact configuration
     * @return - artifact saved or not
     * @throws Exception - artifact saving error
     */
    public boolean saveConfiguration(String artifactContent, String path) throws RemoteException,ManageGenericArtifactServiceRegistryExceptionException {
        //changed the throwing exceptions to specfic exceptions rather than generic Exception.
        try {
            return manageGenericArtifactServiceStub.addRXTResource(artifactContent, path);
        } catch (RemoteException e) {
            log.info("Error on saving artifact configuration");
            throw new RemoteException("Error on saving artifact configuration");
        } catch (ManageGenericArtifactServiceRegistryExceptionException e) {
            log.info("Error on saving artifact configuration");
            throw new ManageGenericArtifactServiceRegistryExceptionException("Error on saving artifact configuration");
        }
    }

    /**
     * This will return ui xml configuration for artifacts as a string.
     *
     * @return artifact configuration
     * @throws Exception
     */
    public String getConfiguration(String key) throws Exception {
        try {
            return manageGenericArtifactServiceStub.getArtifactUIConfiguration(key);
        } catch (RemoteException e) {
            log.info("Error on getting artifact configuration");
            throw new Exception("Error on getting artifact configuration");
        } catch (ManageGenericArtifactServiceRegistryExceptionException e) {
            log.info("Error on getting artifact configuration");
            throw new Exception("Error on getting artifact configuration");
        }
    }
}