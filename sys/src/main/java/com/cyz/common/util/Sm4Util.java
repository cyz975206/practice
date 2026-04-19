package com.cyz.common.util;

import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;

/**
 * SM4 ECB/PKCS7Padding 加解密工具类
 */
public class Sm4Util {

    private static final int KEY_SIZE = 16;

    /**
     * 将密钥补齐到16字节（不足补零）
     */
    private static byte[] normalizeKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[KEY_SIZE];
        System.arraycopy(keyBytes, 0, normalized, 0, Math.min(keyBytes.length, KEY_SIZE));
        return normalized;
    }

    /**
     * 判断文本是否为 SM4 加密后的密文（尝试解密，成功则认为是密文）
     */
    public static boolean isEncrypted(String text, String key) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            decrypt(text, key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * SM4 ECB加密，返回Base64编码的密文
     */
    public static String encrypt(String plaintext, String key) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        try {
            byte[] keyBytes = normalizeKey(key);
            byte[] inputBytes = plaintext.getBytes(StandardCharsets.UTF_8);

            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new SM4Engine());
            cipher.init(true, new KeyParameter(keyBytes));

            byte[] output = new byte[cipher.getOutputSize(inputBytes.length)];
            int len = cipher.processBytes(inputBytes, 0, inputBytes.length, output, 0);
            len += cipher.doFinal(output, len);

            byte[] result = new byte[len];
            System.arraycopy(output, 0, result, 0, len);
            return Base64.toBase64String(result);
        } catch (Exception e) {
            throw new RuntimeException("SM4 encryption failed", e);
        }
    }

    /**
     * SM4 ECB解密，输入Base64编码的密文，返回明文
     */
    public static String decrypt(String ciphertext, String key) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        try {
            byte[] keyBytes = normalizeKey(key);
            byte[] inputBytes = Base64.decode(ciphertext);

            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new SM4Engine());
            cipher.init(false, new KeyParameter(keyBytes));

            byte[] output = new byte[cipher.getOutputSize(inputBytes.length)];
            int len = cipher.processBytes(inputBytes, 0, inputBytes.length, output, 0);
            len += cipher.doFinal(output, len);

            return new String(output, 0, len, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4 decryption failed", e);
        }
    }
}
