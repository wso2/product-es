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
 * The artifacts namespace contains methods for working with artifacts
 * of assets and app extensions
 * @namespace
 * @example
 *  var artifacts = require('rxt').artifacts;
 */
var artifacts = {};
(function(artifacts, constants) {
    var ARTIFACT_MAP = 'artifact.map';
    var DEFAULT_ASSET_ARTIFACTS = 'default.asset.artifacts';
    var VALID_ARTIFACTS = ['workflows', 'mail_templates'];
    var EXTENSION_PATH = '/extensions';
    var log = new Log('rxt.artifacts');
    /**
     * Returns the artifacts map
     * @return Object An ArtifactMap instance
     */
    var getArtifactMap = function() {
        var artifactMap = application.get(ARTIFACT_MAP);
        if (!artifactMap) {
            artifactMap = new ArtifactMap();
            application.put(ARTIFACT_MAP, artifactMap);
        }
        return artifactMap;
    };
    /**
     * Represents an artifact
     * @param String name    The name of the artifact
     * @param Object options An options object
     */
    function Artifact(name, options) {
        var configs = options || {};
        if (!typeof name === 'string') {
            throw 'An artifact cannot be created without a name';
        }
        this.name = name;
        this.path = configs.path || '';
        this.fileExtension = configs.extension;
    }
    Artifact.prototype.getImpl = function() {};
    /**
     * Manages artifacts by tenant
     */
    function ArtifactMap() {
        this.artifactMap = {};
    }
    /**
     * Registers a new artifact either as an asset specific artifact or a global
     * artifact
     * @example
     *  //Method #1 : Registering an asset type specific artifact
     *  afm.register(new Artifact(),'workflow',-1234);
     *  //Method #2 : Registering a global artifact
     *  afm.register(new Artifact(),'workflow',-1234,'gadget');
     * @param  Object artifact   An artifact instance
     * @param  String  group     The group name of the artifact (E.g. workflow)
     * @param  Number tenantId   The ID of the tenant
     * @param  String assetType  The type of asset
     */
    ArtifactMap.prototype.register = function(artifact, group, tenantId, assetType) {
        var tenantArtifactMap = this.artifactMap[tenantId] ? this.artifactMap[tenantId] : (this.artifactMap[tenantId] = {});
        var assetMap;
        var typeMap;
        var globalMap;
        var groupMap;
        //Locate the tenants service map
        if (!tenantArtifactMap) {
            throw 'Unable to locate the service map for the tenant: ' + tenantId + ' to register the service: ' + artifact.name;
        }
        //Determine if the asset type is provided
        if (assetType) {
            assetMap = tenantArtifactMap.assets ? tenantArtifactMap.assets : (tenantArtifactMap.assets = {});
            typeMap = assetMap[assetType] ? assetMap[assetType] : (assetMap[assetType] = {});
            groupMap = typeMap[group] ? typeMap[group] : (typeMap[group] = {});
            groupMap[artifact.name] = artifact;
            return true;
        }
        globalMap = tenantArtifactMap.globals ? tenantArtifactMap.globals : (tenantArtifactMap.globals = {});
        groupMap = globalMap[group] ? globalMap[group] : (globalMap[group] = {});
        groupMap[artifact.name] = artifact;
        return true;
    };
    /**
     * Locates an artifact  given its name.The method can be invoked
     * to obtain either a global or an asset specific artifact.
     * @example
     *  //Method #1 : Obtaining an asset  specific artifact
     *  var artifact = afm.get('workflow_asset_create','workflow',-1234,'gadget');
     *  //Method #2 : Obtaining a global artifact
     *  var artifact = afm.get('workflow_asset_create','workflow',-1234);
     *
     * @param  String artifactName The name of the artifact
     * @param  Number tenantId    The tenant id
     * @param  String assetType   (Optional) The type of the asset
     * @return Object             The requested artifact
     */
    ArtifactMap.prototype.get = function(artifactName, group, tenantId, assetType) {
        var tenantArtifactMap = this.artifactMap[tenantId];
        var assetMap;
        var typeMap;
        var globalMap;
        var groupMap;
        var artifact;
        if (!tenantArtifactMap) {
            throw 'Unable to locate the artifact map for the tenant: ' + tenantId + ' to return the service: ' + artifactName;
        }
        assetMap = tenantArtifactMap.assets ? tenantArtifactMap.assets : {};
        log.info(assetMap);
        //Check if the user specified an asset type
        if (assetType) {
            log.info('Asset specific artifact');
            typeMap = assetMap[assetType] ? assetMap[assetType] : {};
            groupMap = typeMap[group] ? typeMap[group] : {};
            artifact = groupMap[artifactName];
        }
        //If an artifact was found at the asset scope,return it
        if (artifact) {
            return artifact;
        }
        log.info('Look under default asset');
        //If not check the default asset extension
        typeMap = assetMap[constants.DEFAULT_ASSET_EXTENSION] ? assetMap[constants.DEFAULT_ASSET_EXTENSION] : {};
        groupMap = typeMap[group] ? typeMap[group] : {};
        artifact = groupMap[artifactName];
        if (artifact) {
            return artifact;
        }
        log.info('Non asset specific artifact');
        //Try to locate the service at the global scope
        globalMap = tenantArtifactMap.globals ? tenantArtifactMap.globals : {};
        groupMap = globalMap[group] ? globalMap[group] : {};
        artifact = groupMap[artifactName];
        return artifact;
    };
    /**
     * Returns the tenant specific artifact map for
     * @param  String tenantId The tenant ID
     * @return Object          A tenant artifact map
     */
    ArtifactMap.prototype.getTenantMap = function(tenantId) {
        //If the tenant map is not present then create a new one 
        if (!this.artifactMap[tenantId]) {
            this.artifactMap[tenantId] = {};
        }
        return this.artifactMap[tenantId];
    };
    artifacts.artifactMap = function() {
        return getArtifactMap();
    };
    /**
     * Returns an artifact matching the name and group provided
     * @param  String artifactName The name of artifact
     * @param  String group        The group of the artifact
     * @param  Number tenantId     The tenant ID
     * @param  String assetType    The type of asset
     * @return Object              The artifact qualified by the name and group
     */
    artifacts.get = function(artifactName, group, tenantId, assetType) {
        var artifactMap = getArtifactMap();
        return artifactMap.get(artifactName, group, tenantId, assetType);
    };
    var buildRelativePath = function(path) {
        var extensionPathIndex = path.indexOf(EXTENSION_PATH, 0);
        var relativePath = path.substring(extensionPathIndex);
        return relativePath;
    };
    /**
     * Checks if the artifact group is allowed (e.g. worflows)
     * @param  String  dirName The artifact group directory name
     * @return Boolean             Indicates whether the directory is allowed
     */
    var isAllowedArtifactGroup = function(dirName) {
        var index = VALID_ARTIFACTS.indexOf(dirName, 0);
        if (index >= 0) {
            return true;
        }
        return false;
    };
    var processArtifactGroup = function(rootDir, artifactMap, tenantId, assetType) {
        var artifacts;
        var artifact;
        var artifactFile;
        var utils = require('utils');
        var fileUtil = utils.file;
        var artifactName;
        var artifactFileExtension;
        var artifactPath;
        var dirName;
        //An artifact group should appear as a directory
        if (!rootDir.isDirectory()) {
            return;
        }
        dirName = rootDir.getName();
        //Obtain the list of sub directories
        artifacts = rootDir.listFiles();
        for (var index = 0; index < artifacts.length; index++) {
            artifactFile = artifacts[index];
            //Only add artifacts within a directory
            //A directory is assumed to be a grouping of artifacts
            if ((!artifactFile.isDirectory()) && (isAllowedArtifactGroup(dirName))) {
                artifactName = fileUtil.getFileName(artifactFile);
                artifactFileExtension = fileUtil.getExtension(artifactFile);
                artifactPath = buildRelativePath(artifactFile.getPath());
                artifact = new Artifact(artifactName, {
                    path: artifactPath,
                    extension: artifactFileExtension
                });
                artifactMap.register(artifact, rootDir.getName(), tenantId, assetType);
                log.info('Registered artifact '+artifactName+' group: '+dirName);
            }
        }
    };
    /**
     * Loads artifacts found in the path provided
     * @param  String path      The path to a location containing artifacts
     * @param  Number tenantId  The ID of the tenant
     * @param  String assetType The asset type under which the artifacts should be loaded
     */
    artifacts.loadDirectory = function(path, tenantId, assetType) {
        //Obtain the tenant artifact map
        var afm = this.artifactMap();
        var dir = new File(path);
        var subDirs;
        //Do not process the directory further  if the directory does not exist
        //or not a directory
        if ((!dir.isExists()) || (!dir.isDirectory())) {
            log.warn('Location ' + path + 'does not exist.Stopped loading artifacts for type: ' + assetType);
            return;
        }
        //Go through each sub directory
        subDirs = dir.listFiles();
        for (var index = 0; index < subDirs.length; index++) {
            subDir = subDirs[index];
            processArtifactGroup(subDir, afm, tenantId, assetType);
        }
    };
}(artifacts, constants));