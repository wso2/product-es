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
 */
/**
 * The workflows namespace contains methods for working with
 * workflows
 * @namespace
 * @example
 * 	var workflows = require('rxt').workflows;
 */
var workflows = {};
(function(workflows, artifacts, constants) {
    var WORKFLOW_GROUP = 'workflows'; //Artifacts are arranged into groups
    /**
     * Returns a workflow implementation file by requiring it.The workflows
     * can be defined in app extensions or asset extensions.
     * @example
     *  //Method #1: Obtaining a non asset specific workflow
     * 	var workflow = workflows.get('self_signup',-1234);
     *
     * 	//Method #2: Obtaining an asset specific workflow
     * 	//If the workflow is not found within the provided asset type it will
     * 	//be searched in the default asset extension
     * 	var workflow = workflows.get('asset_create',-1234,'gadget');
     *
     * @param  String workflowName  A globally unique workflow name
     * @param  Number tenantId     The tenant ID
     * @param  String assetType    The asset type 
     * @return Object              A workflow object exposing process and execute methods
     */
    workflows.get = function(workflowName, tenantId, assetType) {
        tenantId = tenantId || constants.DEFAULT_TENANT;
        var workflow;
        workflow = artifacts.get(workflowName, WORKFLOW_GROUP, tenantId, assetType);
        if (!workflow) {
            throw 'Unable to locate the workflow: ' + workflowName + ' for tenant: ' + tenantId;
        }
        //Check if the workflow path exists
        var workflowFile = new File(workflow.path || '');
        if (!workflowFile.isExists()) {
            throw 'Unable to locate the workflow definition file: ' + workflow.path + ' for workflow: ' + workflowName;
        }
        return require(workflow.path);
    };
}(workflows, artifacts, constants));