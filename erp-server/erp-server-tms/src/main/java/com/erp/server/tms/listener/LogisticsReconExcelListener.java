package com.erp.server.tms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 物流商对账单导入解析监听器
 * @author Will
 * @date: 2026/06/02
 */
public class LogisticsReconExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    /**
     * 表头
     */
    @Getter
    private Map<Integer, String> headMap = Collections.emptyMap();

    /**
     * Excel 行数据
     */
    @Getter
    private final List<Map<Integer, String>> rows = new ArrayList<>();

    /**
     * 读取表头
     * @author Will
     * @date: 2026/06/02
     * @param headMap
     * @param context
     * @return void
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
    }

    /**
     * 读取行数据
     * @author Will
     * @date: 2026/06/02
     * @param data
     * @param context
     * @return void
     */
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        rows.add(data);
    }

    /**
     * 完成解析
     * @author Will
     * @date: 2026/06/02
     * @param context
     * @return void
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 仅收集数据，落库由 Service 统一处理。
    }
}
