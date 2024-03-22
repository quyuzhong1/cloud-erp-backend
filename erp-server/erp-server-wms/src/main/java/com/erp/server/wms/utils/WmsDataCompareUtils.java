package com.erp.server.wms.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.excel.EasyExcel;
import com.common.core.utils.FastDFSClientUtil;
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
	
	public static String getPkValue(Map<Integer, String> data , List<Integer> excelPkIndexList) {
		StringBuffer sb = new StringBuffer();
		boolean firstFlag = true;
		for(Integer excelPkIndex : excelPkIndexList) {
			if(firstFlag) {
				firstFlag = false;
			}else {
				sb.append("-");
			}
			String d = data.get(excelPkIndex);
			if(StringUtils.isNotBlank(d)) {
				sb.append(d);
			}
		}
		return sb.toString();
	} 
}
