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

package org.wso2.es.integration.common.clients;

import org.apache.axis2.databinding.types.URI;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TPresentationName;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TPresentationSubject;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TPriority;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TStatus;

import java.util.Calendar;

public class WorkItem {

    private URI id;
    private TPresentationSubject presentationSubject;
    private TPresentationName presentationName;
    private TPriority priority;
    private TStatus status;
    private Calendar createdTime;
    private String role;

    public WorkItem(URI id, TPresentationSubject presentationSubject,
                    TPresentationName presentationName, TPriority priority, TStatus status,
                    Calendar createdTime, String role) {
        this.id = id;
        this.presentationSubject = presentationSubject;
        this.presentationName = presentationName;
        this.priority = priority;
        this.status = status;
        this.createdTime = createdTime;
        this.role = role;
    }

    public URI getId() {
        return id;
    }

    public TPresentationSubject getPresentationSubject() {
        return presentationSubject;
    }

    public TPresentationName getPresentationName() {
        return presentationName;
    }

    public TPriority getPriority() {
        return priority;
    }

    public TStatus getStatus() {
        return status;
    }

    public Calendar getCreatedTime() {
        return createdTime;
    }

    public String getRole() {
        return role;
    }
}
