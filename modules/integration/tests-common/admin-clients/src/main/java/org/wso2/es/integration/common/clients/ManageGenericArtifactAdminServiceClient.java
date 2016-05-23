/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.es.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.generic.stub.ManageGenericArtifactServiceRegistryExceptionException;
import org.wso2.carbon.governance.generic.stub.ManageGenericArtifactServiceStub;

import java.rmi.RemoteException;

public class ManageGenericArtifactAdminServiceClient {
    private final String serviceName = "ManageGenericArtifactService";
    private ManageGenericArtifactServiceStub manageGenericArtifactServiceStub;
    private String endPoint;
    private static final Log log = LogFactory.getLog(ProfilesAdminServiceClient.class);

    public ManageGenericArtifactAdminServiceClient(String backEndUrl, String sessionCookie)
            throws RemoteException {
        this.endPoint = backEndUrl + serviceName;
        try {
            manageGenericArtifactServiceStub = new ManageGenericArtifactServiceStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Error on initializing ManageGenericArtifactService : " + axisFault.getMessage());
            throw new RemoteException("Error on initializing ManageGenericArtifactService : ", axisFault);
        }
        AuthenticateStub.authenticateStub(sessionCookie, manageGenericArtifactServiceStub);
    }

    public boolean addRXTResource(String rxtConfig, String path)
            throws RemoteException, ManageGenericArtifactServiceRegistryExceptionException {
       return manageGenericArtifactServiceStub.addRXTResource(rxtConfig, "/trunk/restservices/@{overview_version}/@{overview_name}");
    }



}
