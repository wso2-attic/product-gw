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

package org.wso2.carbon.gateway.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.test.reports.CodeCoverageUtils;
import org.wso2.carbon.gateway.test.reports.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * This class has the functions that are needed to generate Jacoco code coverage report.
 */
public class JacocoReport {

    private static final Logger log = LoggerFactory.getLogger(JacocoReport.class);

    public static void mergJacocoFiles() {
        try {
            CodeCoverageUtils.executeMerge(PathUtil.getJacocoCoverageHome(), PathUtil.getCoverageMergeFilePath());
            log.info("Jacoco merging all the files.");
        } catch (Exception e) {
            log.info("Something went wrong wile merging Jacoco files", e);
        }
    }

    public static void generateCoverageReport(File classesDir) throws Exception {
        ReportGenerator reportGenerator = new ReportGenerator(new File(PathUtil.getCoverageMergeFilePath()), classesDir,
                new File(CodeCoverageUtils.getJacocoReportDirectory()), null);
        reportGenerator.create();

        log.info("Jacoco coverage dump file path : " + PathUtil.getCoverageDumpFilePath());
        log.info("Jacoco class file path : " + classesDir);
        log.info("Jacoco coverage HTML report path : " + CodeCoverageUtils.getJacocoReportDirectory() + File.separator
                + "index.html");
    }

    /**
     * This methods will insert jacoco agent settings into startup script under JAVA_OPTS
     *
     * @param scriptName - Name of the startup script
     * @throws IOException - throws if shell script edit fails
     */
    public static void insertJacocoAgentToShellScript(String scriptName, String carbonHome) throws IOException {

        String jacocoAgentFile = CodeCoverageUtils.getJacocoAgentJarLocation();
        String coverageDumpFilePath = PathUtil.getCoverageDumpFilePath();

        CodeCoverageUtils
                .insertStringToFile(new File(carbonHome + File.separator + "bin" + File.separator + scriptName + ".sh"),
                        new File(carbonHome + File.separator + "tmp" + File.separator + scriptName + ".sh"),
                        "-Dcom.sun.management.jmxremote",
                        "-javaagent:" + jacocoAgentFile + "=destfile=" + coverageDumpFilePath + "" +
                                ",append=true,includes=" + CodeCoverageUtils.getInclusionJarsPattern(":") + " \\");
    }

    /**
     * This methods will insert jacoco agent settings into windows bat script
     *
     * @param scriptName - Name of the startup script
     * @throws IOException - throws if shell script edit fails
     */
    public static void insertJacocoAgentToBatScript(String scriptName, String carbonHome) throws IOException {

        String jacocoAgentFile = CodeCoverageUtils.getJacocoAgentJarLocation();
        String coverageDumpFilePath = PathUtil.getCoverageDumpFilePath();

        CodeCoverageUtils.insertJacocoAgentToStartupBat(
                new File(carbonHome + File.separator + "bin" + File.separator + scriptName + ".bat"),
                new File(carbonHome + File.separator + "tmp" + File.separator + scriptName + ".bat"), "-Dcatalina.base",
                "-javaagent:" + jacocoAgentFile + "=destfile=" + coverageDumpFilePath + "" +
                        ",append=true,includes=" + CodeCoverageUtils.getInclusionJarsPattern(":"));
    }

    /**
     * This method will check the OS and edit server startup script to inject jacoco agent
     *
     * @throws IOException - If agent insertion fails.
     */
    public static void instrumentForCoverage(String carbonHome) {
        String scriptName = "carbon";

        try {
            if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")) {
                insertJacocoAgentToBatScript(scriptName, carbonHome);
                if (log.isDebugEnabled()) {
                    log.debug("Included files " + CodeCoverageUtils.getInclusionJarsPattern(":"));
                    log.debug("Excluded files " + CodeCoverageUtils.getExclusionJarsPattern(":"));
                }
            } else {
                insertJacocoAgentToShellScript(scriptName, carbonHome);
            }
        } catch (Exception e) {
            log.error("Something went wrong while adding Jaccoc agent to script", e);
        }
    }

    public static String getCarbonHome(String carbonServerZipFile) throws IOException {
        int indexOfZip = carbonServerZipFile.lastIndexOf(".zip");
        String carbonHome;
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
        }
        return carbonHome;
    }
}
