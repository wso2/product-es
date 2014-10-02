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
package org.wso2.carbon.store.notifications.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.mail.MailConstants;

import java.util.Collections;

public class EmailTransformHandler extends AbstractHandler implements Handler {
    private String name;

    public String getName() {
        return name;
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        if (msgContext.getTo() != null && msgContext.getTo().getAddress().startsWith("mailto:")) {
            msgContext.getOptions().setProperty(MailConstants.TRANSPORT_MAIL_CUSTOM_HEADERS, Collections.singletonMap("Content-Type", "text/html"));
        }
        return InvocationResponse.CONTINUE;
    }

    public void revoke(MessageContext msgContext) {
    }

    public void setName(String name) {
        this.name = name;
    }
}
