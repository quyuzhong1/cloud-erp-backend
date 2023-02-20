//package com.common.core.file;
//
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.io.IOUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;
//import java.util.zip.ZipOutputStream;
//
//
///**
// * 对字符串进行加解密和加解压
// *
// * @author ***
// */
//@SuppressWarnings("restriction")
//public class ZipBaseUtils {
//
//	public static void main(String[] args) {
//		String primstr = "UEsDBC0AAAAIAAaPJkfS5clx//////////8JABQARW1wbG95ZWVzAQAQAM4BAAAAAAAAmgAAAAAAAACLrlZyKS2pVLJSetqz+/mU+Uo6Sp4uSlamhjpKXvmZeS6JJalAuRh9EEPD0MjCzMzEAAK0DSwMDDRj9IE6fFITy1IhSvNKc3J0lPwSc0Hanu5Z8GRHJ1BBZrFjSkpqipJVWmJOcWqtDg5Ljahh6bO5fU9nzybeUmMcllqaGJhYEGvp877uJ7um4LO0uBhmoQk1fJmWloJpWywAUEsBAjMALQAAAAgABo8mR9LlyXH//////////wkAFAAAAAAAAAAAAAAAAAAAAEVtcGxveWVlcwEAEADOAQAAAAAAAJoAAAAAAAAAUEsFBgAAAAABAAEASwAAANUAAAAAAA==";
//		System.out.println(unzipString(primstr));
//	}
//
//
//	private static Logger log = LoggerFactory.getLogger(ZipBaseUtils.class);
//
//	/**
//	 * 将字符串压缩后Base64
//	 *
//	 * @param primStr 待加压加密函数
//	 * @return
//	 */
//	public static String zipString(String primStr) {
//		if (primStr == null || primStr.length() == 0) {
//			return primStr;
//		}
//		ByteArrayOutputStream out = null;
//		ZipOutputStream zout = null;
//		try {
//			out = new ByteArrayOutputStream();
//			zout = new ZipOutputStream(out);
//			zout.putNextEntry(new ZipEntry("0"));
//			zout.write(primStr.getBytes("UTF-8"));
//			zout.closeEntry();
//			return Base64.encodeBase64String(out.toByteArray());
//		} catch (IOException e) {
//			log.error("对字符串进行加压加密操作失败：", e);
//			return null;
//		} finally {
//			IOUtils.closeQuietly(zout);
//		}
//	}
//
//	/**
//	 * 将压缩并Base64后的字符串进行解密解压
//	 *
//	 * @param compressedStr 待解密解压字符串
//	 * @return
//	 */
//	public static final String unzipString(String compressedStr) {
//		if (compressedStr == null) {
//			return null;
//		}
//		ByteArrayOutputStream out = null;
//		ByteArrayInputStream in = null;
//		ZipInputStream zin = null;
//		String decompressed = null;
//		try {
//			byte[] compressed = Base64.decodeBase64(compressedStr);
//			out = new ByteArrayOutputStream();
//			in = new ByteArrayInputStream(compressed);
//			zin = new ZipInputStream(in);
//			zin.getNextEntry();
//			byte[] buffer = new byte[1024];
//			int offset = -1;
//			while ((offset = zin.read(buffer)) != -1) {
//				out.write(buffer, 0, offset);
//			}
//			decompressed = out.toString("UTF-8");
//		} catch (IOException e) {
//			log.error("对字符串进行解密解压操作失败：", e);
//			decompressed = null;
//		} finally {
//			IOUtils.closeQuietly(zin, in, out);
//		}
//		return decompressed;
//	}
//}