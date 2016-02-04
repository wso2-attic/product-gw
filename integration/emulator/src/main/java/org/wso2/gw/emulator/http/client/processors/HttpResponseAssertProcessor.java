/*
 * *
 *  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.gw.emulator.http.client.processors;

import org.wso2.gw.emulator.dsl.Operation;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.gw.emulator.http.params.Header;

import java.util.List;
import java.util.Map;

public class HttpResponseAssertProcessor extends AbstractClientProcessor<HttpClientResponseProcessorContext> {

    @Override
    public void process(HttpClientResponseProcessorContext processorContext) {

        if(!processorContext.getExpectedResponseContext().getAssertionStatus()){
            assertResponseContent(processorContext);
            assertHeaderParameters(processorContext);
        }else{
            System.out.println("Assertion ignored");
        }
    }

    private void assertResponseContent(HttpClientResponseProcessorContext processorContext) {

        if (processorContext.getExpectedResponseContext().getBody().equalsIgnoreCase(processorContext.getReceivedResponseContext()
                                                                                      .getResponseBody())) {
            System.out.println("Equal content");
        } else {
            System.out.println("Wrong content");
        }
    }

    private void assertHeaderParameters(HttpClientResponseProcessorContext processorContext) {

        if(processorContext.getExpectedResponseContext().getHeaders() == null || processorContext.getExpectedResponseContext().getHeaders().isEmpty()) {
            return;
        }
        Map<String, List<String>> receivedHeaders = processorContext.getReceivedResponseContext().getHeaderParameters();
        Operation operation = processorContext.getClientInformationContext().getExpectedResponse().getOperations();;

        boolean value = false;
        if (operation == Operation.AND) {

            for(Map.Entry<String, List<String>> entry : processorContext.getReceivedResponseContext().getHeaderParameters().entrySet()) {
                entry.getKey();
            }

            for (Header header : processorContext.getExpectedResponseContext().getHeaders()) {
                List<String> receivedHeaderValues = receivedHeaders.get(header.getName());

                if (receivedHeaderValues == null || receivedHeaderValues.isEmpty() || !receivedHeaderValues.contains(header.getValue())) {
                    System.out.print("Header not present");
                    break;
                }
            }
        }else{
            for (Header header : processorContext.getExpectedResponseContext().getHeaders()) {
                List<String> receivedHeaderValues = receivedHeaders.get(header.getName());

                if (receivedHeaderValues == null || receivedHeaderValues.isEmpty() || !receivedHeaderValues.contains(header.getValue())) {
                   ;
                }else{
                    value = true;
                }
            }
            if (value){
                System.out.println("Headers are present");
            }else {
                System.out.println("Non of the Headers present");
            }
        }
    }



}
