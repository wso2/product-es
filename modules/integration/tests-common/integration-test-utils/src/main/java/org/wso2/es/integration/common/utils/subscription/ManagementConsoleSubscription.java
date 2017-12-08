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

import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.es.integration.common.clients.*;

import java.rmi.RemoteException;

public class ManagementConsoleSubscription {

    private static AutomationContext automationContext;
    private static String sessionCookie;
    private static String backEndUrl;
    private static String userName;
    private static String userNameWithoutDomain;

    private ManagementConsoleSubscription() {
    }

    /**
     * Subscribe for management console notifications and receive the
     * notification
     *
     * @param path      path of the resource or collection
     * @param eventType event type to be subscribed
     * @param env       ManageEnvironment
     * @param userInf   UserInfo
     * @return true if the subscription is succeeded and notification is
     *         received, false otherwise
     * @throws Exception
     */
    public static boolean init(String path, String eventType, AutomationContext autoContext)
            throws Exception {

        automationContext = autoContext;
        backEndUrl = automationContext.getContextUrls().getBackEndUrl();
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        sessionCookie = loginLogoutClient.login();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        boolean result = (addRole() && consoleSubscribe(path, eventType) && update(path) && getNotification(path));
        clean(path);
        return result;
    }

    /**
     * add a role
     *
     * @return true if the created role exist
     * @throws Exception
     */
    private static boolean addRole() throws Exception {
        UserManagementClient userManagementClient =
                new UserManagementClient(backEndUrl, sessionCookie);

        if (userManagementClient.roleNameExists("RoleSubscriptionTest")) {
            return true;
        }

        userManagementClient.addRole("RoleSubscriptionTest",
                                     new String[]{userNameWithoutDomain}, new String[]{""});
        return userManagementClient.roleNameExists("RoleSubscriptionTest");
    }

    /**
     * subscribe for management console notifications
     *
     * @param path      path of the collection or resource to be subscribed
     * @param eventType event to be subscribed
     * @return true if the subscription is created, false otherwise
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     */
    private static boolean consoleSubscribe(String path, String eventType) throws RemoteException,
                                                                                  RegistryException {
        InfoServiceAdminClient infoServiceAdminClient =
                new InfoServiceAdminClient(backEndUrl, sessionCookie);
        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(path, "work://RoleSubscriptionTest",
                                                 eventType, sessionCookie);

        return bean.getSubscriptionInstances() != null;

    }

    /**
     * update a collection or resource
     *
     * @param path path of the collection or resource to be subscribed
     * @return true if property exists, false otherwise
     * @throws Exception
     */
    private static boolean update(String path) throws Exception {
        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(backEndUrl, sessionCookie);

        propertiesAdminServiceClient.setProperty(path, "TestProperty", "TestValue");

        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl, sessionCookie);

        return resourceAdminServiceClient.getProperty(path, "TestProperty").equals("TestValue");
    }

    /**
     * get management console subscriptions
     *
     * @param path
     * @return true if the required notification is generated, false otherwise
     * @throws java.rmi.RemoteException
     * @throws IllegalStateFault
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws InterruptedException
     */
    private static boolean getNotification(String path) throws RemoteException, IllegalStateFault,
                                                               IllegalAccessFault,
                                                               IllegalArgumentFault,
                                                               InterruptedException {
        boolean success = false;
        HumanTaskAdminClient humanTaskAdminClient =
                new HumanTaskAdminClient(backEndUrl, sessionCookie);
        Thread.sleep(5000);//force delay otherwise get work items return error

        // get all the notifications
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);

        for (WorkItem workItem : workItems) {
            if (workItem.getPresentationSubject().toString().contains(path + " was updated.")) {
                success = true;
                break;
            }
        }
        return success;
    }

    /**
     * delete the added role and remove the added property of the collection or
     * reource
     *
     * @param path path of the collection or resource
     * @throws Exception
     */
    private static void clean(String path) throws Exception {
        UserManagementClient userManagementClient =
                new UserManagementClient(backEndUrl, sessionCookie);

        if (userManagementClient.roleNameExists("RoleSubscriptionTest")) {
//            userManagementClient.deleteRole("RoleSubscriptionTest");
        }

        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(backEndUrl, sessionCookie);

        propertiesAdminServiceClient.removeProperty(path, "TestProperty");
    }
}
