package com.erp.server.tms.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.tms.dto.excel.LogisticsReconMatchImportExcelDTO;
import com.erp.server.tms.service.LogisticsReconDetailService;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物流商对账导入匹配 Excel 解析监听器（分批提交匹配，仿 LogisticsReconExcelListener）。
 *
 * @author Will
 * @since 2026-06-15
 */
public class LogisticsReconMatchImportExcelListener
        extends AnalysisEventListener<LogisticsReconMatchImportExcelDTO> {

    private static final int BATCH_COUNT = 3000;

    private final String mainId;
    private final LogisticsReconDetailService logisticsReconDetailService =
            SpringUtil.getBean(LogisticsReconDetailService.class);

    @Getter
    private final Map<String, LogisticsReconMatchImportExcelDTO> rowByTrackNo = new LinkedHashMap<>();

    @Getter
    private final Map<String, List<String>> trackNoErrorMap = new LinkedHashMap<>();

    @Getter
    private final Set<String> matchedTrackNoSet = new HashSet<>();

    @Getter
    private int totalCount = 0;

    private final List<LogisticsReconMatchImportExcelDTO> buffer = new ArrayList<>();

    public LogisticsReconMatchImportExcelListener(String mainId) {
        this.mainId = mainId;
    }

    @Override
    public void invoke(LogisticsReconMatchImportExcelDTO row, AnalysisContext context) {
        if (StrUtil.isBlank(row.getTrackNo())) {
            return;
        }
        totalCount++;
        buffer.add(row);
        rowByTrackNo.putIfAbsent(StrUtil.trim(row.getTrackNo()), row);
        if (buffer.size() >= BATCH_COUNT) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!buffer.isEmpty()) {
            flush();
        }
    }

    private void flush() {
        logisticsReconDetailService.processImportMatchBatch(mainId, buffer, trackNoErrorMap, matchedTrackNoSet);
        buffer.clear();
    }
}
