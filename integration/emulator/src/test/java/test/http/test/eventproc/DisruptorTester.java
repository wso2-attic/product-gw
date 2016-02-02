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

package test.http.test.eventproc;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DisruptorTester {

    public static void main(String[] args) throws Exception{


        Executor executor = Executors.newCachedThreadPool();
        MessageEventFactory messageEventFactory = new MessageEventFactory();

        int bufferSize = 1024;
        Disruptor<MessageEvent> disruptor = new Disruptor<MessageEvent>(messageEventFactory, bufferSize, executor);


        disruptor.handleEventsWith(new MessageEventHandler());

        disruptor.start();

        RingBuffer<MessageEvent> ringBuffer = disruptor.getRingBuffer();
        MessageEventProducer producer = new MessageEventProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(1024);
        for (long l = 0; true; l++)
        {
            bb.put(("Foo" + l).getBytes());
            producer.onData(bb);
            Thread.sleep(1000);
        }
    }
}
