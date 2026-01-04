package com.erp.server.wms.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ZipUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WmsDataCompareUtils {
	@Data
	public static class WmsDataCompareExcelDto{
		private List<List<String>> headFieldLists = new ArrayList<>();
		private List<Map<String, String>> datas = new ArrayList<>();
		private Integer importDataCount = 0;
	}
	
	public static WmsDataCompareExcelDto getWmsDataCompareExcelDto(List<String> excelFiles) {
		WmsDataCompareExcelDto dto = new WmsDataCompareExcelDto();
		if(CollUtil.isEmpty(excelFiles)) {
			return dto;
		}
		int i = 1;
        for(String excelFile : excelFiles) {
        	log.warn("数据对比解析第{}个文件开始：{}" , i , excelFile);
        	InputStream inputStream = null;
        	try {
				inputStream = FastDFSClientUtil.getInputStream(excelFile);
			} catch (Exception e) {
				log.error("获取文件失败" , e);
				throw new ServiceException("获取文件失败");
			}
        	List<Map<String, String>> makeDataInputStream = null;
        	try {
        		makeDataInputStream = ExcelPrintUtils.makeDataInputStream(inputStream);
			}catch (ServiceException e) {
				throw e;
			}catch (Exception e) {
				log.error("读取excel失败" , e);
				throw new ServiceException("读取excel失败");
			}
        	
        	if(CollUtil.isNotEmpty(makeDataInputStream)) {
        		Map<String, String> map = makeDataInputStream.get(0);
        		List<String> headFields = new ArrayList<>();
        		for(Map.Entry<String, String> m : map.entrySet()) {
        			headFields.add(m.getKey());
        		}
        		dto.getHeadFieldLists().add(headFields);
        		dto.getDatas().addAll(makeDataInputStream);
        	}
        	log.warn("数据对比解析第{}个文件结束：{}" , i , excelFile);
        	i = i + 1;
        }
        dto.setImportDataCount(dto.getDatas().size());
        return dto;
	}
	
	public static String mergeExcel(List<String> excelFiles) {
		int size = excelFiles.size();
        if(size > 1) {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
        	InputStream[] ins = new InputStream[size];
        	for(int i = 0; i < size ; i++) {
        		InputStream inputStream = null;
    			try {
    				inputStream = FastDFSClientUtil.getInputStream(excelFiles.get(i));
    			} catch (Exception e) {
    				log.error("获取文件失败" , e);
    				throw new ServiceException("获取文件失败");
    			}
    			ins[i] = inputStream;
        	}
        	ZipUtil.zip(zipOutputStream, excelFiles.toArray(new String[] {}), ins);
        	excelFiles = new ArrayList<>();
        	return FastDFSClientUtil.uploadFile(baos.toByteArray(), UUID.randomUUID().toString() + ".zip", null);
        }else {
        	return excelFiles.get(0);
        }
	}
	
	public static String convertToCamel(String underlineName) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;

        for (int i = 0; i < underlineName.length(); i++) {
            char currentChar = underlineName.charAt(i);

            if (currentChar == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(currentChar));
                    capitalizeNext = false;
                } else {
                    sb.append(Character.toLowerCase(currentChar));
                }
            }
        }
        return sb.toString();
    }
	
	public static WmsDataCompareExcelDto getOnlyHeadAndCount(List<String> excelFiles) {
		WmsDataCompareExcelDto dto = new WmsDataCompareExcelDto();
		if(CollUtil.isEmpty(excelFiles)) {
			return dto;
		}
		for(String excelFile : excelFiles) {
			InputStream inputStream = null;
			try {
				inputStream = FastDFSClientUtil.getInputStream(excelFile);
			} catch (Exception e) {
				log.error("获取文件失败" , e);
				throw new ServiceException("获取文件失败");
			}
			try {
				Workbook workbook = WorkbookFactory.create(inputStream);
				Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
				// 读取表头
	            Row headerRow = sheet.getRow(0);
	            dto.getHeadFieldLists().add(getCellValues(headerRow));
	            int lastRowNum = sheet.getLastRowNum();
				dto.setImportDataCount(dto.getImportDataCount() + lastRowNum);
				
			} catch (Exception e) {
				log.error("读取excel失败" , e);
				throw new ServiceException("读取excel失败");
			}
			
		}
		return dto;
	}
	
	private static List<String> getCellValues(Row row){
		List<String> cellValues = new ArrayList<>();
		int i = 0;
		for (Cell cell : row) {
            String cellValue = cell.toString();
            switch (cell.getCellType()) {
                case STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case NUMERIC:
                	double numericCellValue = cell.getNumericCellValue();
                	if(DateUtil.isCellDateFormatted(cell)) {
                		cellValue = cn.hutool.core.date.DateUtil.format(DateUtil.getJavaDate(numericCellValue), "yyyy-MM-dd");
                	}else {
                		if(numericCellValue == Math.floor(numericCellValue)) {
                			cellValue = String.valueOf((int)numericCellValue);
                		}else {
                			cellValue = String.valueOf(numericCellValue);
                		}
                	}
                    break;
                case BOOLEAN:
                    cellValue = String.valueOf(cell.getBooleanCellValue());
                    break;
                case FORMULA:
                    cellValue = cell.getCellFormula();
                    break;
                case BLANK:
                	cellValue = "";
                default:
                    cellValue = ""; // 其他类型为空字符串
            }
            while(cell.getColumnIndex() > i) {
            	i = i + 1;
            	cellValues.add(null);
            }
            i = i + 1;
            cellValues.add(cellValue);
        }
		return cellValues;
	}
	
	public static boolean areAllListsEqual(List<List<String>> listOfLists) {
	    if (listOfLists == null || listOfLists.isEmpty()) {
	        return false;
	    }

	    List<String> firstList = listOfLists.get(0);

	    for (int i = 1; i < listOfLists.size(); i++) {
	        List<String> currentList = listOfLists.get(i);

	        if (!areListsEqual(firstList, currentList)) {
	            return false;
	        }
	    }

	    return true;
	}

	public static boolean areListsEqual(List<String> list1, List<String> list2) {
	    if (list1.size() != list2.size()) {
	        return false;
	    }

	    for (int i = 0; i < list1.size(); i++) {
	        if (!list1.get(i).equals(list2.get(i))) {
	            return false;
	        }
	    }

	    return true;
	}
	
	// 自定义合并策略
   public static class MergeStrategy extends AbstractMergeStrategy {
        private int headerSize;
        
        private List<String> diffIndexs;
        
        private Integer currCellStyleCount = 0;
        
        public MergeStrategy(int headerSize , List<String> diffIndexs) {
            this.headerSize = headerSize;
            this.diffIndexs = diffIndexs;
        }

		@Override
		protected void merge(org.apache.poi.ss.usermodel.Sheet sheet, org.apache.poi.ss.usermodel.Cell cell, Head head,
				Integer relativeRowIndex) {
			Workbook workbook = sheet.getWorkbook();
			int rowIndex = cell.getRowIndex();
			int columnIndex = cell.getColumnIndex();
			if(currCellStyleCount > 60000) {
				return;
			}
			String key = rowIndex + "-" + columnIndex;
			
			CellStyle cellStyle = null;
			
//			// 设置边框样式
//			cellStyle.setBorderBottom(BorderStyle.THIN);
//			cellStyle.setBorderTop(BorderStyle.THIN);
//			cellStyle.setBorderLeft(BorderStyle.THIN);
//			cellStyle.setBorderRight(BorderStyle.THIN);
			
			if(diffIndexs.stream().anyMatch(d -> key.equals(d))) {
				if(cellStyle == null) {
					cellStyle = workbook.createCellStyle();
					currCellStyleCount = currCellStyleCount + 1;
				}
				Font font = workbook.createFont();
		        font.setColor(Font.COLOR_RED);
				cellStyle.setFont(font);
			}
			
			if (rowIndex == 0 && columnIndex == 0) {
				if(cellStyle == null) {
					cellStyle = workbook.createCellStyle();
					currCellStyleCount = currCellStyleCount + 1;
				}
				sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
				cellStyle.setAlignment(HorizontalAlignment.CENTER);
		        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			}
			if (rowIndex == 0 && columnIndex == 1) {
				if(cellStyle == null) {
					cellStyle = workbook.createCellStyle();
					currCellStyleCount = currCellStyleCount + 1;
				}
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, headerSize));
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
    	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            }
			if (rowIndex == 0 && columnIndex == (headerSize + 1)) {
				if(cellStyle == null) {
					cellStyle = workbook.createCellStyle();
					currCellStyleCount = currCellStyleCount + 1;
				}
                sheet.addMergedRegion(new CellRangeAddress(0, 0, headerSize + 1, headerSize*2));
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
    	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            }
			
			cell.setCellStyle(cellStyle);
		}
		
    }
   
}
