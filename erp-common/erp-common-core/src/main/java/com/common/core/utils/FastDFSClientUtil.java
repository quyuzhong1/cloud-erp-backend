package com.common.core.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FastDFS文件上传下载工具类
 */
@Configuration
@Slf4j
public class FastDFSClientUtil {

	private static String configFile;

	/**
	 * 全类共享的单个 FastDFS 客户端（底层单 socket 连接）。{@link StorageClient1} 非线程安全，
	 * 故各上传/下载方法以 {@code synchronized} 串行化对该共享连接的访问；这也是并发上传瓶颈的根因。
	 * 若需提升并发，应改为连接池或按调用新建短连接，而非简单去掉方法上的 {@code synchronized}。
	 */
	private static StorageClient1 storageClient1 = null;

	public static String publicUrl;

	@Value("${fdfs.configFile}")
	public void setConfigFile(String configFile){
		FastDFSClientUtil.configFile = configFile;
	}

	@Value("${fdfs.publicUrl:''}")
	public void setPublicUrl(String publicUrl){
		FastDFSClientUtil.publicUrl = publicUrl;
	}


	/**
	 * 初始化FastDFS Client
	 */
	public static StorageClient1 getStorageClient() {
		if (storageClient1 == null) {
			try {
				ClientGlobal.initByProperties(configFile);
				TrackerClient trackerClient = new TrackerClient(ClientGlobal.g_tracker_group);
				TrackerServer trackerServer = trackerClient.getTrackerServer();
				StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
				storageClient1 = new StorageClient1(trackerServer, storageServer);
			} catch (Exception e) {
				throw new RuntimeException("连接存储服务器出错", e);
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
	 * @param multipartFile     文件对象
	 * @return
	 */
	public synchronized static String uploadFile(MultipartFile multipartFile) {
		String originalFilename = multipartFile.getOriginalFilename().toLowerCase();
		String fileName = CharSequenceUtil.isBlank(originalFilename) ? multipartFile.getName() : originalFilename;
		File file = FileUtil.multiToFile(multipartFile);
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
			FileInputStream input = new FileInputStream(file);
			byte[] buff = IOUtils.toByteArray(input);
			String uploadFile = uploadFile(fileName, metaList, buff);
			input.close();
			return uploadFile;

		} catch (Exception e) {
			log.error("uploadFile  ",e);
		}
		return null;
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
	 * 指定字符集上传文件
	 *
	 * @param inputStream 文件流
	 * @param fileName    文件名
	 * @param metaList    文件元数据
	 * @return
	 */
	public synchronized static String uploadFile(InputStreamReader inputStream, Charset charset, String fileName, Map<String, String> metaList) {
		try {
			byte[] buff = IOUtils.toByteArray(inputStream, charset);
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


	public static void deleteBatchFile(List<String> urlList) {
		try {
			if(CollectionUtils.isNotEmpty(urlList)){
				for(String url:urlList){
					deleteFile(url);
				}

			}
		}catch (Exception e){
          log.error("批量删除fastDFS 出错  result ={}", JSONUtil.toJsonStr(urlList));
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
	 * 校验文件是否存在
	 * @param fileUrl 文件路径
	 */
	public static boolean exist(String fileUrl) {
		try {
			FileInfo fileInfo = getStorageClient().query_file_info1(fileUrl);
			return !ObjectUtils.isEmpty(fileInfo);
		}catch (Exception e){
			return false;
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

	/**
	 * 批量获取文件大小
	 * @param fileUrlList 文件URL列表
	 * @return Map，key为文件URL，value为文件大小（字节），如果文件不存在或获取失败，value为null
	 */
	public static Map<String, Long> getBatchFileSize(List<String> fileUrlList) {
		Map<String, Long> result = new HashMap<>();
		if (CollectionUtils.isEmpty(fileUrlList)) {
			return result;
		}
		
		for (String fileUrl : fileUrlList) {
			if (StringUtils.isBlank(fileUrl)) {
				continue;
			}
			try {
				FileInfo fileInfo = getStorageClient().query_file_info1(fileUrl);
				if (fileInfo != null) {
					// FileInfo对象包含文件大小信息，通过getFileSize()方法获取
					long fileSize = fileInfo.getFileSize();
					result.put(fileUrl, fileSize);
				} else {
					result.put(fileUrl, null);
				}
			} catch (Exception e) {
				log.warn("获取文件大小失败，url={}, 错误信息={}", fileUrl, e.getMessage());
				result.put(fileUrl, null);
			}
		}
		return result;
	}

    /**
     * 流式上传底层方法：仅供已持有共享连接锁的 {@link #streamUploadFile} 调用，禁止对外直接使用。
     * {@link StorageClient1} 非线程安全且全 JVM 共享单连接，绕过 {@code streamUploadFile} 的 {@code synchronized}
     * 直接并发调用本方法会导致数据串包/文件损坏，故声明为 {@code private}。
     */
    private static String uploadFile2Client(long fileSize, UploadCallback callback, String fileName, Map<String, String> metaList) throws IOException, MyException {
        NameValuePair[] nameValuePairs = null;
        if (metaList != null) {
            nameValuePairs = new NameValuePair[metaList.size()];
            int index = 0;
            for (Map.Entry<String, String> entry : metaList.entrySet()) {
                nameValuePairs[index++] = new NameValuePair(entry.getKey(), entry.getValue());
            }
        }
        String filePath = getStorageClient().upload_file1("", fileSize, callback, FilenameUtils.getExtension(fileName), nameValuePairs);
        return checkFileId(filePath);
    }

	/**
	 * 上传文件（流式，避免整文件读入内存）。
	 * <p>
	 * <strong>{@code synchronized} 系有意为之，请勿删除：</strong>全类共用一个静态 {@link #storageClient1}
	 * （底层单 socket 连接），而 {@code org.csource} 的 {@link StorageClient1} 非线程安全，多线程在同一连接上
	 * 并发读写会串包、数据损坏。此处的锁是保护这唯一共享连接的正确性手段，去锁前必须先改掉"全 JVM 共用单连接"
	 * 的模型（如引入连接池或按调用新建短连接），否则会从"上传串行变慢"升级为"上传数据损坏"。
	 * 已知的并发瓶颈根因即此共享单连接，后续优化方向为连接池。
	 *
	 * @param file     文件对象
	 * @param fileName 文件名
	 * @param metaList 文件元数据
	 * @return 上传成功后的文件 ID（group/path）
	 */
	public synchronized static String streamUploadFile(File file, String fileName, Map<String, String> metaList) {
        try {
            long size = file.length();
            // 使用 File 打开流，避免 getAbsolutePath/getCanonicalPath 与路径字符串在相对路径、符号链接等场景下与 JVM 解析不一致
            UploadCallback sender = out -> {
                try (FileInputStream fis = new FileInputStream(file)) {
                    IOUtils.copy(fis, out);
                }
                // 返回值是 FastDFS 协议的 errno（状态码），不是写入字节数：
                // csource StorageClient#do_upload_file 会把 callback.send(out) 的返回值作为本次上传的错误码，
                // 0=成功，非 0=失败（do_upload_file 直接返回 null，表现为上传"返回空"）。
                // 上传字节数由协议头里的 file_size（即 upload_file1 传入的 size）单独声明，回调只负责把字节写入 out。
                // 切勿改成返回 IOUtils.copy 的字节数，否则任意非空文件都会变成非 0 errno 而上传失败。
                return 0;
            };
            return uploadFile2Client(size, sender, fileName, metaList);
        } catch (Exception e) {
            // 保留 cause，便于全局异常处理器/线上日志定位根因；对外仍只暴露 FILE_UPLOAD_FAILED 文案，不泄露内部路径
            throw new ServiceException(e, ApiError.FILE_UPLOAD_FAILED);
        }
    }
}
