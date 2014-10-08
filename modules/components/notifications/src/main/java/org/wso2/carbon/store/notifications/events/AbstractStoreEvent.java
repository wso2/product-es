/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.store.notifications.events;

import org.wso2.carbon.registry.common.eventing.RegistryEvent;

public abstract class AbstractStoreEvent<T> extends RegistryEvent<T> {

    protected String eventName;
    private String resourcePath;
    private int tenantId;

    public AbstractStoreEvent(T message) {
        super(message);
    }

    /**
     * Set the path of the resource related to event
     *
     * @param resourcePath resource path on which the event triggered
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        setTopic(TOPIC_SEPARATOR + eventName + resourcePath);
        setOperationDetails(resourcePath, eventName, RegistryEvent.ResourceType.UNKNOWN);
    }

    /**
     * Get the resource path related to the event
     *
     * @return resourcePath
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Returns the tenantId
     *
     * @return tenantId
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * Set the tenantId
     *
     * @param tenantId tenantId
     */
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
