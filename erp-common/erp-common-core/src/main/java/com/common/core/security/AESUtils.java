package com.common.core.security;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 采用AES加密算法进行加密与解密
 *
 * @author QuYuZhong
 * @version 1.0.0
 */
public class AESUtils {
	static final String DEFAULT_KEY = "ERP_0123456789ABCDEF";

	/**
	 * AES加密算法
	 *
	 * @param content 待加密内容
	 * @return 返回加密后的字符串
	 */
	public static String encode(String content) {
		return encode(DEFAULT_KEY, content);
	}

	/**
	 * AES加密算法
	 *
	 * @param encodeKey 加密Key
	 * @param content   待加密内容
	 * @return 返回加密后的字符串
	 */
	public static String encode(String encodeKey, String content) {
		try {
			if (encodeKey == null || encodeKey.length() < 1) {
				encodeKey = DEFAULT_KEY;
			}
			//1.构造秘钥生成器，指定秘钥算法为AES算法，这里不区分大小写
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

			//2.根据encodeRules初始化秘钥生成器，生成一个128位随机源
			//这里分为多种的方式生成秘钥生成器，这里我使用的是：使用用户提供的随机源初始化此密钥生成器，使其具有确定的密钥大小。
			keyGenerator.init(128, new SecureRandom(encodeKey.getBytes()));

			//3.生成原始对称秘钥，这是一个接口类，
			SecretKey secretKey = keyGenerator.generateKey();

			//4.获得原始对称秘钥的字节数组
			byte[] secreKeyByte = secretKey.getEncoded();

			//5.根据字节数组生成AES秘钥
			SecretKey key = new SecretKeySpec(secreKeyByte, "AES");

			//6.根据指定算法AES制成密码器
			Cipher cipher = Cipher.getInstance("AES");

			//7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的key
			cipher.init(Cipher.ENCRYPT_MODE, key);

			//8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
			byte[] contentByte = content.getBytes("UTF-8");

			//9.使用密码器进行加密操作
			byte[] byte_AES = cipher.doFinal(contentByte);

			//10.将加密后的字符数组转成Base64的字符格式
			String key_content_base64 = Base64.getEncoder().encodeToString(byte_AES);
			return key_content_base64;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException
				| IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException(String.format("AES加密失败:%s", (e.getMessage() == null ? e.toString() : e.getMessage())));
		}
	}

	/**
	 * AES解密算法
	 *
	 * @param content 待解密内容
	 * @return 返回解密后的字符串
	 */
	public static String decode(String content) {
		return decode(DEFAULT_KEY, content);
	}

	/**
	 * AES解密算法
	 *
	 * @param decodeKey 解密Key
	 * @param content   待解密内容
	 * @return 返回解密后的字符串
	 */
	public static String decode(String decodeKey, String content) {
		try {
			if (decodeKey == null || decodeKey.length() < 1) {
				decodeKey = DEFAULT_KEY;
			}
			//1.构造秘钥生成器
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

			//2.初始化秘钥生成器
			keyGenerator.init(128, new SecureRandom(decodeKey.getBytes()));

			//3.生成原始对称秘钥
			SecretKey secretKey = keyGenerator.generateKey();

			//4.将原始对称秘钥转成byte数组
			byte[] secretKeyByte = secretKey.getEncoded();

			//5.根据指定算法AES和数组生成AES秘钥
			SecretKey key = new SecretKeySpec(secretKeyByte, "AES");

			//6.指定AES算法生成密码器      Cipher
			Cipher cipher = Cipher.getInstance("AES");

			//7.初始化密码器
			cipher.init(Cipher.DECRYPT_MODE, key);

			//8.获取Base64的字符串，进行Base64反编译
			byte[] base_Decrypt = Base64.getDecoder().decode(content);

			//9.使用密码器进行解密
			byte[] decrypt_byte = cipher.doFinal(base_Decrypt);

			//10.把数组转成string字符串
			String str = new String(decrypt_byte, "UTF-8");
			return str;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException e) {

			throw new RuntimeException(String.format("AES解密失败:%s", (e.getMessage() == null ? e.toString() : e.getMessage())));
		}
	}

	/**
	 * 测试方法
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		String str = encode("45644", "我就是我的！123544");
		System.out.println(str);
		String str1 = decode("45644", str);
		System.out.println(str1);
	}

}
