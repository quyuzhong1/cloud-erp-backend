package com.erp.server.wms.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.wms.dto.WmsDataComparePlanDTO.ImportDataMappingDTO;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WmsDataCompareUtils {
	@Data
	public static class WmsDataCompareExcelDto{
		public Integer importDataCount = 0;
		public List<List<String>> headFieldLists = new ArrayList<>();
		public List<List<String>> datas = new ArrayList<>();
	}
	
	public static WmsDataCompareExcelDto getWmsDataCompareExcelDto(List<String> excelFiles) {
		WmsDataCompareExcelDto dto = new WmsDataCompareExcelDto();
		if(CollUtil.isEmpty(excelFiles)) {
			return dto;
		}
		Integer importDataCount = 0;
        for(String excelFile : excelFiles) {
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
			} catch (Exception e) {
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
        		
        		importDataCount = importDataCount + makeDataInputStream.size();
        		
        		for(Map<String, String> makeData : makeDataInputStream) {
        			List<String> data = new ArrayList<>();
        			for(Map.Entry<String, String> m : makeData.entrySet()) {
        				data.add(m.getValue());
        			}
        			dto.getDatas().add(data);
        		}
        	}
        }
        dto.setImportDataCount(importDataCount);
        return dto;
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
	
	public static void compareExcelIndexList(List<ImportDataMappingDTO> importDataMappingDTOList , List<String> headFieldList){
		importDataMappingDTOList.removeIf(i -> StringUtils.isBlank(i.getSystemField()) || StringUtils.isBlank(i.getImportField()));
		for(int i = 0; i < headFieldList.size() ; i++) {
			String headField = headFieldList.get(i);
			for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
				if(StringUtils.isNotBlank(importDataMappingDTO.getImportField()) && importDataMappingDTO.getImportField().equals(headField)) {
					importDataMappingDTO.setHeadIndex(i);
					break;
				}
			}
		}
		importDataMappingDTOList.sort((i1 , i2) -> {
			if(i1.getHeadIndex() == null) {
				return -1;
			}
			if(i2.getHeadIndex() == null) {
				return 1;
			}
			return i1.getHeadIndex().compareTo(i2.getHeadIndex());
		});
	}
	
	// 自定义合并策略
   public static class MergeStrategy extends AbstractMergeStrategy {
        private int headerSize;
        
        private List<String> diffIndexs;

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
			String key = rowIndex + "-" + columnIndex;
			
			CellStyle cellStyle = workbook.createCellStyle();
			
			// 设置边框样式
			cellStyle.setBorderBottom(BorderStyle.THIN);
			cellStyle.setBorderTop(BorderStyle.THIN);
			cellStyle.setBorderLeft(BorderStyle.THIN);
			cellStyle.setBorderRight(BorderStyle.THIN);
			
			if(diffIndexs.stream().anyMatch(d -> key.equals(d))) {
				Font font = workbook.createFont();
		        font.setColor(Font.COLOR_RED);
				cellStyle.setFont(font);
			}
			
			if (rowIndex == 0 && columnIndex == 0) {
				sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
				cellStyle.setAlignment(HorizontalAlignment.CENTER);
		        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			}
			if (rowIndex == 0 && columnIndex == 1) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, headerSize));
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
    	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            }
			if (rowIndex == 0 && columnIndex == (headerSize + 1)) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, headerSize + 1, headerSize*2));
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
    	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            }
			cell.setCellStyle(cellStyle);
		}
		
    }
   
}
