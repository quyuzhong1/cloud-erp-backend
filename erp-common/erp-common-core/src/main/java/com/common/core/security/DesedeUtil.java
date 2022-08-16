package com.common.core.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.security.Key;

/**
 *
 */

/**
 * DES : Data Encryption Standard 数据加密标准
 *
 * <p>DESede（TripleDES、3DES）对称加密算法</p>
 *
 * 设Ek()和Dk()代表DES算法的加密和解密过程，K代表DES算法使用的密钥，P代表明文，C代表密文
 * 3DES加密过程为：C=Ek3(Dk2(Ek1(P)))
 * 3DES解密过程为：P=Dk1(EK2(Dk3(C)))
 *
 * @author Quyuzhong
 * @version 1.0
 * @since 1.0
 */
public class DesedeUtil {

	/**
	 * 密钥生成算法
	 */
	public static final String KEY_ALGORITHM = "DESede";

	/**
	 * 加密解密算法
	 * 加密/解密算法/工作模式/填充方式
	 */
	public static final String CIPHER_ALGORITHM = "DESede/ECB/NoPadding";

	/**
	 * 生成密钥
	 * Java 6 仅支持112位和168位密钥
	 * key最大为24字节，即192位，其中包含24位奇偶校验位，故密钥长度为168位。
	 * @return 二进制密钥
	 */
	public static byte[] genkey() {
		try {
			KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
			SecretKey secretKey = kg.generateKey();
			return secretKey.getEncoded();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 加密
	 * @return
	 */
	public static byte[] encrypt(byte[] data, byte[] key) {
		try {
			Key k = toKey(key);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, k);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 解密
	 * @return
	 */
	public static byte[] decrypt(byte[] data, byte[] key) {
		try {
			Key k = toKey(key);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, k);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 转换密钥
	 * java只支持24字节密钥，如果key为16字节则复制前8字节到后面凑够24字节（即Ek1等于Ek3）
	 *
	 * @param key 二进制密钥
	 * @return 密钥
	 * @throws Exception
	 */
	private static Key toKey(byte[] key) {
		try {
			byte[] nkey = null;
			if (key.length == 16) {
				nkey = new byte[key.length + 8];
				System.arraycopy(key, 0, nkey, 0, key.length);
				System.arraycopy(key, 0, nkey, 16, 8);
			} else {
				nkey = key;
			}

			DESedeKeySpec dks = new DESedeKeySpec(nkey);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
			return keyFactory.generateSecret(dks);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
