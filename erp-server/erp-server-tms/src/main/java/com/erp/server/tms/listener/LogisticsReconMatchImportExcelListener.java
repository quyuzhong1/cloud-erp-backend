package com.erp.server.tms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.tms.dto.excel.LogisticsReconMatchImportExcelDTO;
import com.erp.server.tms.service.LogisticsReconDetailService;
import com.erp.server.tms.service.support.LogisticsReconImportMatchContext;
import com.erp.server.tms.util.LogisticsReconMatchGroupHelper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物流商对账导入匹配 Excel 解析监听器（按模板识别号分批提交匹配）。
 *
 * @author Will
 * @since 2026-06-15
 */
public class LogisticsReconMatchImportExcelListener
        extends AnalysisEventListener<LogisticsReconMatchImportExcelDTO> {

    /**
     * 单批提交匹配的行数
     */
    private static final int BATCH_COUNT = 3000;

    private final LogisticsReconImportMatchContext context;

    private final LogisticsReconDetailService logisticsReconDetailService =
            SpringUtil.getBean(LogisticsReconDetailService.class);

    /** 识别号分组键 → 首条 Excel 行（同识别组去重） */
    @Getter
    private final Map<String, LogisticsReconMatchImportExcelDTO> rowByGroupKey = new LinkedHashMap<>();

    /** 识别号分组键 → 校验/匹配错误文案列表 */
    @Getter
    private final Map<String, List<String>> groupErrorMap = new LinkedHashMap<>();

    /** 本文件已命中账单识别组的键集合 */
    @Getter
    private final Set<String> matchedGroupKeySet = new HashSet<>();

    /**
     * 本文件已处理过的识别组（跨 Excel 分批去重，避免同识别组重复提交匹配）。
     */
    @Getter
    private final Set<String> handledGroupKeySet = new HashSet<>();

    /** 已读取有效行数（固定模板识别列非全空） */
    @Getter
    private int totalCount = 0;

    private final List<LogisticsReconMatchImportExcelDTO> buffer = new ArrayList<>();

    public LogisticsReconMatchImportExcelListener(LogisticsReconImportMatchContext context) {
        this.context = context;
    }

    @Override
    public void invoke(LogisticsReconMatchImportExcelDTO row, AnalysisContext analysisContext) {
        if (LogisticsReconMatchGroupHelper.isTemplateRowBlank(row)) {
            return;
        }
        totalCount++;
        buffer.add(row);
        if (LogisticsReconMatchGroupHelper.isConfiguredIdentifyComplete(row, context.getUniqueKeyList())) {
            String groupKey = LogisticsReconMatchGroupHelper.buildImportMatchExcelGroupKey(row, context.getUniqueKeyList());
            rowByGroupKey.putIfAbsent(groupKey, row);
        } else {
            rowByGroupKey.put("row:" + analysisContext.readRowHolder().getRowIndex(), row);
        }
        if (buffer.size() >= BATCH_COUNT) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!buffer.isEmpty()) {
            flush();
        }
    }

    /**
     * 提交当前批次到明细服务做导入匹配（认领 + 异步匹配）。
     *
     * @author Will
     * @date 2026/6/12
     */
    private void flush() {
        try {
            logisticsReconDetailService.processImportMatchBatch(context, buffer, groupErrorMap,
                    matchedGroupKeySet, handledGroupKeySet);
        } finally {
            buffer.clear();
        }
    }
}
