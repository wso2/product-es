/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.es.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.automation.engine.extensions.ExecutionListenerExtension;

public class ESServerExtension1 extends ExecutionListenerExtension {

    private static final Log log = LogFactory.getLog(ESServerExtension1.class);

    public void initiate() {
        try {
            log.info("Initializing Testing Enterprise Store Jaggery-APPs =============================");
            //TODO
        } catch (Exception e) {
            handleException("Error while initiating test environment", e);
        }
    }

    public void onExecutionStart() {
        try {
            log.info("Waiting till Jaggey-Apps get initialized =============================");
            Thread.sleep(20000);
            //TODO implement a logic to confirm that web apps are fully deployed instead of thread sleep
            log.info("Start Test execution =============================");

        } catch (Exception e) {
            handleException("Fail to wait till Jaggey-Apps get initialized ", e);
        }
    }

    public void onExecutionFinish() {
        try {
            log.info("Completed excecuting test cases for testing Jaggery-Apps =============================");

        } catch (Exception e) {
            handleException("Fail to stop complete testing Jaggery-Apps ", e);
        }
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new RuntimeException(msg, e);
    }
}