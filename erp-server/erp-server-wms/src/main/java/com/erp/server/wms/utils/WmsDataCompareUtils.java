package com.erp.server.wms.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.util.CellRangeAddress;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.metadata.Cell;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.wms.dto.WmsDataComparePlanDTO.ImportDataMappingDTO;
import com.erp.server.wms.listener.WmsDataCompareExcelListener;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;

public class WmsDataCompareUtils {
	@Data
	public static class WmsDataCompareExcelDto{
		public Integer importDataCount = 0;
		public List<List<String>> headFieldLists = new ArrayList<>();
	}
	
	public static WmsDataCompareExcelDto getWmsDataCompareExcelDto(List<String> excelFiles) {
		WmsDataCompareExcelDto dto = new WmsDataCompareExcelDto();
		if(CollUtil.isEmpty(excelFiles)) {
			return dto;
		}
		Integer importDataCount = 0;
        for(String excelFile : excelFiles) {
        	WmsDataCompareExcelListener totalRowsExcelListener = new WmsDataCompareExcelListener();
        	EasyExcel.read(FastDFSClientUtil.getInputStream(excelFile), Map.class, totalRowsExcelListener).sheet().doRead();
        	
        	if(CollUtil.isNotEmpty(totalRowsExcelListener.getHeadFieldList())) {
        		dto.getHeadFieldLists().add(totalRowsExcelListener.getHeadFieldList());
        	}
        	
        	if(totalRowsExcelListener.getTotalRows() > 0) {
        		importDataCount = importDataCount + totalRowsExcelListener.getTotalRows() - 1; 
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
			if(i1.getImportField() == null) {
				return -1;
			}
			if(i2.getImportField() == null) {
				return 1;
			}
			return i1.getImportField().compareTo(i2.getImportField());
		});
	}
	
	// 自定义合并策略
    static class MergeStrategy extends AbstractMergeStrategy {
        private int headerSize;

        public MergeStrategy(int headerSize) {
            this.headerSize = headerSize;
        }

		@Override
		protected void merge(org.apache.poi.ss.usermodel.Sheet sheet, org.apache.poi.ss.usermodel.Cell cell, Head head,
				Integer relativeRowIndex) {
			if (cell.getRowIndex() < headerSize - 1) {
                // 合并第一行单元格
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cell.getColumnIndex(), cell.getColumnIndex() + 1));
            }
		}
    }
}
