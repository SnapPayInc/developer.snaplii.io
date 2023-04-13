/**
 * Alipay.com Inc. Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * SHA256WithRSA签名器
 *
 * @author zhongyu
 * @version $Id: Signer.java, v 0.1 2019年12月19日 9:10 PM zhongyu Exp $
 */
public class Signer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Signer.class);

    public static final String SIGN_FIELD           = "sign";
    public static final String SIGN_TYPE_FIELD      = "sign_type";
    /**
     * 默认的签名算法，EasySDK统一固定使用RSA2签名算法（即SHA_256_WITH_RSA），但此参数依然需要用户指定以便用户感知，因为在开放平台接口签名配置界面中需要选择同样的算法
     */
    public static final String RSA2 = "RSA2";

    /**
     * RSA2对应的真实签名算法名称
     */
    public static final String SHA_256_WITH_RSA = "SHA256WithRSA";

    /**
     * RSA2对应的真实非对称加密算法名称
     */
    public static final String RSA = "RSA";
    /**
     * 默认字符集编码，EasySDK统一固定使用UTF-8编码，无需用户感知编码，用户面对的总是String而不是bytes
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static String getSignCheckContent(Map<String, String> params) {
        if (params == null) {
            return null;
        }

        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            content.append(i == 0 ? "" : "&").append(key).append("=").append(value);
        }
        return content.toString();
    }

    /**
     * 验证签名
     *
     * @param content      待验签的内容
     * @param sign         签名值的Base64串
     * @param publicKeyPem 支付宝公钥
     * @return true：验证成功；false：验证失败
     */
    public static boolean verify(String content, String sign, String publicKeyPem) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            byte[] encodedKey = publicKeyPem.getBytes();
            encodedKey = Base64.getDecoder().decode(encodedKey);
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            Signature signature = Signature.getInstance(SHA_256_WITH_RSA);
            signature.initVerify(publicKey);
            signature.update(content.getBytes(DEFAULT_CHARSET));
            return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
        } catch (Exception e) {
            String errorMessage = "验签遭遇异常，content=" + content + " sign=" + sign +
                    " publicKey=" + publicKeyPem + " reason=" + e.getMessage();
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * 计算签名
     *
     * @param content       待签名的内容
     * @param privateKeyPem 私钥
     * @return 签名值的Base64串
     */
    public static String sign(String content, String privateKeyPem) {
        try {
            byte[] encodedKey = privateKeyPem.getBytes();
            encodedKey = Base64.getDecoder().decode(encodedKey);
            PrivateKey privateKey = KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(encodedKey));

            Signature signature = Signature.getInstance(SHA_256_WITH_RSA);
            signature.initSign(privateKey);
            signature.update(content.getBytes(DEFAULT_CHARSET));
            byte[] signed = signature.sign();
            return new String(Base64.getEncoder().encode(signed));
        } catch (Exception e) {
            String errorMessage = "签名遭遇异常，content=" + content + " privateKeySize=" + privateKeyPem.length() + " reason=" + e.getMessage();
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * 对参数集合进行验签
     *
     * @param parameters 参数集合
     * @param publicKey  支付宝公钥
     * @return true：验证成功；false：验证失败
     */
    public static boolean verifyParams(Map<String, String> parameters, String publicKey) {
        String sign = parameters.get(SIGN_FIELD);
        parameters.remove(SIGN_FIELD);
        parameters.remove(SIGN_TYPE_FIELD);

        String content = getSignCheckContent(parameters);

        return verify(content, sign, publicKey);
    }
}