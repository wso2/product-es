/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
var resources = {};
(function(core, resources) {
    var log = new Log();
    var getRxtExtensionPath = function(type, options) {
        return options.CONFIG_BASE_PATH + options.EXTENSION_PATH + type;
    };
    var getAssetScriptPath = function(type, options) {
        return getRxtExtensionPath(type, options) + options.ASSET_SCRIPT_PATH;
    };
    var getDefaultAssetScriptPath = function(options) {
        return options.DEFAULT_ASSET_SCRIPT;
    };
    var getDefaultAssetTypeScriptPath = function(options, type) {
        return '/extensions/assets/' + type + '/asset.js';
    };
    var addToConfigs = function(tenantId, type, assetResource) {
        var configs = core.configs(tenantId);
        if (!configs.assetResources) {
            configs.assetResources = {};
        }
        configs.assetResources[type] = assetResource;
    };
    var loadAssetScriptContent = function(path) {
        var file = new File(path);
        var content = '';
        if (!file.isExists()) {
            log.warn('Unable to locate default asset script at path ' + path);
            return content;
            //throw 'Unable to load the default asset script.';
        }
        try {
            file.open('r');
            content = file.readAll();
        } catch (e) {
            log.error('Unable to read the default asset script.A custom asset script will not be loaded from ' + path);
            //throw 'Unable to read the default asset script';
        } finally {
            file.close();
        }
        return content;
    };
    var loadDefaultAssetScript = function(options, type, assetResource) {
        var content = loadAssetScriptContent(getDefaultAssetScriptPath(options));
        if (content) {
            assetResource = evalAssetScript(content, assetResource)
        }
        return assetResource;
    };
    var loadAssetScript = function(options, type, assetResource) {
        var content = loadAssetScriptContent(getDefaultAssetTypeScriptPath(options, type));
        var defConfiguration = assetResource.configure();
        var ref = require('utils').reflection;
        if (content) {
            assetResource = evalAssetScript(content, assetResource);
            var ptr = assetResource.configure;
            //The configuration object of the default asset.js needs to be combined with 
            //what is defined by the user in per type asset script
            assetResource.configure = function() {
                var configs = defConfiguration; //Assume the user has not defined a configuration
                //If the user has defined some configuration logic
                if (ptr) {
                    var customConfig = ptr();
                    //Combine the configuration
                    ref.copyAllPropValues(customConfig, configs);
                }
                return configs;
            };
        }
        return assetResource;
    }
    var evalAssetScript = function(scriptContent, assetResource) {
        var module = 'function(asset,log){' + scriptContent + '};';
        var modulePtr = eval(module);
        modulePtr.call(this, assetResource, log);
        return assetResource;
    };
    var buildDefaultResources = function(options, type, assetResource) {
        var asset = {};
        asset.manager = null;
        asset.renderer = null;
        asset.server = null;
        asset.configure = null;
        asset = loadDefaultAssetScript(options, type, asset);
        assetResource._default = asset;
        assetResource.configure = asset.configure;
    };
    var buildAssetResources = function(options, type, assetResource) {
        var asset = {};
        asset.manager = null;
        asset.renderer = null;
        asset.server = null;
        asset.configure = assetResource.configure;
        asset = loadAssetScript(options, type, asset);
        assetResource.manager = asset.manager;
        assetResource.renderer = asset.renderer;
        assetResource.server = asset.server;
        assetResource.configure = asset.configure;
    };
    var loadResources = function(options, tenantId, sysRegistry) {
        var manager = core.rxtManager(tenantId);
        var rxts = manager.listRxtTypes();
        // var resourcePath;
        var type;
        var map = {};
        for (var index in rxts) {
            type = rxts[index];
            // resourcePath = getAssetScriptPath(type, options);
            // var content = sysRegistry.content(resourcePath);
            var asset = {};
            asset.manager = null;
            asset.renderer = null;
            asset.server = null;
            asset.configure = null;
            //asset = loadDefaultAssetScript(options, type, asset);
            //asset = loadAssetScript(options, type, asset);
            buildDefaultResources(options, type, asset);
            buildAssetResources(options,type,asset);
            //Perform any rxt mutations
            if (asset.configure) {
                manager.applyMutator(type, asset.configure());
            }
            // log.info('Configuration: '+asset.toSource());
            addToConfigs(tenantId, type, asset);
        }
    };
    var init = function(tenantId) {
        var server = require('store').server;
        var options = server.options();
        if (!options.rxt) {
            log.error('Unable to locate rxt configuation block in provided tenant configuration.Check the configuration file that is the registry');
            throw 'Unable to locate rxt configuation block in provided tenant configuration.Check the configuration file that is the registry';
        }
        options = options.rxt;
        var sysRegistry = server.systemRegistry(tenantId);
        loadResources(options, tenantId, sysRegistry);
    };
    resources.manager = function(tenantId) {
        init(tenantId);
    };
    resources.init = function() {
        var event = require('event');
        event.on('tenantLoad', function(tenantId) {
            init(tenantId);
        });
    };
}(core, resources));