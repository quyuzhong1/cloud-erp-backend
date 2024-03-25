package com.erp.server.wms.listener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

public class WmsDataCompareExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    private int totalRows = 0;
    
    private List<String> headFieldList;
    
    private List<List<String>> datas = new ArrayList<>();

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
    	if(data != null && data.size() > 0) {
    		List<String> d = new ArrayList<>();
    		for(int i = 0; i < data.size(); i++) {
    			d.add(data.get(i));
    		}
    		datas.add(d);
    	}
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

	public List<List<String>> getDatas() {
		return datas;
	}

}
