package org.wso2.es.integration.common.clients;

/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.reporting.stub.ReportingResourcesSupplierReportingExceptionException;
import org.wso2.carbon.reporting.stub.ReportingResourcesSupplierStub;

import java.rmi.RemoteException;

public class ReportResourceSupplierClient {

    private final String serviceName = "ReportingResourcesSupplier";
    private ReportingResourcesSupplierStub reportingResourcesSupplierStub;
    private String endPoint;
    private static final Log log = LogFactory.getLog(ReportResourceSupplierClient.class);

    public ReportResourceSupplierClient(String backEndUrl, String sessionCookie)
            throws RemoteException {
        this.endPoint = backEndUrl + serviceName;
        try {
            reportingResourcesSupplierStub = new ReportingResourcesSupplierStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Error on initializing reportingResourcesSupplierStub : " + axisFault.getMessage());
            throw new RemoteException("Error on initializing reportingResourcesSupplierStub : ", axisFault);
        }
        AuthenticateStub.authenticateStub(sessionCookie, reportingResourcesSupplierStub);
    }

    public ReportResourceSupplierClient(String backEndUrl, String userName, String password)
            throws RemoteException {
        this.endPoint = backEndUrl + serviceName;
        try {
            reportingResourcesSupplierStub = new ReportingResourcesSupplierStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Error on initializing reportingResourcesSupplierStub : " + axisFault.getMessage());
            throw new RemoteException("Error on initializing reportingResourcesSupplierStub : ", axisFault);
        }
        AuthenticateStub.authenticateStub(userName, password, reportingResourcesSupplierStub);
    }

    public String getReportResource(String componentName, String reportTemplate)
            throws RemoteException {
        try {
            return reportingResourcesSupplierStub.getReportResources(componentName, reportTemplate);
        } catch (ReportingResourcesSupplierReportingExceptionException e) {
            log.error("Cannot get report resource" + e.getMessage());
            throw new RemoteException("Cannot get report resource", e);
        }
    }
}
