/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.store.notifications.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidMessageException;
import org.wso2.carbon.event.ws.internal.builders.utils.BuilderUtils;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.eventing.RegistryEventingConstants;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;

public class StoreSubscriptionManager {

    private static final Log log = LogFactory.getLog(StoreSubscriptionManager.class);
    private EventingService eventingService;

    public void subscribe(String userName, String resourcePath,String endpoint, String eventName) {

        eventingService = Utils.getRegistryEventingService();
        RegistryService registryService = Utils.getRegistryService();
        UserRegistry userRegistry = null;

        if (eventingService != null && registryService != null) {
            try {
                userRegistry = registryService.getRegistry(userName);
            } catch (RegistryException e) {
                log.error("User Registry not available. " +e);
            }
            createSubscription(userRegistry, resourcePath, endpoint, eventName);
        }
    }

    public void unsubscribe(String id) {
        eventingService = Utils.getRegistryEventingService();
            if (eventingService != null) {
                eventingService.unsubscribe(id);
            }
    }

    public Map getEventTypes() {
            Map map=null;
            if (eventingService != null) {
                map = eventingService.getEventTypes();
            }
            return map;
    }

    public List<Subscription> getAllSubscriptions(){
        List<Subscription> subscriptionList=null;
        if (eventingService != null) {
            try {
                subscriptionList = eventingService.getAllSubscriptions();
            } catch (EventBrokerException e) {
                log.error("Retrieving all subscriptions failed. "+e);
            }
        }
        return subscriptionList;
    }

    private Subscription createSubscription(UserRegistry userRegistry, String path, String endpoint, String eventName){

        Subscription subscription = null;

        ResourcePath resourcePath = new ResourcePath(path);
        try {
            if (Utils.getRegistryEventingService() == null) {
                throw new IllegalStateException("Registry Eventing Service Not Found");
            } else {
                String topic = RegistryEventingConstants.TOPIC_PREFIX + RegistryEvent.TOPIC_SEPARATOR + eventName + path;

                subscription = BuilderUtils.createSubscription(endpoint, Constants.TOPIC_FILTER, topic);

                subscription.setEventDispatcherName(RegistryEventingConstants.TOPIC_PREFIX);
                int callerTenantId = userRegistry.getCallerTenantId();
                subscription.setTenantId(callerTenantId);
                String name = userRegistry.getUserName();
                if (callerTenantId != MultitenantConstants.SUPER_TENANT_ID &&
                        callerTenantId > -1) {
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext currentContext =
                                PrivilegedCarbonContext.getThreadLocalCarbonContext();
                        currentContext.setTenantId(callerTenantId, true);
                        String tenantDomain = currentContext.getTenantDomain();
                        if (tenantDomain != null &&
                                !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                            name = name + "@" + tenantDomain;
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
                subscription.setOwner(name);
                String subscriptionId;
                    subscriptionId =
                            Utils.getRegistryEventingService().subscribe(subscription);

                if (subscriptionId == null) {
                    throw new IllegalStateException("Subscription Id invalid");
                }
                subscription.setId(subscriptionId);
            }

        } catch (RuntimeException e) {
            log.error("Failed to subscribe to information of the resource " +
                    resourcePath + ".", e);
        } catch (InvalidMessageException e) {
            log.error("Failed to subscribe to information of the resource " +
                    resourcePath + ".", e);
        }
        return subscription;
    }

}
