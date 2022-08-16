package com.common.core.security;

import org.apache.commons.net.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 加密解密工具
 *
 * @author Quyuzhong
 * @version 1.0.0
 */
public class EncryptUtil {
	private static final String HEX_NUMS_STR = "0123456789ABCDEF";

	/**
	 * encrypt, 加密
	 *
	 * @param password
	 * @return
	 */
	public static String encrypt(String password) {
		try {
			return encrypStr(password);
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 加密，错误返回null
	 *
	 * @param password
	 * @return
	 */
	public static String encryptAllowNull(String password) {
		try {
			return encrypStr(password);
		} catch (Exception e) {
			return null;
		}
	}

	private static String encrypStr(String password) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] raw = HEX_NUMS_STR.getBytes(StandardCharsets.UTF_8);
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(password.getBytes());
		return byte2hex(encrypted).toLowerCase();
	}

	/**
	 * decrypt, 解密
	 *
	 * @param password
	 * @return
	 */
	public static String decrypt(String password) {
		try {
			return decryptStr(password);
		} catch (Exception e) {
			return "";
		}
	}

	private static String decryptStr(String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] raw = HEX_NUMS_STR.getBytes("ASCII");
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] encrypted1 = hex2byte(password);
		byte[] original = cipher.doFinal(encrypted1);
		return new String(original);
	}

	/**
	 * decrypt, 解密，错误返回null
	 *
	 * @param password
	 * @return
	 */
	public static String decryptAllowNull(String password) {
		try {
			return decryptStr(password);
		} catch (Exception e) {
			return null;
		}
	}

	private static String byte2hex(byte[] b) {
		StringBuilder hs = new StringBuilder();
		String stmp = "";
		for (byte aB : b) {
			stmp = (Integer.toHexString(aB & 0XFF));
			if (stmp.length() == 1) {
				hs.append("0").append(stmp);
			} else {
				hs.append(stmp);
			}
		}
		return hs.toString().toUpperCase();
	}

	private static byte[] hex2byte(String strhex) {
		if (strhex == null) {
			return null;
		}
		int l = strhex.length();
		if (l % 2 == 1) {
			return null;
		}
		byte[] b = new byte[l / 2];
		for (int i = 0; i != l / 2; i++) {
			b[i] = (byte) Integer.parseInt(strhex.substring(i * 2, i * 2 + 2),
					16);
		}
		return b;
	}


	public static String decryptByPKCS5Padding(String str) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec
					(HEX_NUMS_STR.getBytes(StandardCharsets.UTF_8), "AES"));
			//先用base64解密
			byte[] encrypted1 = new Base64().decode(str);
			byte[] original = cipher.doFinal(encrypted1);
			return new String(original, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return "";
		}
	}

	public static String encryptByPKCS5Padding(String str) {
		try {
			//"算法/模式/补码方式"
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE,
					new SecretKeySpec(HEX_NUMS_STR.getBytes(StandardCharsets.UTF_8), "AES"));
			byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
			//此处使用BASE64做转码功能，同时能起到2次加密的作用。
			return new Base64().encodeToString(encrypted);
		} catch (Exception e) {
			return "";
		}
	}

	public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {


//		System.out.println("1" + encrypt(""));
//        System.out.println("2" + encrypt(null));
//        System.out.println(encrypt("autek"));
//        System.out.println(encrypt("webuser123"));
		System.out.println("A2PDQ2WMYTIYVC");
		System.out.println("token:    " + decrypt("33ea58175f23c1f05928d50e14a9181e814d4411463eed2403dd15f81ec9d25b22186f9f1bd93440a575352f91c2a065"));
		System.out.println("asscess:    " + decrypt("bc77fadec1cea8a79a989d8fde61f5ab63852e8ce221c9447b645046db789429"));
		System.out.println("scretKey:    " + decrypt("9fc706cfa8fa242f92f996e3554faf18cd2c5797f81c9524948dc44aabf30048d67de733b55520367a3fdfaf3e533517"));
	}


}
