/*
*Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.gateway.test.utils;

import java.io.File;
import java.util.Locale;

/**
 * PathUtil
 */
public class PathUtil {

    public static String getJacocoCoverageHome() {
        return System.getProperty("basedir") + File.separator + "target" + File.separator +
                "jacoco";
    }

    public static String getCoverageMergeFilePath() {
        return getJacocoCoverageHome() + File.separator + "jacoco-data-merge" + ".exec";
    }

    /**
     * Get report creation location
     */
    public static String getJacocoReportDirectory() {
        String jacocoReportDir = System.getProperty("report.dir");
        if (jacocoReportDir == null) {
            jacocoReportDir = PathUtil.getCoverageDirPath();
        }

        return jacocoReportDir;
    }

    public static String getCoverageDirPath() {
        return System.getProperty("basedir") + File.separator + "target" + File.separator +
                "jacoco" + File.separator + "coverage";
    }

    public static String getUserDirPath() {
        return System.getProperty("user.dir");
    }

    public static String getSystemResourceLocation() {
        String resourceLocation;
        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")) {
            resourceLocation = System.getProperty("framework.resource.location").replace("/", "\\");
        } else {
            resourceLocation = System.getProperty("framework.resource.location").replace("/", "/");
        }
        return resourceLocation;
    }

    public static String getCoverageDumpFilePath() {
        return getJacocoCoverageHome() + File.separator + "jacoco" + System.currentTimeMillis() + ".exec";
    }

    public static String getJarExtractedFilePath() {
        return System.getProperty("basedir") + File.separator + "target" + File.separator + "jar";
    }
}
