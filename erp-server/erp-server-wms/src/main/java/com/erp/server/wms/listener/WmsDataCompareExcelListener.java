package com.erp.server.wms.listener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

public class WmsDataCompareExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    private int totalRows = 0;
    
    private List<String> headFieldList;
    
    private List<Map<Integer, String>> datas = new ArrayList<>();

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
    	// 处理表头数据
        headFieldList = new ArrayList<>();
        for(int i = 0; i < headMap.size(); i++) {
        	headFieldList.add(headMap.get(i));
        }
    }
    
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
    	datas.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        totalRows = context.readRowHolder().getRowIndex();
    }

    public int getTotalRows() {
        return totalRows;
    }

	public List<String> getHeadFieldList() {
		return headFieldList;
	}

	public List<Map<Integer, String>> getDatas() {
		return datas;
	}
	
}
