package com.common.core.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketException;

public class FTPUtil {

	private static Logger logger = LoggerFactory.getLogger(FTPUtil.class);

	/**
	 * 获取FTPClient对象
	 *
	 * @param host     服务器IP
	 * @param port     服务器端口号
	 * @param userName 用户名
	 * @param password 密码
	 * @return FTPClient
	 */
	public FTPClient getFTPClient(String host, int port, String userName, String password) {
		return this.getFTPClient(host, port, userName, password, false);
	}

	/**
	 * 获取FTPClient对象
	 *
	 * @param host        服务器IP
	 * @param port        服务器端口号
	 * @param userName    用户名
	 * @param password    密码
	 * @param bActiveMode 是否是主动模式
	 * @return FTPClient
	 */
	public FTPClient getFTPClient(String host, int port, String userName, String password, boolean bActiveMode) {
		FTPClient ftpClient = null;
		try {
			ftpClient = new FTPClient();
			// 连接FPT服务器,设置IP及端口
			ftpClient.connect(host, port);
			// 设置用户名和密码
			ftpClient.login(userName, password);
			// 设置连接超时时间,5000毫秒
			ftpClient.setConnectTimeout(50000);
			// 设置中文编码集，防止中文乱码
			ftpClient.setControlEncoding("UTF-8");
			if (bActiveMode) {
				ftpClient.enterLocalActiveMode();
			} else {
				ftpClient.enterLocalPassiveMode();
			}
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				logger.info("未连接到FTP，用户名或密码错误");
				ftpClient.disconnect();
			} else {
				logger.info("FTP连接成功");
			}

		} catch (SocketException e) {
			e.printStackTrace();
			logger.info("FTP的IP地址可能错误，请正确配置");
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("FTP的端口错误,请正确配置");
		}
		return ftpClient;
	}

	/**
	 * 关闭FTP方法
	 *
	 * @param ftpClient
	 * @return
	 */
	public boolean closeFTP(FTPClient ftpClient) {
		try {
			if (ftpClient != null) {
				ftpClient.logout();
			}
		} catch (Exception e) {
			logger.error("FTP关闭失败");
		} finally {
			if (ftpClient != null && ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException ioe) {
					logger.error("FTP关闭失败");
				}
			}
		}

		return false;
	}

	/**
	 * 下载FTP下指定文件
	 *
	 * @param ftpClient FTPClient对象
	 * @param filePath  FTP文件路径
	 * @param fileName  文件名
	 * @param downPath  下载保存的目录
	 * @return
	 */
	public void downLoadFTP(FTPClient ftpClient, String filePath, String fileName, String downPath) {
		InputStream in = null;
		FileOutputStream out = null;

		try {
			// 跳转到文件目录
			ftpClient.changeWorkingDirectory(filePath);
			// 设置以二进制方式传输
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

			in = ftpClient.retrieveFileStream(fileName);
			out = new FileOutputStream(downPath + File.separator + fileName);
			IOUtils.copy(in, out);

		} catch (Exception e) {
			logger.error("下载失败");
		} finally {
			try {
				assert out != null;
				out.flush();
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 下载FTP下指定文件
	 *
	 * @param ftpClient FTPClient对象
	 * @param filePath  FTP文件路径
	 * @param fileName  文件名
	 * @return 注意，调用方一定要关闭InputStream
	 */
	public InputStream downLoadFTP(FTPClient ftpClient, String filePath, String fileName) {
		InputStream in = null;

		try {
			// 跳转到文件目录
			ftpClient.changeWorkingDirectory(filePath);
			// 设置以二进制方式传输
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

			in = ftpClient.retrieveFileStream(fileName);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("下载失败");
		}

		return in;
	}

	/**
	 * FTP文件上传工具类
	 *
	 * @param ftpClient
	 * @param filePath
	 * @param ftpPath
	 * @return
	 */
	public boolean uploadFile(FTPClient ftpClient, String filePath, String ftpPath) {
		boolean flag = false;
		InputStream in = null;
		try {
			// 设置PassiveMode传输
			ftpClient.enterLocalPassiveMode();
			//设置二进制传输，使用BINARY_FILE_TYPE，ASC容易造成文件损坏
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			//判断FPT目标文件夹时候存在不存在则创建
			if (!ftpClient.changeWorkingDirectory(ftpPath)) {
				ftpClient.makeDirectory(ftpPath);
			}
			//跳转目标目录
			ftpClient.changeWorkingDirectory(ftpPath);

			//上传文件
			File file = new File(filePath);
			in = new FileInputStream(file);
			String tempName = ftpPath + File.separator + file.getName();
			flag = ftpClient.storeFile(new String(tempName.getBytes("UTF-8"), "ISO-8859-1"), in);
			if (flag) {
				logger.info("上传成功");
			} else {
				logger.error("上传失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("上传失败");
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}

	public boolean uploadFile(FTPClient ftpClient, InputStream inputStream, String filename, String ftpPath) {
		boolean flag = false;
		try {
			// 设置PassiveMode传输
			ftpClient.enterLocalPassiveMode();
			//设置二进制传输，使用BINARY_FILE_TYPE，ASC容易造成文件损坏
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			//判断FPT目标文件夹时候存在不存在则创建
			if (!ftpClient.changeWorkingDirectory(ftpPath)) {
				ftpClient.makeDirectory(ftpPath);
			}
			//跳转目标目录
			ftpClient.changeWorkingDirectory(ftpPath);

			//上传文件
			String tempName = ftpPath + File.separator + filename;
			flag = ftpClient.storeFile(new String(tempName.getBytes("UTF-8"), "ISO-8859-1"), inputStream);
			if (flag) {
				logger.info("上传成功");
			} else {
				logger.error("上传失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("上传失败");
		}

		return flag;
	}

	/**
	 * FPT上文件的复制
	 *
	 * @param ftpClient FTPClient对象
	 * @param olePath   原文件地址
	 * @param newPath   新保存地址
	 * @param fileName  文件名
	 * @return
	 */
	public boolean copyFile(FTPClient ftpClient, String olePath, String newPath, String fileName) {
		boolean flag = false;

		try {
			// 跳转到文件目录
			ftpClient.changeWorkingDirectory(olePath);
			//设置连接模式，不设置会获取为空
			ftpClient.enterLocalPassiveMode();
			// 获取目录下文件集合
			FTPFile[] files = ftpClient.listFiles();
			ByteArrayInputStream in = null;
			ByteArrayOutputStream out = null;
			for (FTPFile file : files) {
				// 取得指定文件并下载
				if (file.getName().equals(fileName)) {

					//读取文件，使用下载文件的方法把文件写入内存,绑定到out流上
					out = new ByteArrayOutputStream();
					ftpClient.retrieveFile(new String(file.getName().getBytes("UTF-8"), "ISO-8859-1"), out);
					in = new ByteArrayInputStream(out.toByteArray());
					//创建新目录
					ftpClient.makeDirectory(newPath);
					//文件复制，先读，再写
					//二进制
					ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
					flag = ftpClient.storeFile(newPath + File.separator + (new String(file.getName().getBytes("UTF-8"), "ISO-8859-1")), in);
					out.flush();
					out.close();
					in.close();
					if (flag) {
						logger.info("转存成功");
					} else {
						logger.error("复制失败");
					}


				}
			}
		} catch (Exception e) {
			logger.error("复制失败");
		}
		return flag;
	}

	/**
	 * 实现文件的移动，这里做的是一个文件夹下的所有内容移动到新的文件，
	 * 如果要做指定文件移动，加个判断判断文件名
	 * 如果不需要移动，只是需要文件重命名，可以使用ftp.rename(oleName,newName)
	 *
	 * @param ftpClient
	 * @param oldPath
	 * @param newPath
	 * @return
	 */
	public boolean moveFile(FTPClient ftpClient, String oldPath, String newPath) {
		boolean flag = false;

		try {
			ftpClient.changeWorkingDirectory(oldPath);
			ftpClient.enterLocalPassiveMode();
			//获取文件数组
			FTPFile[] files = ftpClient.listFiles();
			//新文件夹不存在则创建
			if (!ftpClient.changeWorkingDirectory(newPath)) {
				ftpClient.makeDirectory(newPath);
			}
			//回到原有工作目录
			ftpClient.changeWorkingDirectory(oldPath);
			for (FTPFile file : files) {

				//转存目录
				flag = ftpClient.rename(new String(file.getName().getBytes("UTF-8"), "ISO-8859-1"), newPath + File.separator + new String(file.getName().getBytes("UTF-8"), "ISO-8859-1"));
				if (flag) {
					logger.info(file.getName() + "移动成功");
				} else {
					logger.error(file.getName() + "移动失败");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("移动文件失败");
		}
		return flag;
	}

	/**
	 * 删除FTP上指定文件夹下文件及其子文件方法，添加了对中文目录的支持
	 *
	 * @param ftpClient FTPClient对象
	 * @param FtpFolder 需要删除的文件夹
	 * @return
	 */
	public boolean deleteByFolder(FTPClient ftpClient, String FtpFolder) {
		boolean flag = false;
		try {
			ftpClient.changeWorkingDirectory(new String(FtpFolder.getBytes("UTF-8"), "ISO-8859-1"));
			ftpClient.enterLocalPassiveMode();
			FTPFile[] files = ftpClient.listFiles();
			for (FTPFile file : files) {
				//判断为文件则删除
				if (file.isFile()) {
					ftpClient.deleteFile(new String(file.getName().getBytes("UTF-8"), "ISO-8859-1"));
				}
				//判断是文件夹
				if (file.isDirectory()) {
					String childPath = FtpFolder + File.separator + file.getName();
					//递归删除子文件夹
					deleteByFolder(ftpClient, childPath);
				}
			}
			//循环完成后删除文件夹
			flag = ftpClient.removeDirectory(new String(FtpFolder.getBytes("UTF-8"), "ISO-8859-1"));
			if (flag) {
				logger.info(FtpFolder + "文件夹删除成功");
			} else {
				logger.error(FtpFolder + "文件夹删除成功");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除失败");
		}
		return flag;

	}

	/**
	 * 遍历解析文件夹下所有文件
	 *
	 * @param folderPath 需要解析的的文件夹
	 * @param ftpClient  FTPClient对象
	 * @return
	 */
	public boolean readFileByFolder(FTPClient ftpClient, String folderPath) {
		boolean flage = false;
		try {
			ftpClient.changeWorkingDirectory(new String(folderPath.getBytes("UTF-8"), "ISO-8859-1"));
			//设置FTP连接模式
			ftpClient.enterLocalPassiveMode();
			//获取指定目录下文件文件对象集合
			FTPFile files[] = ftpClient.listFiles();
			InputStream in = null;
			BufferedReader reader = null;
			for (FTPFile file : files) {
				//判断为txt文件则解析
				if (file.isFile()) {
					String fileName = file.getName();
					if (fileName.endsWith(".txt")) {
						in = ftpClient.retrieveFileStream(new String(file.getName().getBytes("UTF-8"), "ISO-8859-1"));
						reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
						String temp;
						StringBuffer buffer = new StringBuffer();
						while ((temp = reader.readLine()) != null) {
							buffer.append(temp);
						}
						if (reader != null) {
							reader.close();
						}
						if (in != null) {
							in.close();
						}
						//ftpClient.retrieveFileStream使用了流，需要释放一下，不然会返回空指针
						ftpClient.completePendingCommand();
						//这里就把一个txt文件完整解析成了个字符串，就可以调用实际需要操作的方法
						System.out.println(buffer.toString());
					}
				}
				//判断为文件夹，递归
				if (file.isDirectory()) {
					String path = folderPath + File.separator + file.getName();
					readFileByFolder(ftpClient, path);
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
			logger.error("文件解析失败");
		}

		return flage;

	}

	public static void main(String[] args) {
		FTPUtil ftpUtil = new FTPUtil();
		FTPClient ftpClient = ftpUtil.getFTPClient("192.168.7.116", 21, "zz", "332211");
		ftpUtil.downLoadFTP(ftpClient, "/SellerCenteral/202044/", "f08993dc-1ce7-11eb-a073-00d8619a8fb9.pdf", "C:\\Users\\admin\\Desktop\\temp");
		//test.copyFile(ftpClient, "/file", "/txt/temp", "你好.txt");
		//test.uploadFile(ftpClient, "C:\\下载\\你好.jpg", "/");
		//test.moveFile(ftpClient, "/file", "/txt/temp");
		//test.deleteByFolder(ftpClient, "/txt");
//		test.readFileByFolder(ftpClient, "/");
		ftpUtil.closeFTP(ftpClient);
		System.exit(0);
	}
}
