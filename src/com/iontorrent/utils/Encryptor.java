/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public class Encryptor {

    public static final String DEFAULT_TYPE = "DES/ECB/PKCS5Padding";
    public static final String DEFAULT_KEY = "testkey";
    private static byte[] iv = {0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d};

    private static byte[] encrypt(byte[] inpBytes,
            SecretKey key, String xform) throws Exception {
        Cipher cipher = Cipher.getInstance(xform);
        IvParameterSpec ips = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ips);
        return cipher.doFinal(inpBytes);
    }

    private static byte[] decrypt(byte[] inpBytes,
            SecretKey key, String xform) throws Exception {
        Cipher cipher = Cipher.getInstance(xform);
        IvParameterSpec ips = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ips);
        return cipher.doFinal(inpBytes);
    }

    public static String encrypt(String data) {
        return encrypt(data, DEFAULT_KEY, DEFAULT_TYPE);
    }

    public static String decrypt(String data) {
        return decrypt(data, DEFAULT_KEY, DEFAULT_TYPE);
    }

    public static String decrypt(String data, String key) {
        return decrypt(data, key, DEFAULT_TYPE);
    }

    public static String encrypt(String data, String key) {
        return encrypt(data, key, DEFAULT_TYPE);
    }

    public static String encrypt(String data, String key, String type) {

        SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "DES");
        byte[] dataBytes = data.getBytes();
        String res = null;
        try {
            res = new String(encrypt(dataBytes, skey, type));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return res;
    }

    public static String decrypt(String data, String key, String type) {

        SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "DES");
        byte[] dataBytes = data.getBytes();
        String res = null;
        try {
            res = new String(decrypt(dataBytes, skey, type));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return res;
    }

    public static void main(String[] unused) throws Exception {
        String test = "This is a test string";
        String enc = encrypt(test);
        String dec = decrypt(enc);

        boolean expected = test.equals(dec);
        System.out.println("Test " + (expected ? "SUCCEEDED!" : "FAILED!"));
    }
}
