/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.wso2.carbon.store.notifications.management;

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

/**
 * Manages subscriptions within the Store
 */
public class StoreSubscriptionManager {

    private EventingService eventingService = ComponentManager.getRegistryEventingService();

    /**
     * Subscribe a user for a particular event on a resource, in a given method
     *
     * @param userName     user that's subscribing to an event
     * @param resourcePath the resource subscribing to
     * @param endpoint     method of notification (user, role or email)
     * @param eventName    the event subscribing for
     * @throws InvalidMessageException if eventing service or registry service is not available
     * @throws RegistryException       if Registry Service is not available
     */
    public void subscribe(String userName, String resourcePath, String endpoint,
                          String eventName) throws InvalidMessageException, RegistryException {

        RegistryService registryService = ComponentManager.getRegistryService();
        UserRegistry userRegistry;

        if (eventingService == null) {
            throw new IllegalStateException("Eventing Service not available");
        } else if (registryService == null) {
            throw new IllegalStateException("Registry Service not available");
        }

        try {
            userRegistry = registryService.getRegistry(userName);
        } catch (RegistryException e) {
            throw new RegistryException("Getting user registry for " + userName + " failed", e);
        }
        createSubscription(userRegistry, resourcePath, endpoint, eventName);
    }

    /**
     * Remove a subscription by Id
     *
     * @param id Subscription id
     */
    public void unsubscribe(String id) {
        if (eventingService == null) {
            throw new IllegalStateException("Registry Eventing Service Not Found");
        }
        eventingService.unsubscribe(id);
    }

    /**
     * Get existing event types
     *
     * @return map EventType map
     */
    public Map getEventTypes() {
        Map map;
        if (eventingService == null) {
            throw new IllegalStateException("Registry Eventing Service Not Found");
        }
        map = eventingService.getEventTypes();
        return map;
    }

    /**
     * Get all the subscriptions created
     *
     * @return subscriptionList list of Subscriptions
     * @throws EventBrokerException if retrieving subscriptions fail
     */
    public List<Subscription> getAllSubscriptions() throws EventBrokerException {
        List<Subscription> subscriptionList;
        if (eventingService == null) {
            throw new IllegalStateException("Registry Eventing Service Not Found");
        }
        subscriptionList = eventingService.getAllSubscriptions();
        return subscriptionList;
    }

    /**
     * Create a subscription object including subscription information
     *
     * @param userRegistry logged in users registry
     * @param path         path of the resource subscribing to
     * @param endpoint     method of notification (user, role or email)
     * @param eventName    the event subscribing for
     * @return subscription Subscription containing the information
     * @throws InvalidMessageException if subscription creation failed
     */
    private Subscription createSubscription(UserRegistry userRegistry, String path,
                                            String endpoint, String eventName) throws InvalidMessageException {

        Subscription subscription;
        ResourcePath resourcePath = new ResourcePath(path);
        try {
            String topic = RegistryEventingConstants.TOPIC_PREFIX + RegistryEvent.TOPIC_SEPARATOR + eventName + path;
            subscription = BuilderUtils.createSubscription(endpoint, Constants.TOPIC_FILTER, topic);
            subscription.setEventDispatcherName(RegistryEventingConstants.TOPIC_PREFIX);

            int callerTenantId = userRegistry.getCallerTenantId();
            subscription.setTenantId(callerTenantId);

            String name = userRegistry.getUserName();
            //Append the domain name if the user is not in Super Tenant Domain
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
            String subscriptionId = eventingService.subscribe(subscription);

            if (subscriptionId == null) {
                throw new IllegalStateException("Subscription Id invalid");
            }
            subscription.setId(subscriptionId);
            return subscription;
        } catch (InvalidMessageException e) {
            throw new InvalidMessageException("Failed to subscribe to information of the resource " +
                    resourcePath, e);
        }
    }

}
