package org.wso2.gw.emulator.util;

import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by dilshank on 12/16/15.
 */
public class FileRead {

    //private FileInputStream filePath

    public static String getFileBody(File filePath) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(filePath);
        int c;
        String content = "";

        while ((c = fileInputStream.read()) != -1) {
            content += (char)c;
        }
        content = content.replace("\n", "").replace("\r", "");

        //System.out.println(content);
        return content;
    }
}
