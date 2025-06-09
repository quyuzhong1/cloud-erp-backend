package com.common.core.file;

import com.common.core.security.SBase64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * 附件相关类
 *
 * @author Lwy
 * 2017年7月11日
 */
@Slf4j
public class FileUpload {

	private FileUpload() {
	}

	/**
	 * 上传附件
	 * @param file	文件
	 * @param folderPath	目录
	 * @return	服务器文件路径
	 */
	public static String writeFile(File file, String folderPath) {

		String savePath = new FileUpload().generateFileSavePath(folderPath, file.getName());
		InputStream input;
		try {
			input = new FileInputStream(file);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		OutputStream output = null;
		try {
			output = new FileOutputStream(folderPath + savePath);
			IOUtils.copy(input, output);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(input, output);
		}

		return folderPath + savePath;
	}

	/**
	 * Web版生成附件的保存路径，不包含根路径。 文件的保存路径格式为：根 + 类型 + 年份 + 时间串 + 文件后缀
	 *
	 * @param rootPath	路径
	 * @param origName 文件名
	 */
	private String generateFileSavePath(String rootPath, String origName) {
		// 获取随机码
		String uuid = UUID.randomUUID().toString();
		int i = origName.lastIndexOf('.');
		String ext = i > -1 ? origName.substring(i) : ""; // 文件后缀名
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		String savePath = format.format(new Date()) + "\\" + uuid + ext;
		// 确保目录已被创建
		File file = new File(rootPath + savePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return savePath;
	}


	/**
	 * 下载或预览  路径方式
	 *
	 * @param filePath
	 */
	public static void downLoad(String filePath, HttpServletResponse response, boolean isOnLine) throws Exception {
		File f = new File(filePath);
		if (!f.exists()) {
			response.sendError(404, "File not found!");
			return;
		}
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(f));
		byte[] buf = new byte[1024];
		int len = 0;

		response.reset(); // 非常重要
		if (isOnLine) { // 在线打开方式
			URL u = new URL("file:///" + filePath);
			response.setContentType(u.openConnection().getContentType());
			response.setHeader("Content-Disposition", "inline; filename=" + f.getName());
			// 文件名应该编码成UTF-8
		} else { // 纯下载方式
			response.setContentType("application/x-msdownload");
			response.setHeader("Content-Disposition", "attachment; filename=" + f.getName());
		}
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			while ((len = br.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(br, out);
		}

	}

	/**
	 * 下载或预览 IO流
	 *
	 * @param filePath
	 * @param fileName
	 */
	public static void downLoadIO(String filePath, String fileName, HttpServletResponse response) throws Exception {
		File f = new File(filePath);
		if (!f.exists()) {
			response.sendError(404, "File not found!");
			return;
		}
		if (StringUtils.isEmpty(fileName)) {
			fileName = f.getName();
		}
		// 支持中文名称文件,需要对header进行单独设置，不然下载的文件名会出现乱码或者无法显示的情况
		// String downloadFileName = new String(fileName .getBytes(), "ISO-8859-1");
		fileName = URLEncoder.encode(fileName, "UTF-8");

		response.reset(); // 非常重要
		URL u = new URL("file:///" + filePath);
		String contentType = u.openConnection().getContentType();
		response.setContentType(contentType);
		response.setHeader("Content-Disposition", "inline;filename=" + fileName);
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(f));
		byte[] buf = new byte[1024];
		int len = 0;

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			while ((len = br.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * 解析base64编码的文件流，例如pdf，excel等
	 *
	 * @param base 64编码
	 * @return
	 * @Param format 文件格式
	 */
	public static ResponseEntity<byte[]> base64Byte(String base, String format) {

		HttpHeaders headers = new HttpHeaders();
		try {
			headers.setContentDispositionFormData("attachment", "php." + format.toLowerCase());
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

			return new ResponseEntity<byte[]>(SBase64.base64ToByte(base), headers, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * 返回文件流(在线预览/下载)
	 *
	 * @param filePath 文件路径
	 * @param fileName 文件名(带后缀)
	 * @param bPreview 是否预览
	 * @return ResponseEntity<byte [ ]>
	 */
	public static ResponseEntity<byte[]> downloadByte(String filePath, String fileName, boolean bPreview) {
		File f = new File(filePath);
		if (!f.exists()) {
			return null;
		}
		if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
			fileName = f.getName();  // 无文件名或文件名不带后缀，自动取文件名
		}
		HttpHeaders headers = new HttpHeaders();
		String mode = "attachment"; // 默认
		if (bPreview) {
			mode = "inline"; // 在线预览
		}
		try {
			URL u = new URL("file:///" + filePath);
			fileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			headers.add("Content-Disposition", mode + ";filename=" + fileName);
			headers.setContentType(MediaType.valueOf(u.openConnection().getContentType()));

			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(f), headers, HttpStatus.CREATED);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 删除文件
	 *
	 * @param path
	 */
	public static boolean deleteFile(String path) {
		boolean flag = false;
		if (path != null) {
			File file = new File(path);
			// 路径为文件且不为空则进行删除
			if (file.isFile() && file.exists()) {
				boolean deleteResult = file.delete();
				if (!deleteResult){
					log.warn("file.delete 删除失败");
				}
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 对临时生成的文件夹和文件夹下的文件进行删除
	 */
	public static void deleteFileFolder(String delpath) {
		try {
			File file = new File(delpath);
			if (!file.isDirectory()) {
				boolean deleteResult = file.delete();
				if (!deleteResult){
					log.warn("file.delete 删除失败");
				}
			} else if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File delfile = new File(delpath + File.separator + filelist[i]);
					if (!delfile.isDirectory()) {
						boolean deleteResult = delfile.delete();
						if (!deleteResult){
							log.warn("file.delete 删除失败");
						}
					} else if (delfile.isDirectory()) {
						deleteFileFolder(delpath + File.separator + filelist[i]);
					}
				}
				boolean deleteResult = file.delete();
				if (!deleteResult){
					log.warn("file.delete 删除失败");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
