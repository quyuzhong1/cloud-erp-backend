///**
// * FileUtil.java
// * 版权所有(C) 2017 Cloud ERP
// * 创建:Quyuzhong 2017-1-12
// */
//package com.common.core.file;
//
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.io.IOUtils;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//
//import java.io.*;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.nio.channels.FileChannel.MapMode;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 文件处理类
// *
// * @author Quyuzhong
// * @version 1.0
// */
//public class FileUtils {
//
//
//	public static String concatSp(String... args) {
//		if (args == null) {
//			return null;
//		}
//		StringBuilder sb = new StringBuilder();
//		for (String arg : args) {
//			sb.append(arg).append("/");
//		}
//		sb.delete(sb.length() - 1, sb.length());
//		return sb.toString();
//	}
//
//
//	/**
//	 * 将字符串内容写入文件中
//	 *
//	 * @param content    待写入内容
//	 * @param outputFile 输出文件名
//	 */
//	public static void writeContentToFile(String content, String outputFile) {
//		FileOutputStream fos = null;
//		Writer os = null;
//		try {
//			fos = new FileOutputStream(new File(outputFile));
//			os = new OutputStreamWriter(fos, "GBK");
//			os.write(content);
//			os.flush();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		} finally {
//			IOUtils.closeQuietly(fos, os);
//		}
//	}
//
//	/**
//	 * 读取Txt文件中的文件输出内容
//	 *
//	 * @param file 源文件
//	 * @return 输出文件内容
//	 */
//	public static String getContentFromFile(File file) {
//		StringBuilder result = new StringBuilder();
//		BufferedReader br = null;
//		try {
//			br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
//			String s = null;
//			while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
//				result.append(System.lineSeparator()).append(s);
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			IOUtils.closeQuietly(br);
//		}
//		return result.toString();
//	}
//
//	/**
//	 * 将文件转化成byte[]
//	 *
//	 * @param filePath
//	 * @return
//	 */
//	public static byte[] getBytesFromFile(String filePath) {
//		if (filePath.equals("")) {
//			System.out.println("没有信息......");
//			return null;
//		}
//		byte[] buffer = null;
//		FileInputStream fis = null;
//		ByteArrayOutputStream bos = null;
//		try {
//			File file = new File(filePath);
//			fis = new FileInputStream(file);
//			bos = new ByteArrayOutputStream(1000);
//			byte[] b = new byte[1000];
//			int n;
//			while ((n = fis.read(b)) != -1) {
//				bos.write(b, 0, n);
//			}
//			buffer = bos.toByteArray();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		} finally {
//			IOUtils.closeQuietly(fis, bos);
//		}
//		return buffer;
//	}
//
//	/**
//	 * 新建目录.
//	 *
//	 * @param path 文件路径
//	 * @throws Exception
//	 */
//	public static void createDirectory(String path) throws Exception {
//		try {
//			// 获得文件对象
//			File f = new File(path);
//			if (!f.exists()) {
//				// 如果路径不存在,则创建
//				f.mkdirs();
//			}
//		} catch (Exception e) {
//			throw new Exception("创建目录错误.path=" + path + "." + e.getMessage());
//		}
//	}
//
//	/**
//	 * 新建文件.
//	 *
//	 * @param path 文件路径
//	 * @throws Exception
//	 */
//	public static void createFile(String path) throws Exception {
//		if (path == null || path.length() < 1) {
//			return;
//		}
//		try {
//			// 获得文件对象
//			File f = new File(path);
//			if (f.exists()) {
//				return;
//			}
//			// 如果路径不存在,则创建
//			if (!f.getParentFile().exists()) {
//				f.getParentFile().mkdirs();
//			}
//			f.createNewFile();
//		} catch (Exception e) {
//			throw e;
//		}
//	}
//
//	/**
//	 * 保存文件(文件不存在会自动创建).
//	 *
//	 * @param path    文件路径
//	 * @param content 文件内容
//	 */
//	public static void saveFile(String path, String content) {
//		try {
//			saveFile(path, content, "UTF-8");
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	/**
//	 * 保存文件(文件不存在会自动创建).
//	 *
//	 * @param fileName 文件
//	 * @param content  文件内容
//	 * @param encoding 编码(UTF-8/gb2312/...)
//	 * @throws Exception
//	 */
//	public static void saveFile(String fileName, String content, String encoding) throws Exception {
//		saveFile(fileName, content, encoding, false);
//	}
//
//
//	public static String path = "D:/data/testFile.csv";
//	private static volatile Map<String, Long> timeMap = new HashMap<>();
//
//	/**
//	 * 自测试，写入文件，默认路径
//	 * 只允许单线程
//	 *
//	 * @param content
//	 */
//	public static synchronized void calTime2File(String key, String content) {
////        Long last = timeMap.get(key);
////        long cost = 0;
////        if (last == null) {
////            timeMap.put(key, System.currentTimeMillis());
////            return;
////        } else {
////            cost = System.currentTimeMillis() - last;
////            timeMap.remove(key);
////        }
////
////        try {
////            FileUtils.saveFile(path, key + "," + content + "," + cost + "\n", "UTF-8", true);
////        } catch (Exception e) {
////            throw new RuntimeException(e);
////        }
//	}
//
//	public static synchronized void calTime2File2(String key, String content) {
//		Long last = timeMap.get(key);
//		long cost = 0;
//		if (last == null) {
//			timeMap.put(key, System.currentTimeMillis());
//			return;
//		} else {
//			cost = System.currentTimeMillis() - last;
//			timeMap.remove(key);
//		}
//
//		try {
//			FileUtils.saveFile(path, key + "," + content + "," + cost + "\n", "UTF-8", true);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	/**
//	 * 保存文件(文件不存在会自动创建).追加
//	 *
//	 * @param fileName 文件
//	 * @param content  文件内容
//	 * @param encoding 编码(UTF-8/gb2312/...)
//	 * @throws Exception
//	 */
//	public static void saveFile(String fileName, String content, String encoding, Boolean isAppend) throws Exception {
//		FileOutputStream fileOutputStream = null;
//		BufferedOutputStream bw = null;
//		try {
//			// 获得文件对象
//			File tmp = new File(fileName);
//			createDirectory(tmp.getParent());
//			// 开始保存文件
//			fileOutputStream = new FileOutputStream(fileName, isAppend);
//			bw = new BufferedOutputStream(fileOutputStream);
//			if (encoding == null) {
//				bw.write(content.getBytes());
//			} else {
//				bw.write(content.getBytes(encoding));
//			}
//		} finally {
//			IOUtils.closeQuietly(bw, fileOutputStream);
//		}
//	}
//
//
//	/**
//	 * 删除文件夹
//	 *
//	 * @param file
//	 */
//	public static void deleteFile(File file) {
//		if (file.exists()) { // 判断文件是否存在
//			if (file.isFile()) { // 判断是否是文件
//				file.delete();
//			} else if (file.isDirectory()) { // 否则如果它是一个目录
//				File[] files = file.listFiles(); // 声明目录下所有的文件 files[];
//				assert files != null;
//				for (File value : files) { // 遍历目录下所有的文件
//					deleteFile(value); // 把每个文件 用这个方法进行迭代
//				}
//			}
//			file.delete();
//		} else {
//			System.out.println("所删除的文件不存在！" + '\n');
//		}
//	}
//
//	/**
//	 * 移动文件/文件夹
//	 *
//	 * @param fromFilePath 源文件路径或文件夹
//	 * @param toPath       目标文件路径
//	 */
//	public static void moveFile(String fromFilePath, String toPath) {
//		File fromFile = new File(fromFilePath);
//		File toFolder = new File(toPath);
//		if (!toFolder.exists()) {
//			toFolder.mkdirs();
//		}
//		File newFile = new File(toFolder.getAbsoluteFile() + "\\" + fromFile.getName());
//		fromFile.renameTo(newFile);
//	}
//
//	/**
//	 * @param folderPath 文件路径 (只删除此路径的最末路径下所有文件和文件夹)
//	 */
//	public static void delFolder(String folderPath) {
//		try {
//			delAllFile(folderPath);    // 删除完里面所有内容
//			String filePath = folderPath;
//			filePath = filePath.toString();
//			File myFilePath = new File(filePath);
//			myFilePath.delete();        // 删除空文件夹
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	/**
//	 * 删除指定文件
//	 *
//	 * @param filePath
//	 */
//	public static void delCurFile(String filePath) {
//		try {
//			File myFilePath = new File(filePath);
//			myFilePath.delete();        // 删除文件
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	/**
//	 * 删除指定文件夹下所有文件
//	 *
//	 * @param path 文件夹完整绝对路径
//	 */
//	public static boolean delAllFile(String path) {
//		boolean flag = false;
//		File file = new File(path);
//		if (!file.exists()) {
//			return flag;
//		}
//		if (!file.isDirectory()) {
//			return flag;
//		}
//		String[] tempList = file.list();
//		if (tempList != null) {
//			File temp;
//			for (String s : tempList) {
//				if (path.endsWith(File.separator)) {
//					temp = new File(path + s);
//				} else {
//					temp = new File(path + File.separator + s);
//				}
//				if (temp.isFile()) {
//					temp.delete();
//				}
//				if (temp.isDirectory()) {
//					delAllFile(path + "/" + s);    // 先删除文件夹里面的文件
//					delFolder(path + "/" + s);    // 再删除空文件夹
//					flag = true;
//				}
//			}
//		}
//		return flag;
//	}
//
//	/**
//	 *      * 获取一个文件夹下的所有文件全路径
//	 *      * @param path
//	 *     
//	 */
//	public static List<String> getAllFileName(String path) {
//		List<String> outFileNames = new ArrayList<>();
//		File file = new File(path);
//		File[] files = file.listFiles();
//
//		for (File item : files) {
//			if (item.isDirectory()) {
//				outFileNames.addAll(getAllFileName(item.getAbsolutePath()));
//			} else {
//				outFileNames.add(item.getAbsolutePath());
//			}
//		}
//
//		return outFileNames;
//
//	}
//
//	/**
//	 * Mapped File way MappedByteBuffer 可以在处理大文件时，提升性能
//	 *
//	 * @param filePath
//	 * @return
//	 * @throws IOException
//	 */
//	public static byte[] toByteArray(String filePath) throws IOException {
//
//		FileChannel fc = null;
//		RandomAccessFile rf = null;
//		try {
//			rf = new RandomAccessFile(filePath, "r");
//			fc = rf.getChannel();
//			MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0,
//					fc.size()).load();
//			//System.out.println(byteBuffer.isLoaded());
//			byte[] result = new byte[(int) fc.size()];
//			if (byteBuffer.remaining() > 0) {
//				// System.out.println("remain");
//				byteBuffer.get(result, 0, byteBuffer.remaining());
//			}
//			return result;
//		} catch (IOException e) {
//			throw e;
//		} finally {
//			IOUtils.closeQuietly(rf, fc);
//		}
//	}
//
//	/**
//	 * 读取文件流
//	 *
//	 * @param file 文件
//	 * @return 文件流
//	 * @throws IOException 异常信息
//	 */
//	public static FileInputStream openInputStream(File file) throws IOException {
//		if (file.exists()) {
//			if (file.isDirectory()) {
//				throw new IOException("File '" + file + "' exists but is a directory");
//			}
//			if (!file.canRead()) {
//				throw new IOException("File '" + file + "' cannot be read");
//			}
//		} else {
//			throw new FileNotFoundException("File '" + file + "' does not exist");
//		}
//		return new FileInputStream(file);
//	}
//
//	/**
//	 * 读取文件
//	 *
//	 * @param file
//	 * @return
//	 * @throws IOException
//	 */
//	public static byte[] readFileToByteArray(File file) throws IOException {
//		InputStream in = null;
//		try {
//			in = openInputStream(file);
//			return IOUtils.toByteArray(in, file.length());
//		} finally {
//			IOUtils.closeQuietly(in);
//		}
//	}
//
//	/**
//	 * 生成图片long类型标识大小，转成B,KB,MB,GB,T
//	 *
//	 * @param size
//	 * @return
//	 */
//	public static String printSize(long size) {
//		//如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
//		if (size < 1024) {
//			return size + "B";
//		} else {
//			size = size / 1024;
//		}
//		//如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
//		//因为还没有到达要使用另一个单位的时候
//		//接下去以此类推
//		if (size < 1024) {
//			return String.valueOf(size) + "KB";
//		} else {
//			size = size / 1024;
//		}
//		if (size < 1024) {
//			//因为如果以MB为单位的话，要保留最后1位小数，
//			//因此，把此数乘以100之后再取余
//			size = size * 100;
//			return String.valueOf((size / 100)) + "."
//					+ String.valueOf((size % 100)) + "MB";
//		} else {
//			//否则如果要以GB为单位的，先除于1024再作同样的处理
//			size = size * 100 / 1024;
//			return String.valueOf((size / 100)) + "."
//					+ String.valueOf((size % 100)) + "GB";
//		}
//	}
//
//	public static ResponseEntity<byte[]> getFileStream(String path, List<File> files) {
//		InputStream stream = null;
//		try {
//			stream = new FileInputStream(new File(path));
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//
//			return new ResponseEntity<byte[]>(IOUtils.toByteArray(stream), headers, HttpStatus.CREATED);
//		} catch (IOException e) {
//			throw new RuntimeException("文件下载处理异常!", e);
//		} finally {
//			IOUtils.closeQuietly(stream);
//			if (CollectionUtils.isNotEmpty(files)) {
//				for (File file : files) {
//					FileUtils.deleteFile(file); // 删除文件
//				}
//			}
//		}
//	}
//}
