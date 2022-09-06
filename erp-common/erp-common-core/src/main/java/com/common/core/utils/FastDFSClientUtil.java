package com.common.core.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * FastDFS文件上传下载工具类
 */
public class FastDFSClientUtil {

	private static final String CONFIG_FILENAME = "config.properties";
	private static StorageClient1 storageClient1 = null;

	// 初始化FastDFS Client
	public static StorageClient1 getStorageClient() {
		if (storageClient1 == null) {
			synchronized (FastDFSClientUtil.class) {
				if (storageClient1 == null) {
					try {
						ClientGlobal.initByProperties(CONFIG_FILENAME);
						TrackerClient trackerClient = new TrackerClient(ClientGlobal.g_tracker_group);
						TrackerServer trackerServer = trackerClient.getTrackerServer();
						StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
						storageClient1 = new StorageClient1(trackerServer, storageServer);
					} catch (Exception e) {
						throw new RuntimeException("连接存储服务器出错", e);
					}
				}
			}
		}
		return storageClient1;
	}


	/**
	 * 上传文件
	 *
	 * @param file     文件对象
	 * @param fileName 文件名
	 * @return
	 */
	public synchronized static String uploadFile(File file, String fileName) {
		return uploadFile(file, fileName, null);
	}

	/**
	 * 上传文件
	 *
	 * @param file     文件对象
	 * @param fileName 文件名
	 * @param metaList 文件元数据
	 * @return
	 */
	public synchronized static String uploadFile(File file, String fileName, Map<String, String> metaList) {
		try {
			byte[] buff = IOUtils.toByteArray(new FileInputStream(file));
			return uploadFile(fileName, metaList, buff);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String uploadFile(String fileName, Map<String, String> metaList, byte[] buff) throws IOException, MyException {
		return uploadFile2Client(buff, fileName, metaList);
	}


	public static String checkFileId(String filePath) {
		if (!filePath.contains("group1") && filePath.startsWith("M00")) {
			filePath = "group1/" + filePath.replace("//", "/");
			// 验证文件是否存在
			try {
				getInputStream(filePath);
			} catch (Exception e) {
				throw new RuntimeException("文件上传异常！");
			}
		}
		if (!filePath.startsWith("group1")) {
			throw new RuntimeException(MessageFormat.format("上传文件路径[{0}]异常！", filePath));
		}
		return filePath;
	}

	/**
	 * 上传文件
	 *
	 * @param buff     文件流
	 * @param fileName 文件名
	 * @param metaList 文件元数据
	 * @return
	 */
	public synchronized static String uploadFile(byte[] buff, String fileName, Map<String, String> metaList) {
		try {
			return uploadFile2Client(buff, fileName, metaList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String uploadFile2Client(byte[] buff, String fileName, Map<String, String> metaList) throws IOException, MyException {
		NameValuePair[] nameValuePairs = null;
		if (metaList != null) {
			nameValuePairs = new NameValuePair[metaList.size()];
			int index = 0;
			for (Map.Entry<String, String> entry : metaList.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				nameValuePairs[index++] = new NameValuePair(name, value);
			}
		}
		String filePath = getStorageClient().upload_file1(buff, FilenameUtils.getExtension(fileName), nameValuePairs);

		// 兼容fastDFS上传文件路径异常问题   20201019
		return checkFileId(filePath);
	}

	/**
	 * 上传文件
	 *
	 * @param inputStream 文件流
	 * @param fileName    文件名
	 * @param metaList    文件元数据
	 * @return
	 */
	public synchronized static String uploadFile(InputStream inputStream, String fileName, Map<String, String> metaList) {
		try {
			byte[] buff = IOUtils.toByteArray(inputStream);
			return uploadFile2Client(buff, fileName, metaList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * 获取文件元数据
	 *
	 * @param fileId 文件ID
	 * @return
	 */
	public static Map<String, String> getFileMetadata(String fileId) {
		try {
			NameValuePair[] metaList = getStorageClient().get_metadata1(fileId);
			if (metaList != null) {
				HashMap<String, String> map = new HashMap<String, String>();
				for (NameValuePair metaItem : metaList) {
					map.put(metaItem.getName(), metaItem.getValue());
				}
				return map;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	 * 删除文件
	 *
	 * @param fileId 文件ID
	 * @return 删除失败返回-1，否则返回0
	 */
	public static int deleteFile(String fileId) {
		try {

			return getStorageClient().delete_file1(fileId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 下载文件
	 *
	 * @param fileId  文件ID（上传文件成功后返回的ID）
	 * @param outFile 文件下载保存位置
	 * @return
	 */
	public static int downloadFile(String fileId, File outFile) {
		FileOutputStream fos = null;
		try {
			byte[] content = getStorageClient().download_file1(fileId);
			fos = new FileOutputStream(outFile);
			IOUtils.write(content, fos);
			return 0;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	/**
	 * 在线下载
	 *
	 * @param fileId      文件id
	 * @param fileName    文件名
	 * @param contentType 文件类型
	 * @param bPreview    是否预览
	 * @return
	 */
	public synchronized static ResponseEntity<byte[]> downloadByte(String fileId, String fileName, String contentType, boolean bPreview) {
		if (StringUtils.isBlank(fileId)) {
			return null;
		}
		HttpHeaders headers = new HttpHeaders();
		String mode = "attachment"; // 默认
		if (bPreview) {
			mode = "inline"; // 在线预览
		}
		try {
			fileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			headers.add("Content-Disposition", mode + ";filename=" + fileName);
			headers.setContentType(MediaType.valueOf(contentType));

			byte[] content = getStorageClient().download_file1(fileId);

			return new ResponseEntity<byte[]>(content, headers, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取文件流
	 */
	public synchronized static InputStream getInputStream(String fileId) {
		if (StringUtils.isBlank(fileId)) {
			return null;
		}
		try {
			byte[] content = getStorageClient().download_file1(fileId);

			return new ByteArrayInputStream(content);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取文件流
	 */
	public synchronized static byte[] getFileByte(String fileId) {
		if (StringUtils.isBlank(fileId)) {
			return null;
		}
		try {
			return getStorageClient().download_file1(fileId);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
