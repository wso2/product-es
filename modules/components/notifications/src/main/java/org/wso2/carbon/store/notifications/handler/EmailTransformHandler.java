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

package org.wso2.carbon.store.notifications.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.mail.MailConstants;
import org.wso2.carbon.store.notifications.management.Constants;

import java.util.Collections;

/**
 * Handler to transform e-mails to html content
 */
public class EmailTransformHandler extends AbstractHandler implements Handler {
    private String name;

    /**
     * Get name of the handler
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set content type to text/html
     *
     * @param msgContext message context
     * @return Invocation response
     * @throws AxisFault
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        if (msgContext.getTo() != null && msgContext.getTo().getAddress().startsWith(Constants.MAILTO_TAG)) {
            msgContext.getOptions().setProperty(MailConstants.TRANSPORT_MAIL_CUSTOM_HEADERS, Collections.singletonMap(Constants.CONTENT_TYPE, Constants.TEXT_HTML));
        }
        return InvocationResponse.CONTINUE;
    }

    /**
     * Set name of the handler
     *
     * @param name name of the handler
     */
    public void setName(String name) {
        this.name = name;
    }
}
