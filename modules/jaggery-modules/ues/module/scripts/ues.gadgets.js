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
var gadgets = gadgets || {};
var log = new Log();
(function () {
    gadgets.addGadgetViaAppContext = function (url, options) {
        var layout = application.get('gadgetLayout') || {};

        if (!getGadgetProps(options.id, layout)) {
            layout[options.container] = layout[options.container] || {};

            layout[options.container][options.id] = layout[options.container][options.id] || {
                'url': url,
                'userPrefs': options.userPrefs
            };

            application.put('gadgetLayout', layout);
        }
    };

    gadgets.getGadgetCode = function (url, options) {
        var clientOpt = {
            'url': url,
            'userPrefs': options.userPrefs
        };
        return '<script type="text/javascript"> '
            + 'if(!__gadgetLayout){' +
            '    var __gadgetLayout = {};' +
            ' }' +
            ' __gadgetLayout["' + options.container + '"] =  __gadgetLayout["' + options.container + '"] || {};' +
            ' __gadgetLayout["' + options.container + '"]["' + options.id + '"] = ' + stringify(clientOpt) + ';' +
            '</script>';
    };
    gadgets.addGadget = function (url, options) {
        print(gadgets.getGadgetCode(url, options));
    };

    var getGadgetProps = function (id, layout) {
        for (var gadgetAreaId in layout) {
            var gadgetArea = layout[gadgetAreaId];
            for (var gadgetId in gadgetArea) {
                if (gadgetId == id) {
                    return (layout[gadgetAreaId][gadgetId]);
                }
            }
        }
    };

})();
