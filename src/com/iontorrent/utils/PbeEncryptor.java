/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
/**
 * Utility class to Encrypt and Decrypt texts.
 * Uses ir.igv.encrytion.algo and ir.igv.encrytion.key from $IONREPORTERMANAGERROOT/server/server.properties
 * 
 * @author chetan_gole
 */
public class PbeEncryptor {

	private static final Log LOGGER = LogFactory.getLog(Encryptor.class);
	private  StandardPBEStringEncryptor encryptor;
	
	private static final String DEFAULT_KEY = "somekey";
	public static final String DEFAULT_ALGORITHM = "PBEWithMD5AndDES"; 	
	            
        private static final String STRING_OUTPUT_TYPE = "hexadecimal"; //To get URL friendly encrypted text
	
	public static String getAlgorithm() {
		return DEFAULT_ALGORITHM;
	}
	
        public PbeEncryptor() {
            this(DEFAULT_ALGORITHM);
        }
        public PbeEncryptor(String alg) {
            
            encryptor = new StandardPBEStringEncryptor();   
            encryptor.setAlgorithm(alg);
            encryptor.setStringOutputType(STRING_OUTPUT_TYPE);        
        }
	public String encrypt(String textToEncrypt) {	
		encryptor.setPassword(DEFAULT_KEY);               
		return encryptor.encrypt(textToEncrypt);
	}

	public String decrypt(String textToDecrypt) {
		encryptor.setPassword(DEFAULT_KEY);                
		return encryptor.decrypt(textToDecrypt);
	}
	
	public String encrypt(String textToEncrypt, String encryptionKey) {	
		encryptor.setPassword(encryptionKey);
		return encryptor.encrypt(textToEncrypt);
	}

	public  String decrypt(String textToDecrypt, String encryptionKey) {
		encryptor.setPassword(encryptionKey);
		return encryptor.decrypt(textToDecrypt);
	}
	public static void main(String[] args) {		
		String textToEncrypt = "WyFVUoZaJHNebRx9gqFAhWAz9lMCQLBx4qjk1sMC2q1y0foX7brVycrh0Fb7mlwvPwxrIuDUDlFvN/sRoHKu/kIYSlIOdd6zsKmsKsWbUOU=";
                String enc = "7EAEC94EEB7C17D2CDC40A069C902E7197311B38465D32FE040899149772FAB322CBE09AD83BE2206D72C842E07A5EFB25C0BC4F33D8DE94606A39C4523D3EC1C628742C6710BD78A8596526541E79FAF44C4F4FC3E00A99DBF116230F4733E204D2F419D694247B3322F0270A6E08065B132DDECC3A3269";
                String key = "IGVKEY123";
		PbeEncryptor en = new PbeEncryptor();
		String encryptedString = en.encrypt(textToEncrypt, key);
		String decryptedString = en.decrypt(encryptedString, key);
		
		p(encryptedString);
		p(decryptedString);
                p("Same? "+enc.equals(encryptedString));
                
                
                
	}
        
        private static void p(String s ) {
            System.out.println("PbeEncryptor: "+s);
        }

}
