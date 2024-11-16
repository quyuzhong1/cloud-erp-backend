package com.common.core.file;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CSV文件导出工具类
 * <p>
 * Created on 2014-08-07
 *
 * @author
 * @reviewer
 */
@Slf4j
public class CsvUtils {

	public static Object resetNull(Object obj) {
		return obj == null ? "" : obj;
	}

	/**
	 * CSV文件生成方法
	 *
	 * @param head
	 * @param dataList
	 * @param outPutPath
	 * @param filename
	 * @return
	 */
	public static File createCSVFile(List<Object> head, List<List<Object>> dataList,
	                                 String outPutPath, String filename) {

		File csvFile = null;
		BufferedWriter csvWriter = null;
		try {
			csvFile = new File(outPutPath + File.separator + filename + ".csv");
			File parent = csvFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			csvFile.createNewFile();

			// GB2312使正确读取分隔符","
			csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					csvFile), "gbk"), 1024);
			// 写入文件头部
			writeRow(head, csvWriter);

			// 写入文件内容
			for (List<Object> row : dataList) {
				writeRow(row, csvWriter);
			}
			csvWriter.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(csvWriter);
		}

		return csvFile;
	}

	public static File createCSVFile(String head, List<String> dataList, String outPutPath, String filename, boolean bSystemLineSep) {
		File file = null;
		BufferedWriter bufferedWriter = null;
		try {
			file = new File(outPutPath + File.separator + filename + ".csv");
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			file.createNewFile();

			// GB2312使正确读取分隔符","
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), 1024);
			// 写入文件头部
			bufferedWriter.write(head);
			bufferedWriter.newLine();

			// 写入文件内容
			for (String row : dataList) {
				bufferedWriter.write(row);
				//Unix系统里，每行结尾只有“<换行>”，即“\n”；Windows系统里面，每行结尾是“ <回车><换行>”，即“\r\n”
				if(bSystemLineSep) {
					bufferedWriter.newLine();
				} else {
					bufferedWriter.write("\r\n");
				}
			}
			bufferedWriter.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(bufferedWriter);
		}

		return file;
	}

	/**
	 * 写一行数据方法
	 *
	 * @param row
	 * @param csvWriter
	 * @throws IOException
	 */
	private static void writeRow(List<Object> row, BufferedWriter csvWriter) throws IOException {
		// 写入文件头部
		for (Object data : row) {
			String rowStr = "\"" + (data == null ? "" : data) + "\",";
			csvWriter.write(rowStr);
		}
		csvWriter.newLine();
	}

	/**
	 * 使用Filewriter
	 */
	public static void useBufferedWriter(List<List<Object>> dataList, String filePath) {
		FileWriter fWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			File file = new File(filePath + "text03.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			// 一次写入的文件大小小于10M时， bufferedWriter并不能显著降低时间,而且此时BufferedOutputStream仍是占优的
			fWriter = new FileWriter(file, true);
			bufferedWriter = new BufferedWriter(fWriter);
			long begin = System.currentTimeMillis();
			for (List<Object> row : dataList) {
				writeRow(row, bufferedWriter);
			}
			bufferedWriter.flush();
			log.info("BufferedWriter 执行耗时: " + (System.currentTimeMillis() - begin));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(fWriter, bufferedWriter);
		}
	}
}
