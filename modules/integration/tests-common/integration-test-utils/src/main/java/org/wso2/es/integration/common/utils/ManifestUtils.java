/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.es.integration.common.utils;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.es.integration.common.utils.domain.InstallationManifest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Encapsulates all actions related to reading  installation
 * manifest file
 */
public class ManifestUtils {

    private static final Log log = LogFactory.getLog(ManifestUtils.class);

    /**
     * Reads the manifest file and then converts the JSON content to an instance of the InstallationManifest
     * DAO
     * @param manifestPath The path to manifest file
     * @return An instance of the InstallationManfiest DAO
     * @throws IOException Thrown if the manifest file cannot be found
     */
    public static InstallationManifest load(String manifestPath) throws IOException {
        Gson gson = new Gson();
        BufferedReader reader = null;
        InstallationManifest manifest = null;
        try {
            reader = new BufferedReader(new FileReader(manifestPath));
            manifest = gson.fromJson(reader, InstallationManifest.class);
        } catch (Exception e) {
            log.error("Unable to read the installation manifest file at " + manifestPath, e);
            throw new IOException("Unable to read the installation manifest file.");
        } finally {
            if (reader != null)
                reader.close();
        }
        return manifest;
    }
}
