package com.iontorrent.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.logging.Logger;

public class DesEncryptor {
	Cipher ecipher;
	Cipher dcipher;
	// 8-byte Salt
	byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
			(byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03 };
	// Iteration count
	int iterationCount = 19;

	public DesEncryptor(String passPhrase) {
		try {
			// Create the key
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt,
					iterationCount);
			SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
					.generateSecret(keySpec);
			ecipher = Cipher.getInstance(key.getAlgorithm());
			dcipher = Cipher.getInstance(key.getAlgorithm());
			// Prepare the parameter to the ciphers
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt,
					iterationCount);
			// Create the ciphers
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		} catch (Exception e) {
			err(ErrorHandler.getString(e));
		}
	}

	public String encrypt(String str) {
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");
			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);
			// Encode bytes to base64 to get a string
			return new sun.misc.BASE64Encoder().encode(enc);
		} catch (Exception e) {
			err(ErrorHandler.getString(e));
		}
		return null;
	}

	public String decrypt(String str) {
		if (str == null)
			return null;
		try {
			// Decode base64 to get bytes
			byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);
			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (Exception e) {
			err(ErrorHandler.getString(e));
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			// Create encrypter/decrypter class
			DesEncryptor encrypter = new DesEncryptor("My Private Key");
			// Encrypt
			String encrypted = encrypter.encrypt("bi20009");
			System.out.println(encrypted);
			String decrypted = encrypter.decrypt(encrypted);
			System.out.println(decrypted);
		} catch (Exception e) {
		}
	}
	private static void p(String msg) {
		Logger.getLogger("DesEncryptor").info(msg);
		System.out.println("DesEncryptor: " + msg);
	}

	private static void err(String msg) {
		Logger.getLogger("DesEncryptor").warning(msg);
		System.out.println("DesEncryptor!: " + msg);
	}

}