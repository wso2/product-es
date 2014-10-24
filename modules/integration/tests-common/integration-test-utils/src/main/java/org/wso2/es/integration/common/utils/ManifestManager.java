/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.es.integration.common.utils;

import org.apache.commons.io.FileUtils;
import org.wso2.es.integration.common.utils.domain.InstallationManifest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.es.integration.common.utils.domain.Resource;
import org.wso2.es.integration.common.utils.domain.ResourcePackage;

public class ManifestManager {

    private InstallationManifest manifest;
    private String carbonHome;
    private static final Log log = LogFactory.getLog(ManifestManager.class);

    public ManifestManager(String carbonHome) throws IOException {
        this.carbonHome = carbonHome;
        init();
    }

    public void execute() {
        //Obtain the list of all resource packages
        Collection<String> packages = manifest.getResourcePackageNames();
        Iterator<String> packageIterator = packages.iterator();
        String packageName;
        ResourcePackage resourcePackage;
        //Go through each package and copy the resources
        while (packageIterator.hasNext()) {
            packageName = packageIterator.next();
            resourcePackage = manifest.getResourcePackage(packageName);
            copyResources(resourcePackage);
        }
    }

    public static String getManifestPath() {
        return File.separator + ManifestConstants.SAMPLE_DIR + getInstallationManifestFile();
    }

    public static String getInstallationManifestFile() {
        return File.separator + ManifestConstants.MANIFEST_FILE_NAME;
    }

    private void init() throws IOException {
        String manifestPath = this.carbonHome + ManifestManager.getManifestPath();
        try {
            manifest = ManifestUtils.load(manifestPath);
        } catch (Exception e) {
            log.error("Unable to initialize the ManifestManager as the test manifest file could not be read" +
                    ".This file should be located in the samples directory as test_manifest.json", e);
            throw new IOException("Unable to read the manifest file test_manifest.json");
        }
    }

    /**
     * Copies the resources defined for the package to the destination directory
     *
     * @param resourcePackage A resource package instance
     */
    private void copyResources(ResourcePackage resourcePackage) {
        Collection<Resource> resources = resourcePackage.getResources();
        Iterator<Resource> resourceIterator = resources.iterator();
        Resource resource;
        while (resourceIterator.hasNext()) {
            resource = resourceIterator.next();
            copyResource(resource);
        }
    }

    /**
     * Copies the resource from its source location to its destination
     *
     * @param resource The resource to be copied
     */
    private void copyResource(Resource resource) {
        String source = null;
        String destination = null;
        File sourceFile;
        File destinationFile;
        boolean success = false;
        try {
            source = resource.getSrc();
            destination = resource.getDest();

            source = getCarbonHomePath(source);
            destination = getCarbonHomePath(destination);
            sourceFile = new File(source);
            destinationFile = new File(destination);
            success = copy(sourceFile, destinationFile);
            if (success) {
                log.info("Successfully moved resource at src: " + source + " to destination: " + destination);
            }
        } catch (IOException e) {
            log.warn("Failed to copy resource at " + source + " to destination: " + destination, e);
        }
    }

    /**
     * Appends the carbon home path to the provided path
     *
     * @param filePath A path to a resource within the carbon home
     * @return A path string appended with the carbon home
     */
    private String getCarbonHomePath(String filePath) {
        return carbonHome + File.separator + filePath;
    }

    private boolean copy(File source, File destination) throws IOException {
        boolean success = false;
        if (!source.exists()) {
            log.warn("Source file: " + source.getPath() + " does not exist.Unable to copy resource to destination");
            return success;
        }
        try {
            if (source.isDirectory()) {
                log.info("Copying directory: " + source.getPath());
                FileUtils.copyDirectory(source, destination);
            } else {
                log.info("Copying file: " + source.getPath());
                FileUtils.copyFile(source, destination);
            }
            success = true;
        } catch (IOException e) {
            log.warn("Unable to copy file from source: " + source.getPath() + " to " +
                    "destination: " + destination.getPath(), e);
        }
        return success;
    }
}
