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
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.es.integration.common.clients.HumanTaskAdminClient;
import org.wso2.es.integration.common.clients.WorkItem;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

public class WorkItemClient {
    private static Log log = LogFactory.getLog(WorkItemClient.class);

    private WorkItemClient() {
    }

    /**
     * get the existing management console notifications
     *
     * @param humanTaskAdminClient
     * @return
     * @throws java.rmi.RemoteException
     * @throws IllegalStateFault
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws InterruptedException
     */
    public static WorkItem[] getWorkItems(HumanTaskAdminClient humanTaskAdminClient)
            throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault,
                   InterruptedException {
        long startTime = new Date().getTime();
        long endTime = startTime + 2 * 60 * 1000;
        WorkItem[] workItems = null;
        // try for a minute to get all the notifications
        while ((new Date().getTime()) < endTime) {
            workItems = humanTaskAdminClient.getWorkItems();
            if (workItems.length > 0) {
                break;
            }
            Thread.sleep(5000);
        }
        return workItems;
    }

    public static WorkItem[] waitForWorkItems(HumanTaskAdminClient humanTaskAdminClient)
            throws RemoteException, InterruptedException,
                   SearchAdminServiceRegistryExceptionException {
        Calendar startTime = Calendar.getInstance();
        WorkItem[] workItems = null;
        while (((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < 30000) {
            log.info("waiting for work items ..... ");
            try {
                workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
            } catch (Exception e) {
                log.error("Error while getting work items... ", e);
                return workItems;
            }
            if (workItems != null) {
                return workItems;
            }
            Thread.sleep(5000);
        }
        return workItems;
    }

}