package com.example.imetlin.sonik.notification.service;

import java.net.URLEncoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

/**
 * AesUtils.java encrypts data
 */

public class AesUtils {

	// Constants ---------------------------------------------------------------------------------------------- Constants
	private static final String TAG_NAME = "NotificationService"; // logger

	private static String IV = "Vic6302222036ciV";
	
	public static String encryptionKey = "DE11C6E6A348D537";
	
  private static String ALGORYTM = "AES/CBC/PKCS5Padding"; 

	// Instance Variables ---------------------------------------------------------------------------- Instance Variables

	// Constructors ---------------------------------------------------------------------------------------- Constructors

	// Public Methods  ------------------------------------------------------------------------------------ Public Methods
	
	public static String encrypt(String plainText, String encryptionKey) throws Exception {
		byte[] cipherText = encryptBase64(plainText, encryptionKey);		
		return Base64.encodeToString(cipherText, Base64.DEFAULT);
	}

	public static byte[] encryptBase64(String plainText, String encryptionKey) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORYTM);
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
		return cipher.doFinal(plainText.getBytes("UTF-8"));
	}

	public static String decrypt(String base64, String encryptionKey) throws Exception {
		return decryptBase64(Base64.decode(base64, Base64.DEFAULT), encryptionKey);
	}

	public static String decryptBase64(byte[] cipherText, String encryptionKey) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORYTM);
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
		return new String(cipher.doFinal(cipherText), "UTF-8");
	}
	
	//to base 64
	public static String b64(String str) {
		return str != null && str.equals("") == false ? Base64.encodeToString(str.getBytes(), Base64.DEFAULT) : "";
	}
	
	//encode string
	public static String e(String str) {
		try {
			return str != null && str.equals("") == false ? URLEncoder.encode(str, "UTF-8") : "";
		} catch (Exception e) {	
			return "";
		}
	}
}