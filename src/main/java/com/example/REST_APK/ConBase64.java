package com.example.REST_APK;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class ConBase64 {

    public static String convert(File originalFile) {

        String encodedBase64 = null;
        try (FileInputStream fileInputStreamReader = new FileInputStream(originalFile)) {
            byte[] bytes = new byte[(int) originalFile.length()];
            fileInputStreamReader.read(bytes);
            encodedBase64 = new String(Base64.encodeBase64(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        originalFile.deleteOnExit();

        return "{\"b64\":\"" + encodedBase64 + "\"}";
    }


}
