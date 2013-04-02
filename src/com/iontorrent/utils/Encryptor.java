/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.utils;

import java.util.logging.Logger;

/**
 * 
 * @author Chantal Roth
 */
public class Encryptor {
	 
	public static String decrypt(String algo, String encrypted, String key)  {
		//DesEncryptor enc = new DesEncryptor(key);
                PbeEncryptor enc = new PbeEncryptor(algo);
		return enc.decrypt(encrypted, key);
	}

        public static String getDefaultAlgorithm() {
            return PbeEncryptor.DEFAULT_ALGORITHM;
        }
	public static String encrypt(String algo, String data, String pass) {		
                PbeEncryptor enc = new PbeEncryptor(algo);
		return enc.encrypt(data, pass);
	}


	private static void p(String msg) {
		Logger.getLogger("Encryptor").info(msg);
		System.out.println("Encryptor: " + msg);
	}

	private static void err(String msg) {
		Logger.getLogger("Encryptor").warning(msg);
		System.out.println("Encryptor!: " + msg);
	}
}
