/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;

public class FileUploadWithAttachmentUtil {

    /**
     * This method uploads a content-type asset (ex: wsdl,policy,wadl,swagger)
     * to a running G-Reg instance
     *
     * @param filePath     The absolute path of the file
     * @param fileVersion  Version of the file
     * @param fileName     Name of the file
     * @param shortName    Asset shortname mentioned in the RXT
     * @param cookieHeader Session cookie
     * @throws IOException
     */
    public static PostMethod uploadContentTypeAssets(String filePath, String fileVersion, String fileName,
                                                     String shortName, String cookieHeader, String apiUrl)
            throws IOException {

        File file = new File(filePath);
        //The api implementation requires fileUpload name in the format
        //of shortname_file (ex: wsdl_file)
        FilePart fp = new FilePart(shortName + "_file", file);
        fp.setContentType(MediaType.TEXT_PLAIN);
        String version = fileVersion;
        String name = fileName;
        StringPart sp1 = new StringPart("file_version", version);
        sp1.setContentType(MediaType.TEXT_PLAIN);
        StringPart sp2 = new StringPart(shortName + "_file_name", name);
        sp2.setContentType(MediaType.TEXT_PLAIN);
        //Set file parts and string parts together
        final Part[] part = {fp, sp1, sp2};

        HttpClient httpClient = new HttpClient();
        PostMethod httpMethod = new PostMethod(apiUrl);

        httpMethod.addRequestHeader("Cookie", cookieHeader);
        httpMethod.addRequestHeader("Accept", MediaType.APPLICATION_JSON);
        httpMethod.setRequestEntity(
                new MultipartRequestEntity(part, httpMethod.getParams())
        );
        httpClient.executeMethod(httpMethod);
        return httpMethod;
    }


}
