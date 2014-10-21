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

package org.wso2.es.integration.common.utils.domain;

import java.io.IOException;

/**
 * Represents a resource that can be moved within the unzipped contents of the
 */
public class Resource {
    //Source of the resource
    private String src;

    //Destination of the resource
    private String dest;

    /**
     * Returns the destination of the resource.If a destination value is not given
     * then the destination becomes the source
     *
     * @return
     */
    public String getDest() throws IOException {
        //If there is not destination then look for the source
        if (dest == null) { //TODO: This needs to be supported by assuming the root
            return getSrc();
        }
        return dest;
    }

    public String getSrc() throws IOException {
        if (src == null) {
            throw new IOException("A source location for the resource has not been defined");
        }
        return src;
    }
}
