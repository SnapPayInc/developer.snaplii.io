package com.example;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HmacUtil {

//    public static String hmacMD5(String key, String content) throws Exception {
//        SecretKey skey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacMD5");
//        Mac mac = Mac.getInstance("HmacMD5");
//        mac.init(skey);
//        mac.update(content.getBytes(StandardCharsets.UTF_8));
//        byte[] result = mac.doFinal();
//        return Base64.getEncoder().encodeToString(result);
//    }

    public static String hmacSHA1(String key, String content) throws Exception {
        SecretKey skey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "hmacSHA1");
        Mac mac = Mac.getInstance("hmacSHA1");
        mac.init(skey);
        mac.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] result = mac.doFinal();
        return Base64.getEncoder().encodeToString(result);
    }
}
