/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.tests.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.wso2.carbon.gateway.test.utils.JacocoReport;

import java.io.File;

public class GWTestSuiteListener implements ISuiteListener {

    private static final Logger log = LoggerFactory.getLogger(GWTestSuiteListener.class);

    @Override
    public void onStart(ISuite iSuite) {
        try {
            GWClientProvider.getInstance().getGwClient().startGateway();
        } catch (Exception e) {
            log.error("Error while staring the Gateway", e);
        }
    }

    @Override
    public void onFinish(ISuite iSuite) {
        try {
            GWClientProvider.getInstance().getGwClient().stopGateway();
            Thread.sleep(1000);
            // TODO: Merge is not needed as only file is created. All the results are appended to file.
            JacocoReport.mergJacocoFiles();
            JacocoReport.generateCoverageReport(new File(JacocoReport.getCarbonHome(System.getProperty("carbon.zip"))
                    + File.separator + "osgi" + File.separator + "plugins" + File.separator));
        } catch (Exception e) {
            log.error("Error while stopping the Gateway", e);
        }
    }
}
