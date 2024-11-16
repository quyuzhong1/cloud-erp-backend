package com.common.core.excel;

import cn.hutool.core.lang.Pair;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.converters.ConverterKeyBuild;
import com.alibaba.excel.converters.bytearray.ByteArrayImageConverter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.util.IoUtils;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.common.core.dto.ExcelData;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.listener.EasyExcelListener;
import com.common.core.utils.IdUtils;
import com.common.core.utils.R;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 导出Excel 模板
 *
 * @author egbert
 */
public class ExcelPrintUtils {
	private static final Log log = LogFactory.getLog(ExcelPrintUtils.class);



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
		final String JXLS_EXCEL = "excel/";
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

	public <T> byte[] exportDynamicHeadersExcel(String sheetName, List<List<String>> head, List<List<T>> data) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();){
			HorizontalCellStyleStrategy horizontalCellStyleStrategy = getHorizontalCellStyleStrategy();
			EasyExcelFactory.write(out)
					.registerConverter(new SqlDateNumberConverter())
					.registerConverter(new SqlDateStringConverter())
					.registerConverter(new SqlTimestampStringConverter())
					.head(head).registerWriteHandler(horizontalCellStyleStrategy).registerWriteHandler(new ExcelCellWidthStyleStrategy()).sheet(sheetName).doWrite(data);
			return out.toByteArray();
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
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


	/**
	 * 复制模板导出excel数据
	 * @Author Luo_WG
	 * @Date 2022/10/10 12:08
	 * @param list 数据集
	 * @param response response
	 * @param excelPath 模板路径
	 * @return void
	 **/
	public void patchExport(List<?> list, HttpServletResponse response, String fileName, String excelPath) throws IOException {
		OutputStream out = null;
		BufferedOutputStream bos = null;
		try {
			//模板的路径
			ClassPathResource classPathResource = new ClassPathResource(excelPath);
			InputStream inputStream = classPathResource.getInputStream();
			getOutputStream(fileName, response);
			out = response.getOutputStream();
			bos = new BufferedOutputStream(out);
			ExcelWriter excelWriter = EasyExcel.write(bos).withTemplate(inputStream).build();
			// LocalDate转化器，导入导出都可以使用
			LocalDateTimeConverter converter = new LocalDateTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

			// LocalDateTime转化器，导入导出都可以使用
			EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
			// LocalDate转化器，导入导出都可以使用
			EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);
			// list转化器，导入导出都可以使用
			EasyExcelListConverter listConverter = new EasyExcelListConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(listConverter.supportJavaTypeKey()), listConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(listConverter.supportJavaTypeKey(), listConverter.supportExcelTypeKey()), listConverter);
			// 图片转换器
			ByteArrayImageConverter byteArrayImageConverter = new ByteArrayImageConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(byteArrayImageConverter.supportJavaTypeKey()), byteArrayImageConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(byteArrayImageConverter.supportJavaTypeKey(), byteArrayImageConverter.supportExcelTypeKey()), byteArrayImageConverter);

			WriteSheet writeSheet = EasyExcel.writerSheet().build();

			//列表数据
			excelWriter.fill(list, writeSheet);
			excelWriter.finish();

		} catch (Exception e) {
			e.printStackTrace();
			log.info("导出模板数据异常！>>>{}" ,e);
		} finally {
			out.flush();
			out.close();
			bos.flush();
		}
	}

	public byte[] patchExport(List<?> list, String excelPath, WriteHandler... writeHandlers) throws IOException {
		ClassPathResource classPathResource = new ClassPathResource(excelPath);
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			 //模板的路径
			 InputStream inputStream = classPathResource.getInputStream()) {
			ExcelWriterBuilder excelWriterBuilder = EasyExcelFactory.write(outputStream).withTemplate(inputStream).autoCloseStream(true);
			for (WriteHandler handler : writeHandlers) {
				excelWriterBuilder.registerWriteHandler(handler);
			}
			ExcelWriter excelWriter = excelWriterBuilder.build();
			// LocalDate转化器，导入导出都可以使用
			LocalDateTimeConverter converter = new LocalDateTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

			// LocalDateTime转化器，导入导出都可以使用
			EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
			// LocalDate转化器，导入导出都可以使用
			EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);
			// list转化器，导入导出都可以使用
			EasyExcelListConverter listConverter = new EasyExcelListConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(listConverter.supportJavaTypeKey()), listConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(listConverter.supportJavaTypeKey(), listConverter.supportExcelTypeKey()), listConverter);
			WriteSheet writeSheet = EasyExcelFactory.writerSheet().build();
			//列表数据
			excelWriter.fill(list, writeSheet);
			excelWriter.finish();
			return outputStream.toByteArray();
		}
	}

	/**
	 * @description: 导出多个sheet页
	 * @author Will
	 * @date: 2023/11/9 10:36
	 * @param pairList
	 * @param response
	 * @param fileName
	 * @param excelPath
	 */
	public void sheetPatchExport(List<Pair<Integer,List<?>>> pairList , HttpServletResponse response, String fileName, String excelPath) throws IOException {
		OutputStream out = null;
		BufferedOutputStream bos = null;
		try {
			//模板的路径
			ClassPathResource classPathResource = new ClassPathResource(excelPath);
			InputStream inputStream = classPathResource.getInputStream();
			getOutputStream(fileName, response);
			out = response.getOutputStream();
			bos = new BufferedOutputStream(out);
			ExcelWriter excelWriter = EasyExcel.write(bos).withTemplate(inputStream).build();
			// LocalDate转化器，导入导出都可以使用
			LocalDateTimeConverter converter = new LocalDateTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

			// LocalDateTime转化器，导入导出都可以使用
			EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
			// LocalDate转化器，导入导出都可以使用
			EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);
			for (Pair<Integer,List<?>> pair : pairList) {
				WriteSheet writeSheet = EasyExcel.writerSheet(pair.getKey()).build();
				//列表数据
				excelWriter.fill(pair.getValue(), writeSheet);
			}
			excelWriter.finish();

		} catch (Exception e) {
			e.printStackTrace();
			log.info("导出模板数据异常！>>>{}" ,e);
		} finally {
			out.flush();
			out.close();
			bos.flush();
		}
	}

	public byte[] sheetPatchExport(List<Pair<Integer,List<?>>> pairList , String fileName, String excelPath) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
			 BufferedOutputStream bos = new BufferedOutputStream(out)) {
			//模板的路径
			ClassPathResource classPathResource = new ClassPathResource(excelPath);
			InputStream inputStream = classPathResource.getInputStream();
			ExcelWriter excelWriter = EasyExcel.write(bos).withTemplate(inputStream).build();
			// LocalDate转化器，导入导出都可以使用
			LocalDateTimeConverter converter = new LocalDateTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

			// LocalDateTime转化器，导入导出都可以使用
			EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
			// LocalDate转化器，导入导出都可以使用
			EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);
			for (Pair<Integer,List<?>> pair : pairList) {
				WriteSheet writeSheet = EasyExcel.writerSheet(pair.getKey()).build();
				//列表数据
				excelWriter.fill(pair.getValue(), writeSheet);
			}
			excelWriter.finish();
			return out.toByteArray();
		} catch (Exception e) {
			throw new ServiceException(ApiError.ERROR_1015);
		}
	}

	/**
	 * 多组合填充
	 * @author yl
	 * @date 2023-08-11 14:28
	 * @param map 数据集合
	 * @return void
	 */
	public void compositeFillExport(Map<String,Object> map, HttpServletResponse response, String fileName, String excelPath) throws IOException {
		OutputStream out = null;
		BufferedOutputStream bos = null;
		try {
			//模板的路径
			ClassPathResource classPathResource = new ClassPathResource(excelPath);
			InputStream inputStream = classPathResource.getInputStream();
			getOutputStream(fileName, response);
			out = response.getOutputStream();
			bos = new BufferedOutputStream(out);
			ExcelWriter excelWriter = EasyExcel.write(bos).withTemplate(inputStream).build();
			// LocalDate转化器，导入导出都可以使用
			LocalDateTimeConverter converter = new LocalDateTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

			// LocalDateTime转化器，导入导出都可以使用
			EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
			// LocalDate转化器，导入导出都可以使用
			EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);
			WriteSheet writeSheet = EasyExcel.writerSheet().build();

			for (Map.Entry<String, Object> item : map.entrySet()) {
				//列表数据
				excelWriter.fill(new FillWrapper(item.getKey(), (Collection) item.getValue()),writeSheet);
			}
			excelWriter.finish();

		} catch (Exception e) {
			e.printStackTrace();
			log.info("导出模板数据异常！");
		} finally {
			out.flush();
			out.close();
			bos.flush();
		}
	}


	/**
	 * @description: 多组合填充
	 * @author Will
	 * @date: 2024/2/22 15:58
	 * @param map
	 * @param obj
	 * @param response
	 * @param fileName
	 * @param excelPath
	 */
	public <T> void compositeFillExport(Map<String,Object> map, T obj, HttpServletResponse response, String fileName, String excelPath) throws IOException {
		OutputStream out = null;
		BufferedOutputStream bos = null;
		try {
			//模板的路径
			ClassPathResource classPathResource = new ClassPathResource(excelPath);
			InputStream inputStream = classPathResource.getInputStream();
			getOutputStream(fileName, response);
			out = response.getOutputStream();
			bos = new BufferedOutputStream(out);
			ExcelWriter excelWriter = EasyExcel.write(bos).withTemplate(inputStream).build();
			// LocalDate转化器，导入导出都可以使用
			LocalDateTimeConverter converter = new LocalDateTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

			// LocalDateTime转化器，导入导出都可以使用
			EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
			// LocalDate转化器，导入导出都可以使用
			EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);
			WriteSheet writeSheet = EasyExcel.writerSheet().build();

			for (Map.Entry<String, Object> item : map.entrySet()) {
				//列表数据
				excelWriter.fill(new FillWrapper(item.getKey(), (Collection) item.getValue()),writeSheet);
			}
			if (obj != null) {
				excelWriter.fill(obj , writeSheet);
			}
			excelWriter.finish();

		} catch (Exception e) {
			e.printStackTrace();
			log.info("导出模板数据异常！");
		} finally {
			out.flush();
			out.close();
			bos.flush();
		}
	}

	public static void exportZipStream(List<ExcelData> excelDataList, HttpServletResponse response, String excelPath,String zipName) {
		try {
			// 开始存入
			OutputStream outputStream = ExcelPrintUtils.getZipOutputStream(zipName, response);
			try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
				try {
					for (ExcelData excelData : excelDataList) {
						ClassPathResource classPathResource = new ClassPathResource(excelPath);
						InputStream inputStream = classPathResource.getInputStream();
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						ExcelWriter excelWriter = EasyExcel.write(byteArrayOutputStream).withTemplate(inputStream).registerWriteHandler(new ExcelFillCellMergeStrategy()).build();
						// LocalDate转化器，导入导出都可以使用
						LocalDateTimeConverter converter = new LocalDateTimeConverter();
						excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
						excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

						// LocalDateTime转化器，导入导出都可以使用
						EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
						excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
						excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
						// LocalDate转化器，导入导出都可以使用
						EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
						excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
						excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);

						WriteSheet writeSheet = EasyExcel.writerSheet().build();
						FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
						//列表数据
						excelWriter.fill(excelData.getDetailList(), fillConfig , writeSheet);

						if (excelData.getData() != null) {
							excelWriter.fill(excelData.getData() , writeSheet);
						}
						excelWriter.finish();
						zipOut.putNextEntry(new ZipEntry(excelData.getFilename()));
						zipOut.write(byteArrayOutputStream.toByteArray());
						zipOut.closeEntry();
					}
				} catch (Exception e) {
					throw new RuntimeException("导出Excel异常", e);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("导出Excel异常", e);
		}
	}

	/**
	 * 方法说明
	 * @author yl
	 * @date 2023-07-05 10:01
	 * @param list
	 * @param obj
	 * @param response
	 * @param fileName
	 * @param excelPath
	 * @return void
	 */
	public <T> void patchExport(List<?> list, T obj, HttpServletResponse response, String fileName, String excelPath) throws IOException {
		OutputStream out = null;
		BufferedOutputStream bos = null;
		try {
			//模板的路径
			ClassPathResource classPathResource = new ClassPathResource(excelPath);
			InputStream inputStream = classPathResource.getInputStream();
			getOutputStream(fileName, response);
			out = response.getOutputStream();
			bos = new BufferedOutputStream(out);
			ExcelWriter excelWriter = EasyExcel.write(bos).withTemplate(inputStream).registerWriteHandler(new ExcelFillCellMergeStrategy()).build();
			// LocalDate转化器，导入导出都可以使用
			LocalDateTimeConverter converter = new LocalDateTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

			// LocalDateTime转化器，导入导出都可以使用
			EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
			// LocalDate转化器，导入导出都可以使用
			EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);

			WriteSheet writeSheet = EasyExcel.writerSheet().build();
			FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
			//列表数据
			excelWriter.fill(list, fillConfig , writeSheet);

			if (obj != null) {
				excelWriter.fill(obj , writeSheet);
			}
			excelWriter.finish();
		} catch (Exception e) {
			e.printStackTrace();
			log.info("导出模板数据异常！");
		} finally {
			out.flush();
			out.close();
			bos.flush();
		}
	}

	public <T> byte[] patchExport(List<?> list, T obj, String excelPath) throws IOException {
		//模板的路径
		ClassPathResource classPathResource = new ClassPathResource(excelPath);
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			 //模板的路径
			 InputStream inputStream = classPathResource.getInputStream()) {
			ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(inputStream).registerWriteHandler(new ExcelFillCellMergeStrategy()).build();
			// LocalDate转化器，导入导出都可以使用
			LocalDateTimeConverter converter = new LocalDateTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

			// LocalDateTime转化器，导入导出都可以使用
			EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
			// LocalDate转化器，导入导出都可以使用
			EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);

			WriteSheet writeSheet = EasyExcel.writerSheet().build();
			FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
			//列表数据
			excelWriter.fill(list, fillConfig, writeSheet);

			if (obj != null) {
				excelWriter.fill(obj, writeSheet);
			}
			excelWriter.finish();
			return outputStream.toByteArray();
		}
	}

	public void patchSheetExport(List<?> list1, List<?> list2, HttpServletResponse response, String fileName, String excelPath) throws IOException{
		OutputStream out = null;
		BufferedOutputStream bos = null;
		try {
			//模板的路径
			ClassPathResource classPathResource = new ClassPathResource(excelPath);
			InputStream inputStream = classPathResource.getInputStream();
			getOutputStream(fileName, response);
			out = response.getOutputStream();
			bos = new BufferedOutputStream(out);
			ExcelWriter excelWriter = EasyExcel.write(bos).withTemplate(inputStream).build();
			// LocalDate转化器，导入导出都可以使用
			LocalDateTimeConverter converter = new LocalDateTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey()), converter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()), converter);

			// LocalDateTime转化器，导入导出都可以使用
			EasyExcelLocalTimeConverter localDateTimeDateConverter = new EasyExcelLocalTimeConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey()), localDateTimeDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateTimeDateConverter.supportJavaTypeKey(), localDateTimeDateConverter.supportExcelTypeKey()), localDateTimeDateConverter);
			// LocalDate转化器，导入导出都可以使用
			EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
			excelWriter.writeContext().currentWriteHolder().converterMap().put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);
			WriteSheet writeSheet1 = EasyExcel.writerSheet(0).build();
			//列表数据
			excelWriter.fill(list1, writeSheet1);
			WriteSheet writeSheet2 = EasyExcel.writerSheet(1).build();
			//列表数据
			excelWriter.fill(list2, writeSheet2);
			excelWriter.finish();

		} catch (Exception e) {
			e.printStackTrace();
			log.info("导出模板数据异常！");
		} finally {
			out.flush();
			out.close();
			bos.flush();
		}
	}

	/**
	 * 导出模板
	 * @Author Luo_WG
	 * @Date 2022/10/14 14:06
	 * @param path excel模板路径
	 * @param excelName excel名称
	 * @param request request
	 * @param response response
	 * @return void
	 **/
	public void exportTemplate(String path, String excelName, HttpServletRequest request, HttpServletResponse response) {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		try {
			InputStream inputStream = resourceLoader.getResource(path).getInputStream();
			XSSFWorkbook wb = new XSSFWorkbook(inputStream);
			// 输出Excel文件
			OutputStream output = response.getOutputStream();
			response.reset();
			// 设置文件头
			response.setHeader("Content-Disposition",
					"attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
			response.setContentType("application/msexcel");
			wb.write(output);
			wb.close();
		} catch (Exception e) {
			log.error("exportTemplate ", e);
		}

	}

	/**
	 * 这是ExcelUtil.getOutputStream
	 * @Author Luo_WG
	 * @Date 2022/10/10 11:27
	 * @param fileName fileName
	 * @param response response
	 * @return java.io.OutputStream
	 **/
	public static OutputStream getOutputStream(String fileName, HttpServletResponse response) throws Exception {
		// 这里文件名如果涉及中文一定要使用URL编码,否则会乱码
		String exportFileName = URLEncoder.encode(fileName+ ExcelTypeEnum.XLSX.getValue(), StandardCharsets.UTF_8.toString());
		//response.setContentType("application/force-download");
		response.setHeader("Content-Disposition", "attachment;filename=" + exportFileName);
		//response.setContentType("application/json;charset=utf-8");
		response.setContentType("application/octet-stream");
		//导出的文件名
//        String excelFileName = URLEncoder.encode(fileName, "utf-8");
//        response.setHeader("Content-disposition", "attachment; filename=" + new String(excelFileName.getBytes("UTF-8"), "ISO-8859-1"));
		return response.getOutputStream();
	}
	public static OutputStream getZipOutputStream(String fileName, HttpServletResponse response) throws Exception {
		// 这里文件名如果涉及中文一定要使用URL编码,否则会乱码
		String exportFileName = URLEncoder.encode(fileName+ ".zip", StandardCharsets.UTF_8.toString());
		//response.setContentType("application/force-download");
		response.setHeader("Content-Disposition", "attachment;filename=" + exportFileName);
		//response.setContentType("application/json;charset=utf-8");
		response.setContentType("application/octet-stream");
		//导出的文件名
//        String excelFileName = URLEncoder.encode(fileName, "utf-8");
//        response.setHeader("Content-disposition", "attachment; filename=" + new String(excelFileName.getBytes("UTF-8"), "ISO-8859-1"));
		return response.getOutputStream();
	}
	/**
	 * 动态获取全部列和数据体
	 */
	public static List<Map<String,String>> parseExcelToData(byte[] stream, Integer parseRowNumber) {
		EasyExcelListener readListener = new EasyExcelListener();
		EasyExcelFactory.read(new ByteArrayInputStream(stream)).registerReadListener(readListener).headRowNumber(parseRowNumber).sheet(0).doRead();
		List<Map<Integer, String>> headList = readListener.getHeadList();
		if(CollectionUtils.isEmpty(headList)){
			throw new RuntimeException("Excel表头不能为空");
		}
		List<Map<Integer, String>> dataList = readListener.getDataList();
		if(CollectionUtils.isEmpty(dataList)){
			throw new RuntimeException("Excel数据内容不能为空");
		}
		//获取头部,取最后一次解析的列头数据
		Map<Integer, String> excelHeadIdxNameMap = headList.get(headList.size() -1);
		//封装数据体
		List<Map<String,String>> excelDataList = Lists.newArrayList();
		for (Map<Integer, String> dataRow : dataList) {
			Map<String,String> rowData = new LinkedHashMap<>();
			excelHeadIdxNameMap.entrySet().forEach(columnHead -> {
				rowData.put(columnHead.getValue(), dataRow.get(columnHead.getKey()));
			});
			excelDataList.add(rowData);
		}
		return excelDataList;
	}

	/**
	 * 返回导入的所有数据
	 */
	public static List<Map<String,String>> makeData(MultipartFile file){
		InputStream inputStream = null;//转换成输入流
		try {
			inputStream = file.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] stream = new byte[0];
		try {
			stream = IoUtils.toByteArray(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(stream == null || stream.length == 0){
			return null;
		}
		List<Map<String,String>> dataList = parseExcelToData(stream, 1);//从动态获取全部列和数据体，默认从第一行开始解析数据
		try {
			if(inputStream != null){
				inputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataList;
	}
	
	public static List<Map<String,String>> parseExcelToAllSheetData(InputStream inputStream, Integer parseRowNumber) {
		EasyExcelListener readListener = new EasyExcelListener();
		EasyExcelFactory.read(inputStream).registerReadListener(readListener).headRowNumber(parseRowNumber).doReadAll();
		List<Map<Integer, String>> headList = readListener.getHeadList();
		if(CollectionUtils.isEmpty(headList)){
			throw new ServiceException("Excel表头行不能为空");
		}
		//获取头部,取最后一次解析的列头数据
		Map<Integer, String> excelHeadIdxNameMap = headList.get(0);
		Set<String> headSet = new HashSet<>();
		for(Map.Entry<Integer, String> headMap : excelHeadIdxNameMap.entrySet()) {
			Integer key = headMap.getKey();
			Integer index = key + 1;
			String head = headMap.getValue();
			if(StringUtils.isBlank(head)) {
				throw new ServiceException("Excel表头第" + index +"列为空值");
			}
			if(headSet.contains(head)) {
				throw new ServiceException("Excel表头行含有重复值：" + head);
			}
			headSet.add(head);
			if(headList.size() > 1) {
				for(int i = 1; i < headList.size(); i++) {
					Map<Integer, String> tempMaps = headList.get(i);
					String tempHead = tempMaps.get(key);
					if(StringUtils.isNotEmpty(head) && StringUtils.isEmpty(tempHead)) {
						throw new ServiceException("Excel第"+ (i + 1) +"个sheet页表头行第"+ index +"列值["+ tempHead +"]，与第1个sheet页表头行的第"+ index +"列值["+ head +"]，不一致，请检查");
					}else if(StringUtils.isEmpty(head) && StringUtils.isNotEmpty(tempHead)) {
						throw new ServiceException("Excel第"+ (i + 1) +"个sheet页表头行第"+ index +"列值["+ tempHead +"]，与第1个sheet页表头行的第"+ index +"列值["+ head +"]，不一致，请检查");
					}else if(StringUtils.isNotEmpty(head) && StringUtils.isNotEmpty(tempHead) && !head.equals(tempHead)) {
						throw new ServiceException("Excel第"+ (i + 1) +"个sheet页表头行第"+ index +"列值["+ tempHead +"]，与第1个sheet页表头行的第"+ index +"列值["+ head +"]，不一致，请检查");
					}
				}
			}
		}
		
		List<Map<Integer, String>> dataList = readListener.getDataList();
		if(CollectionUtils.isEmpty(dataList)){
			throw new RuntimeException("Excel数据内容不能为空");
		}
		//封装数据体
		List<Map<String,String>> excelDataList = Lists.newArrayList();
		for (Map<Integer, String> dataRow : dataList) {
			Map<String,String> rowData = new LinkedHashMap<>();
			excelHeadIdxNameMap.entrySet().forEach(columnHead -> {
				rowData.put(columnHead.getValue(), dataRow.get(columnHead.getKey()));
			});
			excelDataList.add(rowData);
		}
		return excelDataList;
	}
	
	public static List<Map<String,String>> makeDataInputStream(InputStream inputStream){
		List<Map<String,String>> dataList = parseExcelToAllSheetData(inputStream, 1);//从动态获取全部列和数据体，默认从第一行开始解析数据
		try {
			if(inputStream != null){
				inputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataList;
	}

	public static void main(String[] args) throws Exception{
		String filePath = "C:\\Users\\Administrator\\Desktop\\新建 XLS 工作表.xls";
		InputStream inputStream = new FileInputStream(filePath);
		List<Map<String, String>> makeDataInputStream = makeDataInputStream(inputStream);
		System.out.println(makeDataInputStream);
	}
}
