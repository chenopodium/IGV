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
	 
	public static String decrypt(String encrypted, String key)  {
		DesEncryptor enc = new DesEncryptor(key);
		return enc.decrypt(encrypted);
	}


	public static String encrypt(String data, String pass) {
		DesEncryptor enc = new DesEncryptor(pass);
		return enc.encrypt(data);
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
