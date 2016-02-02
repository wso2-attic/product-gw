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

package test.http.test.orderproc;

import com.lmax.disruptor.RingBuffer;


public class OrderProducer {

    private final RingBuffer<Order> ringBuffer;


    public OrderProducer(
            RingBuffer<Order> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }


    public void produce(String orderId, String orderContent, int orderQuantity) {
        long seq = ringBuffer.next();
        try {
            Order order = ringBuffer.get(seq);
            order.setOrderId(orderId);
            order.setOrderContent(orderContent);
            order.setOrderQuantity(orderQuantity);
        } finally {
            ringBuffer.publish(seq);
        }


    }




}
