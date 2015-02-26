/*
* Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.social.core;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum SortOrder {
    NEWEST(new Comparator<Activity>() {
        @Override
        public int compare(Activity o2, Activity o1) {
            return o1.getTimestamp() - o2.getTimestamp();
        }
    }),
    OLDEST(null),
    POPULAR(new Comparator<Activity>() {

        @Override
        public int compare(Activity o1, Activity o2) {
            int x =  o2.getLikeCount() - o2.getDislikeCount();
            int y = o1.getLikeCount() - o1.getDislikeCount();
            return x - y;
        }
    });

    private Comparator<? super Activity> comparator;

    SortOrder(Comparator<Activity> comparator) {
        this.comparator = comparator;
    }

    public void sort(List<Activity> activities) {
        if (comparator != null) {
            Collections.sort(activities, comparator);
        }
    }

}
