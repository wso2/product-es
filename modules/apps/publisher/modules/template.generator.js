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
var EMAIL_TEMPLATE_LC = "<html><h2>Notification for <%=assetType%>:'<%=assetName%>'</h2><h3>Event:lifecycle state changed</h3><p><b>Comment:</b><%=comment%></p></html>";
var EMAIL_TEMPLATE_UPDATE = "<html><h2>Notification for <%=assetType%>:'<%=assetName%>'</h2><h3>Event:<%=assetType%> updated</h3><p><b>Comment:</b><%=comment%></p></html>";
var EMAIL_TEMPLATE_VERSION = "<html><h2>Notification for <%=assetType%>:'<%=assetName%>'</h2><h3>Event:new version created</h3><p><b>Comment:</b><%=comment%></p></html>";
var EMAIL_TEMPLATE_DEFAULT= "<html><h2>Notification</h2><p><b>Comment:</b><%=comment%></p></html>";

var generateEmail = function (tenantId, assetType, assetName, comment, eventName) {
    var stringAssetName=stringify(assetName);
    var stringComment=stringify(comment);
    var stringAssetType=stringify(assetType);

    var EMAIL_TEMP = EMAIL_TEMPLATE_DEFAULT;
    if(eventName=="lc.state.change"){
        EMAIL_TEMP = EMAIL_TEMPLATE_LC;
    }else if(eventName=="asset.update"){
        EMAIL_TEMP = EMAIL_TEMPLATE_UPDATE;
    }else if(eventName=="version.creation"){
        EMAIL_TEMP = EMAIL_TEMPLATE_VERSION;
    }

    message=new JaggeryParser().parse(EMAIL_TEMP).toString();

    var htmlResult = eval('(function() { var result = ""; var assetName='+stringAssetName+'; var comment ='+stringComment+'; var assetType ='+stringAssetType+
        '; print = function(text) { if(typeof text === "object") {result += stringify(text);} else {result += text;} };'+message+' return result;}())');
    return htmlResult;
};



