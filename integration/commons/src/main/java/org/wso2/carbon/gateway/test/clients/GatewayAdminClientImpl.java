/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.gateway.test.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.test.utils.FileManipulator;
import org.wso2.carbon.gateway.test.utils.GWServerManager;
import org.wso2.carbon.gateway.test.utils.PathUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Responsible for start, stop, deploy and restart the GW server
 */
public class GatewayAdminClientImpl implements GatewayAdminClient {

    private static final Logger log = LoggerFactory.getLogger(GatewayAdminClientImpl.class);
    private String carbonZip;
    private String carbonHome;
    private List<String> artifacts;
    private GWServerManager serverManager;

    public GatewayAdminClientImpl() {
        serverManager = new GWServerManager();
    }

    public void startGateway() throws Exception {
        carbonZip = System.getProperty("carbon.zip");
        carbonHome = serverManager.setUpCarbonHome(carbonZip);
        if (carbonHome != null) {
            serverManager.startServerUsingCarbonHome(carbonHome);
        }
    }

    public void stopGateway() throws Exception {
        serverManager.shutdownServer();
    }

    public void restartGateway() throws Exception {
        serverManager.shutdownServer();
        if (carbonHome != null) {
            serverManager.startServerUsingCarbonHome(carbonHome);
        } else {
            log.error("Cannot start gateway - carbon home is not set properly ");
        }
        Thread.sleep(1000);
    }

    public void deployArtifact(String relativeFilePath) {
        if (artifacts == null) {
            artifacts = new ArrayList<String>();
        }
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        String fullPath = PathUtil.getSystemResourceLocation() + File.separator + relativeFilePath;
        try {
            File file = new File(fullPath);
            String dstFileName = carbonHome + "/conf/camel/" + FileManipulator.getFileName(file);
            //FileManipulator.backupFile(new File(dstFileName));
            FileManipulator.copyFileToDir(file, dstFileName);
            artifacts.add(dstFileName);
            log.info("Successfully Deployed");
        } catch (IOException e) {
            log.error("Exception occurred while copying artifacts", e);
        }
    }

    public void cleanArtifacts() {
        for (String artifact : artifacts) {
            // FileManipulator.restoreBackup(new File(artifact));
            FileManipulator.removeFile(new File(artifact));
        }
        log.info("Restored Successfully");
    }
}
