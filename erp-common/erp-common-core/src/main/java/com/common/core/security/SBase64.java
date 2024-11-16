package com.common.core.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Base64加密解密类
 *
 * @author Quyuzhong
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class SBase64 {

	/**
	 * 字符串转Base64
	 *
	 * @param toConvert
	 * @return
	 */
	public static String stringToBase64(String toConvert) {
		byte[] data = toConvert.getBytes();
		return Base64.encodeBase64String(data);
	}

	/**
	 * base64转字符串
	 *
	 * @param base64Str base64字符串
	 * @return
	 */
	public static String base64ToString(String base64Str) {
		try {
			// Base64解码
			byte[] b = Base64.decodeBase64(base64Str);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}

			String result = new String(b, "utf-8");
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * base64转字节流
	 *
	 * @param base64Str base64字符串
	 * @return
	 */
	public static byte[] base64ToByte(String base64Str) {
		byte[] b;
		try {
			// Base64解码
			b = Base64.decodeBase64(base64Str);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}

			return b;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 文件转Base64字符串
	 *
	 * @param fileName 文件名
	 * @return Base64字符串
	 * @throws IOException 抛出异常
	 */
	public static String fileToBase64(String fileName) throws IOException {
		InputStream in = null;
		byte[] data = null;
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				throw new IOException("fileName:" + fileName + ",文件不存在");
			}

			in = new FileInputStream(fileName);
			data = new byte[in.available()];
			in.read(data);
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(in);
		}
		// 对字节数组Base64编码
		return Base64.encodeBase64String(data);// 返回Base64编码过的字节数组字符串
	}

	/**
	 * Base64字符串转文件并写入
	 *
	 * @param base64Str Base64字符串
	 * @param fileName  文件名
	 * @throws Exception
	 * @return true:转换成功,false:转换失败
	 */
	public static Boolean base64ToFile(String base64Str, String fileName) throws Exception {
		if (base64Str == null) { // 图像数据为空
			return false;
		}
		File file = new File(fileName);
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

		} catch (Exception e) {
			throw new Exception("创建目录" + file.getParentFile() + "失败." + e.getMessage());
		}

		OutputStream out = null;
		try {
			// Base64解码
			byte[] b = Base64.decodeBase64(base64Str);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}

			out = new FileOutputStream(fileName);
			out.write(b);
			out.flush();
			return true;
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void main(String[] args) throws IOException {
		String primstr = fileToBase64("C:/Users/Administrator/Desktop/itext测试/iTextAsian.zip");

		try {
			base64ToFile(primstr, "C:/Users/Administrator/Desktop/itext测试/新建文本文档1.zip");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
