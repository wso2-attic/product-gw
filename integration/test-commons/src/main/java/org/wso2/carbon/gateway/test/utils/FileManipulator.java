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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * FileManipulator
 */
public class FileManipulator {
    private static final Logger log = LoggerFactory.getLogger(FileManipulator.class);

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {

                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                log.debug("file " + file + " deleted");
            }
        }
    }

    public static void copyDir(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists() && !dstDir.mkdir()) {
                throw new IOException("Fail to create the directory: " + dstDir.getAbsolutePath());
            }

            String[] children = srcDir.list();

            if (children != null) {

                int length = children.length;

                int i = 0;
                while (i < length) {
                    String aChildren = children[i];
                    copyDir(new File(srcDir, aChildren), new File(dstDir, aChildren));
                    ++i;
                }
            }
        } else {
            copyFile(srcDir, dstDir);
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        String dstAbsPath = dst.getAbsolutePath();
        String dstDir = dstAbsPath.substring(0, dstAbsPath.lastIndexOf(File.separator));
        File dir = new File(dstDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Fail to create the directory: " + dir.getAbsolutePath());
        } else {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(dst);
                byte[] e = new byte[10240];

                int len;
                while ((len = in.read(e)) > 0) {
                    out.write(e, 0, len);
                }
            } finally {
                try {
                    in.close();
                } catch (IOException var17) {
                    log.warn("Unable to close the InputStream " + var17.getMessage(), var17);
                }

                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException var16) {
                    log.warn("Unable to close the OutputStream " + var16.getMessage(), var16);
                }
            }
        }
    }

    public static void copyFileToDir(File src, String dstPath) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(src);
            Path newFile = Paths.get(dstPath);

            // Files.createFile(newFile);
            Files.copy(input, newFile, StandardCopyOption.REPLACE_EXISTING);
            //            output = new FileOutputStream(dstPath);
            //            byte[] buf = new byte[1024];
            //            int bytesRead;
            //            while ((bytesRead = input.read(buf)) > 0) {
            //                output.write(buf, 0, bytesRead);
            //            }
            //
        } catch (IOException e) {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e1) {
                    log.error("Error while closing input stream ", e1);
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e1) {
                    log.error("Error while closing output stream ", e1);
                }
            }
            throw e;

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e1) {
                    log.error("Error while closing input stream ", e1);
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e1) {
                    log.error("Error while closing output stream ", e1);
                }
            }
        }
    }

    public static File[] getMatchingFiles(String sourceDir, String fileNamePrefix, String extension) {
        ArrayList fileList = new ArrayList();
        File libDir = new File(sourceDir);
        String libDirPath = libDir.getAbsolutePath();
        String[] items = libDir.list();
        if (items == null) {
            return new File[0];
        } else {
            int length = items.length;

            for (int i = 0; i < length; ++i) {
                String item = items[i];
                if (fileNamePrefix != null && extension != null) {
                    if (item.startsWith(fileNamePrefix) && item.endsWith(extension)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else if (fileNamePrefix == null && extension != null) {
                    if (item.endsWith(extension)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else if (fileNamePrefix != null) {
                    if (item.startsWith(fileNamePrefix)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else {
                    fileList.add(new File(libDirPath + File.separator + item));
                }
            }
            return (File[]) fileList.toArray(new File[fileList.size()]);
        }
    }

    public static void deleteDir(String directory) {
        deleteDir(new File(directory));
    }

    public static void backupFile(File file) {
        if (file.exists()) {
            File change = new File(file.getPath() + "_back");
            if (file.renameTo(change)) {
                log.debug("backup created for " + file);
            }
        }
    }

    public static void restoreBackup(File file) {
        if (file.exists()) {
            FileManipulator.deleteFile(file);
            File change = new File(file.getPath() + "_back");

            if (change.exists()) {
                File original = new File(file.getPath());
                if (change.renameTo(original)) {
                    log.debug("backup restored as " + original);
                }
            }
        }
    }

    public static void removeFile(File file) {
        if (file.exists()) {
            FileManipulator.deleteFile(file);
        }
    }

    public static String getFileName(File file) {
        return file.getName();
    }
}
