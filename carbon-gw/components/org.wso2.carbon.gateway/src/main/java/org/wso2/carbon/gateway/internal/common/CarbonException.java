/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.gateway.internal.common;

/**
 * Custom exception class for gateway specific exceptions
 */
public class CarbonException extends Exception {

    public CarbonException() {
    }

    public CarbonException(String message) {
        super(message);
    }

    public CarbonException(Throwable cause) {
        super(cause);
    }

    public CarbonException(String message, Throwable cause) {
        super(message, cause);
    }

}
