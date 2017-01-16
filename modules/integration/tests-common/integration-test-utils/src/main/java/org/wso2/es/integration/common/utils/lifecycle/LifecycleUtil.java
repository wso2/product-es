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
package org.wso2.es.integration.common.utils.lifecycle;

import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.es.integration.common.clients.*;
import org.wso2.es.integration.common.utils.RegistryProviderUtil;
import org.wso2.es.integration.common.utils.subscription.WorkItemClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

public class LifecycleUtil {

    private AutomationContext automationContext;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private UserManagementClient userManagementClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private HumanTaskAdminClient humanTaskAdminClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private static final String ASPECT_NAME = "StateDemoteLC";
    private static final String ACTION_DEMOTE = "Demote";
    private static final String LC_STATE2 = "Development";
    private static final String ACTION_ITEM_CLICK = "itemClick";
    private static final String CHILD = "resource.txt";
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    /**
     * @param path    path of the collection or resource to be subscribed
     * @param type    type of the element
     * @return true if subscriptions are created for all events and required
     *         notifications are captured, false otherwise
     * @throws Exception
     */
    public boolean init(String path, AutomationContext automationContext, String type)
            throws Exception {

        this.automationContext = automationContext;
        backEndUrl = this.automationContext.getContextUrls().getBackEndUrl();
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(this.automationContext);
        sessionCookie = loginLogoutClient.login();
        userName = this.automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(this.automationContext);
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backEndUrl, sessionCookie);
        userManagementClient =
                new UserManagementClient(backEndUrl, sessionCookie);

        infoServiceAdminClient =
                new InfoServiceAdminClient(backEndUrl, sessionCookie);

        humanTaskAdminClient =
                new HumanTaskAdminClient(backEndUrl, sessionCookie);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl, sessionCookie);

        addRole();
        boolean result1 = true;
        boolean result2 = true;
        boolean result3 = true;
        boolean result4 = true;
        boolean result5 = true;
        boolean result6 = true;
        boolean result7 = true;
        boolean result8 = true;
        if (!path.equals("/")) {
            result1 =
                    consoleSubscribe(path, "LifeCycleCreated") && addLifeCycle(path) &&
                    getNotification("The LifeCycle was created") &&
                    managementUnsubscribe(path);
            result2 =
                    consoleSubscribe(path, "CheckListItemChecked") &&
                    checkItem(path) &&
                    getNotification("The CheckList item 'Effective Inspection Completed' of LifeCycle State 'Tested' was Checked" +
                                    " for resource at " + path + ".") &&
                    managementUnsubscribe(path);
            result3 =
                    consoleSubscribe(path, "CheckListItemUnchecked") &&
                    unCheckItem(path) &&
                    getNotification("The CheckList item 'Effective Inspection Completed' of LifeCycle State 'Tested' was Unchecked" +
                                    " for resource at " + path + ".") &&
                    managementUnsubscribe(path);
            result4 =
                    consoleSubscribe(path, "LifeCycleStateChanged") &&
                    changeState(path) &&
                    getNotification("The LifeCycle State Changed from 'Tested' to 'Development'" +
                                    " for resource at " + path + ".") &&
                    managementUnsubscribe(path);
        }
        result5 =
                consoleSubscribe(path, type + "Updated") && update(path) &&
                getNotification("at path " + path + " was updated.") &&
                managementUnsubscribe(path);

        if (type.equals("Collection")) {
            result6 =
                    consoleSubscribe(path, "ChildCreated") &&
                    addChild(path) &&
                    getNotification("A resource was added to the collection " + path +
                                    " at Path: " + path + "/" + CHILD) &&
                    managementUnsubscribe(path);
            result7 =
                    consoleSubscribe(path, "ChildDeleted") &&
                    deleteChild(path) &&
                    getNotification("A resource was removed from the collection " + path +
                                    " at Path: " + path + "/" + CHILD) &&
                    managementUnsubscribe(path);
        }
        if (!path.equals("/")) {
            result8 =
                    consoleSubscribe(path, type + "Deleted") && delete(path) &&
                    getNotification("at path " + path + " was deleted.");
        }
        clean();
        return result1 && result2 && result3 && result4 && result5 && result6 && result7 && result8;
    }

    /**
     * add a role
     *
     * @return true if the created role exist
     * @throws Exception
     */
    private boolean addRole() throws Exception {

        if (!userManagementClient.roleNameExists("RoleSubscriptionTest")) {
            userManagementClient.addRole("RoleSubscriptionTest",
                                         new String[]{userNameWithoutDomain}, new String[]{""});
        }
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
    private boolean consoleSubscribe(String path, String eventType) throws RemoteException,
                                                                           RegistryException {

        // subscribe for management console notifications
        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(path, "work://RoleSubscriptionTest", eventType,
                                                 sessionCookie);
        return bean.getSubscriptionInstances() != null;

    }

    /**
     * get management console subscriptions
     *
     * @param type type of the element
     * @return true if the required notification is generated, false otherwise
     * @throws java.rmi.RemoteException
     * @throws IllegalStateFault
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws InterruptedException
     */
    private boolean getNotification(String type) throws RemoteException, IllegalStateFault,
                                                        IllegalAccessFault,
                                                        IllegalArgumentFault,
                                                        InterruptedException {
        boolean success = false;
        Thread.sleep(3000);//force delay otherwise getWorkItems return null
        // get all the management console notifications
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
        for (WorkItem workItem : workItems) {
            // search for the correct notification
            if ((workItem.getPresentationSubject().toString()).contains(type)) {
                success = true;
                break;
            }
        }
        workItems = null;
        return success;
    }

    /**
     * add a life cycle to a collection or resource
     *
     * @param path path of the collection or resource to be subscribed
     * @return true if the life cycle is added to the resource or collection,
     *         false otherwise
     * @throws Exception
     */
    private boolean addLifeCycle(String path) throws Exception {
        LifeCycleManagementClient lifeCycleManagementClient =
                new LifeCycleManagementClient(
                        backEndUrl, sessionCookie);
        String filePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +

                File.separator + "es" + File.separator + "lifecycle" +
                File.separator + "StateDemoteLifeCycle.xml";
        String lifeCycleConfiguration = FileManager.readFile(filePath);
        // add life cycle
        lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration);
        wsRegistryServiceClient.associateAspect(path, ASPECT_NAME);
        return (lifeCycleAdminServiceClient.getLifecycleBean(path) != null);
    }

    /**
     * change the state of the life cycle
     *
     * @param path path of the collection or resource to be subscribed
     * @return true if the state changes, false otherwise
     * @throws Exception
     */
    private boolean changeState(String path) throws Exception {
        lifeCycleAdminServiceClient.invokeAspect(path, ASPECT_NAME, ACTION_DEMOTE, null);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(path);
        boolean success = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + ASPECT_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                success = prop.getValues()[0].equalsIgnoreCase(LC_STATE2);
            }
        }
        return success;
    }

    /**
     * check items of the a life cycle
     *
     * @param path path of the collection or resource to be subscribed
     * @return true if all the items of the life cycle are checked, false
     *         otherwise
     * @throws java.rmi.RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    private boolean checkItem(String path) throws RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException {
        lifeCycleAdminServiceClient.invokeAspect(path, ASPECT_NAME, ACTION_ITEM_CLICK,
                                                 new String[]{"true", "true", "true"});
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(path);
        Property[] p = lifeCycle.getLifecycleProperties();
        boolean result1 = false, result2 = false, result3 = false;
        for (Property prop : p) {
			if (("registry.custom_lifecycle.checklist.option." + ASPECT_NAME + ".0.item").equalsIgnoreCase(prop.getKey())) {
                result1 = (prop.getValues()[3].equals("value:true"));
            }
            if (("registry.custom_lifecycle.checklist.option." + ASPECT_NAME + ".1.item").equalsIgnoreCase(prop.getKey())) {
                result2 = (prop.getValues()[3].equals("value:true"));
            }
            if (("registry.custom_lifecycle.checklist.option." + ASPECT_NAME + ".2.item").equalsIgnoreCase(prop.getKey())) {
                result3 = (prop.getValues()[3].equals("value:true"));
            }
        }
        return result1 && result2 && result3;
    }

    /**
     * uncheck items of the a life cycle
     *
     * @param path path of the collection or resource to be subscribed
     * @return true if all the items of the life cycle are unchecked, false
     *         otherwise
     * @throws java.rmi.RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    private boolean unCheckItem(String path) throws RemoteException,
                                                    CustomLifecyclesChecklistAdminServiceExceptionException {
        lifeCycleAdminServiceClient.invokeAspect(path, ASPECT_NAME, ACTION_ITEM_CLICK,
                                                 new String[]{"false", "false", "false"});
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(path);
        Property[] p = lifeCycle.getLifecycleProperties();
        boolean resultProp1 = false;
        boolean resultProp2 = false;
        boolean resultProp3 = false;
        for (Property prop : p) {
            if (("registry.custom_lifecycle.checklist.option." + ASPECT_NAME + ".0.item").equalsIgnoreCase(prop.getKey())) {
                resultProp1 = (prop.getValues()[3].equals("value:false"));
            }
            if (("registry.custom_lifecycle.checklist.option." + ASPECT_NAME + ".1.item").equalsIgnoreCase(prop.getKey())) {
                resultProp2 = (prop.getValues()[3].equals("value:false"));
            }
            if (("registry.custom_lifecycle.checklist.option." + ASPECT_NAME + ".2.item").equalsIgnoreCase(prop.getKey())) {
                resultProp3 = (prop.getValues()[3].equals("value:false"));
            }

        }
        return resultProp1 && resultProp2 && resultProp3;
    }

    /**
     * update a collection or resource
     *
     * @param path path of the collection or resource to be subscribed
     * @return true if property exists, false otherwise
     * @throws Exception
     */
    private boolean update(String path)
            throws RemoteException, PropertiesAdminServiceRegistryExceptionException {
        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient( backEndUrl, sessionCookie);
        propertiesAdminServiceClient.setProperty(path, "TestPropertyUpdate", "TestValueUpdate");

        long startTime = System.currentTimeMillis();
        long waitTime = 10000;

        while ((System.currentTimeMillis() - startTime) < waitTime) {
            try {
                if (resourceAdminServiceClient.getProperty(path, "TestPropertyUpdate").equals("TestValueUpdate")) {
                    return true;
                }
            } catch (ResourceAdminServiceExceptionException ignored) {

            }
        }
        return false;
    }


    /**
     * add a resource to a collection
     *
     * @param path path of the collection
     * @return true if resource is added to the collection, false otherwise
     * @throws Exception
     */
    private boolean addChild(String path) throws Exception {

        String resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "es" + File.separator + CHILD;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(path + "/" + CHILD, "test/plain", "testDesc", dh);
        return resourceAdminServiceClient.getResource(path + "/" + CHILD)[0].getAuthorUserName()
                .contains(userNameWithoutDomain);

    }

    /**
     * delete a resource from a collection
     *
     * @param path path of the collection
     * @return true if resource is deleted from the collection, false otherwise
     * @throws Exception
     */
    private boolean deleteChild(String path) throws Exception {

        return resourceAdminServiceClient.deleteResource(path + "/" + CHILD);
    }

    /**
     * delete a resource or a collection
     *
     * @param path path of the resource or the collection
     * @return true if the collection or resource is deleted, false otherwise
     * @throws Exception
     */
    private boolean delete(String path) throws Exception {
        return resourceAdminServiceClient.deleteResource(path);
    }

    /**
     * subscribe for management console subscription
     *
     * @param path path of the resource or the collection
     * @return true if the subscription is created, otherwise false
     * @throws Exception
     */
    public boolean managementUnsubscribe(String path) throws Exception {

        String sessionID = sessionCookie;
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(path, sessionID);
        infoServiceAdminClient.unsubscribe(path, sBean.getSubscriptionInstances()[0].getId(),
                                           sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(path, sessionID);
        return (sBean.getSubscriptionInstances() == null);
    }

    /**
     * delete the role created
     *
     * @throws Exception
     */
    private void clean() throws Exception {
        if (userManagementClient.roleNameExists("RoleSubscriptionTest")) {
//            userManagementClient.deleteRole("RoleSubscriptionTest");
        }

        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        humanTaskAdminClient = null;
        resourceAdminServiceClient = null;
        registryProviderUtil = null;
    }
}
