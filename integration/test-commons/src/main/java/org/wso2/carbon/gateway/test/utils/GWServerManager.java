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

package org.wso2.carbon.gateway.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.test.reports.CodeCoverageUtils;
import org.wso2.carbon.gateway.test.reports.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

/**
 * GWServerManager
 */
public class GWServerManager {
    private static final Logger log = LoggerFactory.getLogger(GWServerManager.class);
    private Process process;
    private Process tempProcess;
    private String carbonHome;
    private String originalUserDir = null;
    private InputStreamHandler inputStreamHandler;
    private InputStreamHandler errorStreamHandler;
    private int defaultHttpsPort = 9090;
    private boolean isCoverageEnable = false;

    public void startServerUsingCarbonHome(String carbonHome) {
        startServerUsingCarbonHome(carbonHome, carbonHome, "carbon");
    }

    public void startServerUsingCarbonHome(String carbonHome, String carbonFolder, String scriptName) {
        if (process == null) {
            try {
                //System.setProperty("carbon.home", carbonFolder);
                originalUserDir = PathUtil.getUserDirPath();
                //System.setProperty("user.dir", carbonFolder);
                File commandDir = new File(carbonHome);
                if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")) {
                    commandDir = new File(carbonHome + File.separator + "bin");
                    tempProcess = Runtime.getRuntime()
                            .exec(new String[] { "cmd.exe", "/c", scriptName + ".bat", "start" }, null, commandDir);
                } else {
                    tempProcess = Runtime.getRuntime()
                            .exec(new String[] { "sh", "bin/" + scriptName + ".sh", "start" }, null, commandDir);
                }

                errorStreamHandler = new InputStreamHandler("errorStream", tempProcess.getErrorStream());
                inputStreamHandler = new InputStreamHandler("inputStream", tempProcess.getInputStream());
                inputStreamHandler.start();
                errorStreamHandler.start();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            log.info("Shutting down server...");
                            shutdownServer();

                        } catch (Exception ex) {
                            log.error("Cannot shutdown server", ex);
                        }

                    }
                });

                ClientConnectionUtil.waitForPort(defaultHttpsPort, 300000L, false);
                log.info("Server started successfully.");
            } catch (IOException var10) {
                throw new RuntimeException("Unable to start server", var10);
            }
            process = tempProcess;
        }
    }

    public String setUpCarbonHome(String carbonServerZipFile) throws IOException {
        if (process != null) {
            return carbonHome;
        } else {
            int indexOfZip = carbonServerZipFile.lastIndexOf(".zip");
            if (indexOfZip == -1) {
                throw new IllegalArgumentException(carbonServerZipFile + " is not a zip file");
            } else {
                String fileSeparator = File.separator.equals("\\") ? "\\" : "/";
                if (fileSeparator.equals("\\")) {
                    carbonServerZipFile = carbonServerZipFile.replace("/", "\\");
                }

                String extractedCarbonDir = carbonServerZipFile
                        .substring(carbonServerZipFile.lastIndexOf(fileSeparator) + 1, indexOfZip);
                FileManipulator.deleteDir(extractedCarbonDir);
                String extractDir = "gwtmp" + System.currentTimeMillis();
                String baseDir = System.getProperty("basedir", ".") + File.separator + "target";
                ArchiveManipulatorUtil.extractFile(carbonServerZipFile, baseDir + File.separator + extractDir);
                carbonHome = (new File(baseDir)).getAbsolutePath() + File.separator + extractDir + File.separator
                        + extractedCarbonDir;

                //insert Jacoco agent configuration to carbon server startup script. This configuration
                //cannot be directly pass as server startup command due to script limitation.
                if (isCoverageEnable) {
                    instrumentForCoverage();
                }

                return carbonHome;
            }
        }
    }

    public void shutdownServer() throws Exception {
//        if (process != null && process.isAlive()) {

            File commandDir = new File(carbonHome);
            if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")) {
                commandDir = new File(carbonHome + File.separator + "bin");
                tempProcess = Runtime.getRuntime()
                        .exec(new String[] { "cmd.exe", "/c", "carbon.bat", "stop" }, null, commandDir);
            } else {
                tempProcess = Runtime.getRuntime()
                        .exec(new String[] { "sh", "bin/carbon.sh", "stop" }, null, commandDir);
            }

//            String processID = getProcessID(defaultHttpsPort).trim();
//            if (!processID.isEmpty()) {
//                if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")) {
//                    //TODO need to implement
//                } else {
//                    Runtime.getRuntime().exec("kill -9 " + processID);
//                }
                if (inputStreamHandler != null) {
                    inputStreamHandler.stop();
                    inputStreamHandler = null;
                }
                if (errorStreamHandler != null) {
                    errorStreamHandler.stop();
                    errorStreamHandler = null;
                }
                process = null;
                System.clearProperty("carbon.home");
                System.setProperty("user.dir", originalUserDir);
//            }
            Thread.sleep(1000);

            //generate coverage report
            if (isCoverageEnable) {
                try {
                    log.info("Generating Jacoco code coverage...");
                    generateCoverageReport(new File(carbonHome + File.separator + "repository" +
                            File.separator + "components" + File.separator + "plugins" + File.separator));
                } catch (IOException e) {
                    log.error("Failed to generate code coverage ", e);
                }
            }

            Thread.sleep(1000);
//        }
    }

    public String getProcessID(int port) throws java.io.IOException {
        Scanner s = new Scanner(Runtime.getRuntime().exec("lsof -t -i:" + port).getInputStream(), "UTF-8")
                .useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void generateCoverageReport(File classesDir) throws Exception {

        CodeCoverageUtils.executeMerge(PathUtil.getJacocoCoverageHome(), PathUtil.getCoverageMergeFilePath());
        ReportGenerator reportGenerator = new ReportGenerator(new File(PathUtil.getCoverageMergeFilePath()), classesDir,
                new File(CodeCoverageUtils.getJacocoReportDirectory()), null);
        reportGenerator.create();

        log.info("Jacoco coverage dump file path : " + PathUtil.getCoverageDumpFilePath());
        log.info("Jacoco class file path : " + classesDir);
        log.info("Jacoco coverage HTML report path : " + CodeCoverageUtils.getJacocoReportDirectory() + File.separator
                + "index.html");
    }

    /**
     * This method will check the OS and edit server startup script to inject jacoco agent
     *
     * @throws IOException - If agent insertion fails.
     */
    private void instrumentForCoverage() throws IOException {
        String scriptName = CommonUtil.getStartupScriptFileName(carbonHome);

        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")) {
            insertJacocoAgentToBatScript(scriptName);
            if (log.isDebugEnabled()) {
                log.debug("Included files " + CodeCoverageUtils.getInclusionJarsPattern(":"));
                log.debug("Excluded files " + CodeCoverageUtils.getExclusionJarsPattern(":"));
            }
        } else {
            insertJacocoAgentToShellScript(scriptName);
        }
    }

    /**
     * This methods will insert jacoco agent settings into windows bat script
     *
     * @param scriptName - Name of the startup script
     * @throws IOException - throws if shell script edit fails
     */
    private void insertJacocoAgentToBatScript(String scriptName) throws IOException {

        String jacocoAgentFile = CodeCoverageUtils.getJacocoAgentJarLocation();
        String coverageDumpFilePath = PathUtil.getCoverageDumpFilePath();

        CodeCoverageUtils.insertJacocoAgentToStartupBat(
                new File(carbonHome + File.separator + "bin" + File.separator + scriptName + ".bat"),
                new File(carbonHome + File.separator + "tmp" + File.separator + scriptName + ".bat"), "-Dcatalina.base",
                "-javaagent:" + jacocoAgentFile + "=destfile=" + coverageDumpFilePath + "" +
                        ",append=true,includes=" + CodeCoverageUtils.getInclusionJarsPattern(":"));
    }

    /**
     * This methods will insert jacoco agent settings into startup script under JAVA_OPTS
     *
     * @param scriptName - Name of the startup script
     * @throws IOException - throws if shell script edit fails
     */
    private void insertJacocoAgentToShellScript(String scriptName) throws IOException {

        String jacocoAgentFile = CodeCoverageUtils.getJacocoAgentJarLocation();
        String coverageDumpFilePath = PathUtil.getCoverageDumpFilePath();

        CodeCoverageUtils
                .insertStringToFile(new File(carbonHome + File.separator + "bin" + File.separator + scriptName + ".sh"),
                        new File(carbonHome + File.separator + "tmp" + File.separator + scriptName + ".sh"),
                        "-Dcom.sun.management.jmxremote",
                        "-javaagent:" + jacocoAgentFile + "=destfile=" + coverageDumpFilePath + "" +
                                ",append=true,includes=" + CodeCoverageUtils.getInclusionJarsPattern(":") + " \\");
    }
}
