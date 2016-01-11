/*
 * Copyright (c) 2015, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.internal.mediation.camel;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.wso2.carbon.messaging.Constants;


/**
 * A class responsible for handle custom error messages
 */
public class CustomErrorHandlingProcessor implements Processor {

    private String errorMessage;

    private String statusCode;

    private String contentType;


    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Throwable throwable = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
                                                   Exception.class);

        if (throwable != null) {
            if (statusCode != null) {
                exchange.setProperty(Constants.HTTP_STATUS_CODE, statusCode);
            }
            if (contentType != null) {
                exchange.setProperty(Constants.HTTP_CONTENT_TYPE, contentType);
            }
            if (errorMessage != null) {
                throwable = new RuntimeException(errorMessage);
            }
            exchange.setException(throwable);
        }
    }
}
