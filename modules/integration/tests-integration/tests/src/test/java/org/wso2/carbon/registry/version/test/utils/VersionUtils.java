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

package org.wso2.carbon.registry.version.test.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;

import java.rmi.RemoteException;

public class VersionUtils {
    private static final Log log = LogFactory.getLog(VersionUtils.class);

    private VersionUtils() {
    }

    public static VersionPath[] deleteAllVersions(ResourceAdminServiceClient resourceAdminClient,
                                                  String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        VersionPath[] versionPaths;
        try {
            versionPaths = resourceAdminClient.getVersionPaths(path);

            if (versionPaths != null) {
                for (VersionPath versionPath : versionPaths) {
                    long versionNo = versionPath.getVersionNumber();
                    String snapshotId = String.valueOf(versionNo);
                    resourceAdminClient.deleteVersionHistory(path, snapshotId);

                }
                return resourceAdminClient.getVersionPaths(path);
            }
        } catch (RemoteException ignored) {
            log.info("Resource not found at " + path);
        }

        return null;
    }
}
