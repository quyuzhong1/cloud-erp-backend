package com.common.core.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.common.core.utils.IdUtils;
import com.common.core.utils.R;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.*;

/**
 * 导出Excel 模板
 *
 * @author egbert
 */
public class ExcelPrintUtils {
	private static final Log log = LogFactory.getLog(ExcelPrintUtils.class);

	long startTime = 0;

	public static void main(String[] args) throws InvalidFormatException, IOException {
		List<String> templateSheetNames = new ArrayList<String>();
		List<String> sheetNames = new ArrayList<String>();
		List<Map<String, Object>> beansList = new ArrayList<Map<String, Object>>();
		Map<String, Object> introBeansMap = new HashMap<String, Object>();
		introBeansMap.put("cs", "测试数据1");
		FileInputStream is = new FileInputStream(new File("D:/model.xlsx"));
		// Populate each list here with the same number of objects.
		templateSheetNames.add("instro");
		sheetNames.add("Introduction");
		beansList.add(introBeansMap);
		templateSheetNames.add("sheetToClone");
		sheetNames.add("Q1 2011");
//		beansList.add(introBeansMap);
		templateSheetNames.add("sheetToClone");
		sheetNames.add("Q2 2011");
		beansList.add(introBeansMap);
		templateSheetNames.add("sheetToClone");
		sheetNames.add("Q3 2011");
		beansList.add(introBeansMap);
		templateSheetNames.add("sheetToClone");
		sheetNames.add("Q4 2011");
		beansList.add(introBeansMap);
	}

	/**
	 * 保存临时文件
	 *
	 * @param fileName 文件名称（带后缀）
	 */
	private String toSaveTempFile(String fileName) {
		String uuid = UUID.randomUUID().toString();
		// 下载excel文件名称
		if (StringUtils.isBlank(fileName)) {
			fileName = IdUtils.randomUUID().toString() + ".xlsx";
		} else {
			fileName = uuid + File.separator + fileName; // 文件名称一样，增加目录存储防止文件覆盖
		}
		// 重新生成文件的名称
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		String savePath = format.format(new Date()) + File.separator + fileName;

		// 生成的文件路径
		final String EXCEL_FILE_PATH = "Attachments" + File.separator + "excel" + File.separator;
		String targetPath = EXCEL_FILE_PATH + savePath;
		File file = new File(targetPath);
		if (!file.exists()) {
			// 先得到文件的上级目录，并创建上级目录，在创建文件
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
		}

		return targetPath;
	}

	/**
	 * 模板文件流获取
	 *
	 * @param fileName
	 * @return
	 */
	private InputStream getTemplate(String fileName) {
		final String JXLS_EXCEL = "excel/print/";
		InputStream stream = null;
		try {
			String name = JXLS_EXCEL + fileName;
			stream = getClass().getClassLoader().getResourceAsStream(name);

			log.debug("templateFileName:" + name);

			if (stream == null) {
				stream = new FileInputStream(fileName);
			}
		} catch (Exception e) {
			System.out.println("获取打印模板文件流" + fileName + "异常!");
		}
		return stream;
	}

	public String exportToExcel(String templateName, String fileName, List<Map<String, Object>> list) {
		return exportToExcel(templateName, fileName, list, null);
	}

	/**
	 * easyexcel填充模板导出 模板中：单个用{xxx} list用{.xxx}
	 * 参考文档：https://www.yuque.com/easyexcel/doc/fill
	 */
	public String exportToExcel(String templateName, String fileName, List<Map<String, Object>> list, Map<String, Object> map) {
		InputStream stream = null;
		try {
			// 获取模板文件流
			stream = getTemplate(templateName);
			if (stream == null) {
				R.runError("模板文件流获取异常！"); // 文件流异常
			}

			String tempPath = toSaveTempFile(fileName); // 获取保存的文件路径

			// 根据模板生成excel
			ExcelWriter excelWriter = EasyExcel.write(tempPath)
					.registerConverter(new SqlDateNumberConverter())
					.registerConverter(new SqlDateStringConverter())
					.registerConverter(new SqlTimestampStringConverter())
					.withTemplate(stream)
					.build();
			WriteSheet writeSheet = EasyExcel.writerSheet().build();

			excelWriter.fill(list, writeSheet);
			if (map != null) {
				excelWriter.fill(map, writeSheet);
			}
			excelWriter.finish();

			// 获取excel下载文件流
			return tempPath;

		} catch (Exception e) {
			R.runError("excel下载，操作文件流异常！" + e);
		} finally {
			IOUtils.closeQuietly(stream); // 解决流关闭问题
		}

		return null;
	}

	/**
	 * easyexcel导出支持多个页签(heads 与 detailList 列表大小必须一致)
	 * @param templateName
	 * @param fileName
	 * @param heads
	 * @param list
	 * @return
	 */
	public ResponseEntity<byte[]> easyexcelExportMutSheet(String templateName, String fileName,
	                                                      List<Map<String, Object>> heads, List<Object> list) {
		InputStream stream = null;
		try {
			stream = getTemplate(templateName);// 获取模板文件流
			if (stream == null) {
				R.runError("模板文件流获取异常！"); // 文件流异常
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// 根据模板生成excel
			ExcelWriter excelWriter = EasyExcel.write(out)
					.registerConverter(new SqlDateNumberConverter())
					.registerConverter(new SqlDateStringConverter())
					.registerConverter(new SqlTimestampStringConverter())
					.withTemplate(stream)
					.build();
			for(int i = 0; i <list.size(); i++) {
				WriteSheet writeSheet = EasyExcel.writerSheet(i).build();
				excelWriter.fill(list.get(i), writeSheet); // 填充list数据

				if (CollectionUtils.isNotEmpty(heads)) {
					Map<String, Object> map = heads.get(i);
					if(map != null && !map.isEmpty()) {
						excelWriter.fill(map, writeSheet);
					}
				}
			}
			excelWriter.finish();

			HttpHeaders headers = new HttpHeaders();
			if (StringUtils.isBlank(fileName)) {
				fileName = IdUtils.randomUUID().toString() + ".xlsx";
			}
			fileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			headers.add("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

			return new ResponseEntity<byte[]>(out.toByteArray(), headers, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(stream); // 解决流关闭问题
		}

	}

	public ResponseEntity<byte[]> exportEasyexcelStream(String templateName, String fileName, List<Map<String, Object>> dataList, boolean fillNullLine) {
		return exportEasyexcelStream(templateName, fileName, dataList, null, 0, fillNullLine);
	}

	public ResponseEntity<byte[]> exportEasyexcelStream(String templateName, String fileName, List<Map<String, Object>> dataList) {
		return exportEasyexcelStream(templateName, fileName, dataList, null, 0);
	}
	public ResponseEntity<byte[]> exportEasyexcelStreamEntity(String templateName, String fileName, Object dataList) {
		return exportEasyexcelStream(templateName, fileName, dataList, null, 0);
	}

	public ResponseEntity<byte[]> exportEasyexcelPointSheet(String templateName, String fileName, Object dataList, Integer sheetNo) {
		return exportEasyexcelStream(templateName, fileName, dataList, null, sheetNo);
	}

	public ResponseEntity<byte[]> exportEasyexcelStream(String templateName, String fileName, Object dataList, Map<String, Object> map, Integer sheetNo, boolean fillNullLine) {
		InputStream stream = null;  //List<Map<String, Object>> dataList
		try {
			// 获取模板文件流
			stream = getTemplate(templateName);
			if (stream == null) {
				R.runError("模板文件流获取异常！"); // 文件流异常
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// 根据模板生成excel
			ExcelWriter excelWriter = EasyExcel.write(out)
					.registerConverter(new SqlDateNumberConverter())
					.registerConverter(new SqlDateStringConverter())
					.registerConverter(new SqlTimestampStringConverter())
					.withTemplate(stream)
					.build();
			WriteSheet writeSheet = EasyExcel.writerSheet(sheetNo).build();

			// 是否填入空行
			if (fillNullLine) {
				List<Map<String, Object>> nullLine = new ArrayList<>();
				nullLine.add(new HashMap<>());
				excelWriter.fill(nullLine, writeSheet);
			}

			excelWriter.fill(dataList, writeSheet);
			if (map != null) {
				excelWriter.fill(map, writeSheet);
			}
			excelWriter.finish();

			HttpHeaders headers = new HttpHeaders();
			if (StringUtils.isBlank(fileName)) {
				fileName = IdUtils.randomUUID().toString() + ".xlsx";
			}
			fileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			headers.add("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

			return new ResponseEntity<byte[]>(out.toByteArray(), headers, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(stream); // 解决流关闭问题
		}
	}

	/**
	 * easyexcel导出Excel文件流
	 *
	 * @param templateName 模板名
	 * @param fileName     文件名
	 * @param dataList     数据
	 */
	public ResponseEntity<byte[]> exportEasyexcelStream(String templateName, String fileName,
	                                                    Object dataList, Map<String, Object> map, Integer sheetNo) {
		InputStream stream = null;  //List<Map<String, Object>> dataList
		try {
			// 获取模板文件流
			stream = getTemplate(templateName);
			if (stream == null) {
				R.runError("模板文件流获取异常！"); // 文件流异常
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// 根据模板生成excel
			ExcelWriter excelWriter = EasyExcel.write(out)
					.registerConverter(new SqlDateNumberConverter())
					.registerConverter(new SqlDateStringConverter())
					.registerConverter(new SqlTimestampStringConverter())
					.withTemplate(stream)
					.build();
			WriteSheet writeSheet = EasyExcel.writerSheet(sheetNo).build();

			excelWriter.fill(dataList, writeSheet);
			if (map != null) {
				excelWriter.fill(map, writeSheet);
			}
			excelWriter.finish();

			HttpHeaders headers = new HttpHeaders();
			if (StringUtils.isBlank(fileName)) {
				fileName = IdUtils.randomUUID().toString() + ".xlsx";
			}
			fileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			headers.add("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

			return new ResponseEntity<byte[]>(out.toByteArray(), headers, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(stream); // 解决流关闭问题
		}

	}

	/**
	 * 指定合并单元格
	 * @param templateName
	 * @param fileName
	 * @param map
	 * @param dataList
	 * @param mergeCells
	 * @return
	 */
	public ResponseEntity<byte[]> easyexcelPointMergeCellStream(String templateName, String fileName, Map<String, Object> map,
	                                                             List<Map<String, Object>> dataList, List<CellRangeAddress> mergeCells) {
		//先解析模板，获得key-col对应关系
		InputStream stream = getTemplate(templateName);
		List<Object> list = EasyExcel.read(stream).sheet().doReadSync();

		try {
			stream = getTemplate(templateName);
			if (stream == null) {
				R.runError("模板文件流获取异常！"); // 文件流异常
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(out)
					.registerConverter(new SqlDateNumberConverter())
					.registerConverter(new SqlDateStringConverter())
					.registerConverter(new SqlTimestampStringConverter());
			for (int i = 0; i < mergeCells.size(); i++) {
				int firstRow = mergeCells.get(i).getFirstRow(); // 合并坐标开始
				int lastRow = mergeCells.get(i).getLastRow();  // 合并坐标结束
				if (firstRow == lastRow) {
					continue; // 20210820 调整 合并单元格数量必须大于等于2
				}
				OnceAbsoluteMergeStrategy onceAbsoluteMergeStrategy = new OnceAbsoluteMergeStrategy(firstRow, lastRow, mergeCells.get(i).getFirstColumn(), mergeCells.get(i).getLastColumn());
				excelWriterBuilder.registerWriteHandler(onceAbsoluteMergeStrategy);
			}
			ExcelWriter excelWriter = excelWriterBuilder.withTemplate(stream).build();
			WriteSheet writeSheet = EasyExcel.writerSheet().build();
			excelWriter.fill(dataList, writeSheet);
			excelWriter.fill(map, writeSheet);
			excelWriter.finish();

			HttpHeaders headers = new HttpHeaders();
			fileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			headers.add("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

			return new ResponseEntity<byte[]>(out.toByteArray(), headers, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(stream); // 解决流关闭问题
		}
	}
	
	/**
	 * easyexcel导出Excel文件流，合并单元格
	 *
	 * @param templateName 模板名
	 * @param fileName     文件名
	 * @param dataList     数据
	 * @param keys     需要合并的列,只按照keys的顺序合并
	 */
	public ResponseEntity<byte[]> exportEasyexcelMergeCellStream(String templateName, String fileName, 
			List<Map<String, Object>> dataList,List<String> keys) {
		//先解析模板，获得key-col对应关系
		InputStream stream = getTemplate(templateName);
		List<Object> list = EasyExcel.read(stream).sheet().doReadSync();
		Map<Object,Object> colKeyMap = (Map<Object, Object>) list.get(list.size()-1);
		Map<String,Object> keyColMap = new HashMap<String,Object>();
		for (Map.Entry<Object,Object> entry : colKeyMap.entrySet()) {
			keyColMap.put(entry.getValue().toString(), entry.getKey());
		}
		List<CellRangeAddress> cellRangeAddresss = new ArrayList<CellRangeAddress>();
		mergeMultiCellRange(dataList, keyColMap, cellRangeAddresss, keys, list.size());

		try {
			// 获取模板文件流
			stream = getTemplate(templateName);
			if (stream == null) {
				R.runError("模板文件流获取异常！"); // 文件流异常
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			// 根据模板生成excel
//			ExcelWriter excelWriter = EasyExcel.write(out)
//					.registerConverter(new SqlDateNumberConverter())
//					.registerConverter(new SqlDateStringConverter())
//					.registerConverter(new SqlTimestampStringConverter())
//					.registerWriteHandler(new ExcelPrintMergeStrategy(cols, mergeRowIndex, mergeRowIndex))
//					.withTemplate(stream)
//					.build();
			ExcelWriterBuilder excelWriterBuilder  = EasyExcel.write(out)
			.registerConverter(new SqlDateNumberConverter())
			.registerConverter(new SqlDateStringConverter())
			.registerConverter(new SqlTimestampStringConverter());
			if(cellRangeAddresss!=null && cellRangeAddresss.size()>0) {
				for (int i = 0; i < cellRangeAddresss.size(); i++) {
					int firstRow = cellRangeAddresss.get(i).getFirstRow(); // 合并坐标开始
					int lastRow = cellRangeAddresss.get(i).getLastRow();  // 合并坐标结束
					if(firstRow == lastRow){
						continue; // 20210820 调整 合并单元格数量必须大于等于2
					}
					OnceAbsoluteMergeStrategy onceAbsoluteMergeStrategy = new OnceAbsoluteMergeStrategy(firstRow, lastRow, cellRangeAddresss.get(i).getFirstColumn(), cellRangeAddresss.get(i).getLastColumn());
					excelWriterBuilder.registerWriteHandler(onceAbsoluteMergeStrategy);
				}
			}
			ExcelWriter excelWriter = excelWriterBuilder.withTemplate(stream).build();
			WriteSheet writeSheet = EasyExcel.writerSheet().build();
			excelWriter.fill(dataList, writeSheet);
			excelWriter.finish();

			HttpHeaders headers = new HttpHeaders();
			fileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			headers.add("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

			return new ResponseEntity<byte[]>(out.toByteArray(), headers, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(stream); // 解决流关闭问题
		}
	}

	/**
	 * easyexcel 动态合并单元格
	 * @param dataList 填充的数据
	 * @param keyColMap 从模板中解析的key-col map
	 * @param cellRangeAddresss 要合并的单元格区域
	 * @param keys 指定要合并的列，从左到右
	 * @param headLenth 模板head行数
	 */
	private void mergeMultiCellRange(List<Map<String, Object>> dataList,Map<String,Object> keyColMap, List<CellRangeAddress> cellRangeAddresss, List<String> keys,int headLenth) {
		List<int[]> paramList = new ArrayList<int[]>();
		int[] paramArr = {0,dataList.size()-1};
		paramList.add(paramArr);
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			paramList = mergeOneCellList(dataList, keyColMap, key, cellRangeAddresss, paramList,headLenth);
		}
	}
	
	private List<int[]> mergeOneCellList(List<Map<String, Object>> dataList,Map<String,Object> keyColMap, String key, List<CellRangeAddress> cellRangeAddresss, List<int[]> list,int headLenth) {
		List<int[]> resList = new ArrayList<int[]>();
		for (int i = 0; i < list.size(); i++) {
			int[] arr = list.get(i);
			List<int[]> chArr = mergeOneCell(dataList, keyColMap, key, cellRangeAddresss, arr[0], arr[1],headLenth);
			resList.addAll(chArr);
		}
		return resList;
	}
	
	private List<int[]> mergeOneCell(List<Map<String, Object>> dataList,Map<String,Object> keyColMap, String key, List<CellRangeAddress> cellRangeAddresss,int startRow, int endRow,int headLenth) {
		List<int[]> resList = new ArrayList<int[]>();
		int col = (int) keyColMap.get(String.format("{.%s}", key));
		Object oldValue = dataList.get(startRow).get(key);
		for (int j = (startRow+1); j <= endRow; j++) {
			Object newValue = dataList.get(j).get(key);
			if(oldValue == newValue || oldValue.equals(newValue)) {
				oldValue = newValue;
				if(j==endRow) {
					CellRangeAddress region = new CellRangeAddress(startRow+headLenth, j+headLenth, col, col);
					cellRangeAddresss.add(region);
					int[] resArr = {startRow,j};
					resList.add(resArr);
				}
			}else {
				CellRangeAddress region = new CellRangeAddress(startRow+headLenth, j-1+headLenth, col, col);
				cellRangeAddresss.add(region);
				int[] resArr = {startRow,j-1};
				resList.add(resArr);
	            oldValue = newValue;
				startRow=j;
			}
		}
		return resList;
	}
	
	/**
	 * easyexcel导出Excel文件流 根据 head和data生成excel文件流
	 *
	 * @param fileName 文件名
	 * @param sheetName sheet名
	 * @param head     表头数据
	 * @param data     表体数据
	 */
	public ResponseEntity<byte[]> exportEasyexcelStream(String fileName, String sheetName, List<List<String>> head, List<List<Object>> data) {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();

			HorizontalCellStyleStrategy horizontalCellStyleStrategy = getHorizontalCellStyleStrategy();

			EasyExcel.write(out)
					.registerConverter(new SqlDateNumberConverter())
					.registerConverter(new SqlDateStringConverter())
					.registerConverter(new SqlTimestampStringConverter())
					.head(head).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(data);

			HttpHeaders headers = new HttpHeaders();
			fileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			headers.add("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

			return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(out); // 解决流关闭问题
		}
	}

	/**
	 * easyexcel导出Excel文件流 根据 head和data生成excel文件流
	 *
	 * @param fileName 文件名
	 * @param sheetName sheet名
	 * @param head     表头数据
	 * @param data     表体数据
	 */
	public String exportToExcel(String fileName, String sheetName, List<List<String>> head, List<List<Object>> data) {
		try {
			HorizontalCellStyleStrategy horizontalCellStyleStrategy = getHorizontalCellStyleStrategy();

			EasyExcel.write(fileName)
					.registerConverter(new SqlDateNumberConverter())
					.registerConverter(new SqlDateStringConverter())
					.registerConverter(new SqlTimestampStringConverter())
					.head(head).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(data);

			return fileName;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<List<String>> getHead(String... headArr) {
		List<List<String>> heads = new ArrayList<>();

		for (String itemHead : headArr) {
			List<String> head = new ArrayList<>();
			head.add(itemHead);

			heads.add(head);
		}

		return heads;
	}

	private HorizontalCellStyleStrategy getHorizontalCellStyleStrategy() {
		WriteCellStyle headWriteCellStyle = new WriteCellStyle();
		headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		WriteFont headWriteFont = new WriteFont();
		headWriteFont.setFontHeightInPoints((short) 11);
		headWriteCellStyle.setWriteFont(headWriteFont);

		WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
		contentWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
		contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
		contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
		contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
		contentWriteCellStyle.setBorderTop(BorderStyle.THIN);

		return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
	}

}
