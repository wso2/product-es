package org.wso2.carbon.social.core;

import org.mozilla.javascript.NativeObject;

public interface ActivityPublisher {
    String publish(NativeObject activity);
}
