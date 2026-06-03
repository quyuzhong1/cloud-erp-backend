package com.erp.server.tms.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.model.tms.dto.LogisticsReconBatchResultDTO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.dto.excel.LogisticsReconImportExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.service.LogisticsReconService;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物流商对账单导入解析监听器（分批落库，单批 1000 条，仿 LogisticsLastMileCostExcelListener）
 * @author Will
 * @date: 2026/06/03
 */
public class LogisticsReconExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    /**
     * 单批落库行数
     */
    private static final int BATCH_COUNT = 1000;

    private final LogisticsReconDTO.ImportDTO dto;
    private final CfgLogisticsCostImportEntity importCfg;
    private final List<CfgLogisticsCostImportDetailEntity> cfgDetails;

    private final LogisticsReconService logisticsReconService = SpringUtil.getBean(LogisticsReconService.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 表头
     */
    @Getter
    private Map<Integer, String> headMap = Collections.emptyMap();

    /**
     * 当前批次缓冲行
     */
    private final List<Map<Integer, String>> buffer = new ArrayList<>();

    /**
     * 已读取总行数
     */
    @Getter
    private int totalRowCount = 0;

    /**
     * 已落库处理行数（用于行号基准，按 sheet 连续）
     */
    private int flushedRows = 0;

    /**
     * 成功落库的对账明细行数
     */
    @Getter
    private int detailCount = 0;

    /**
     * 成功落库的费用项条数
     */
    @Getter
    private int subCount = 0;

    /**
     * 累计金额
     */
    @Getter
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * 出现的币别集合
     */
    private final Set<String> currencies = new HashSet<>();

    /**
     * 校验失败行
     */
    @Getter
    private final List<LogisticsReconImportExcelDTO> errorList = new ArrayList<>();

    public LogisticsReconExcelListener(LogisticsReconDTO.ImportDTO dto, CfgLogisticsCostImportEntity importCfg,
                                       List<CfgLogisticsCostImportDetailEntity> cfgDetails) {
        this.dto = dto;
        this.importCfg = importCfg;
        this.cfgDetails = cfgDetails;
    }

    /**
     * 读取表头
     * @author Will
     * @date: 2026/06/03
     * @param headMap 表头
     * @param context 解析上下文
     * @return void
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
    }

    /**
     * 每解析一行回调，累积到批次缓冲，达到阈值即落库
     * @author Will
     * @date: 2026/06/03
     * @param data 行数据
     * @param context 解析上下文
     * @return void
     */
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        totalRowCount++;
        // 末列为空时 map 缺列，补齐与表头一致避免取值错位
        for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
            if (ObjectUtil.isEmpty(data.get(entry.getKey()))) {
                data.put(entry.getKey(), "");
            }
        }
        buffer.add(new HashMap<>(data));
        if (buffer.size() >= BATCH_COUNT) {
            flush();
        }
    }

    /**
     * 解析完成后落库剩余批次
     * @author Will
     * @date: 2026/06/03
     * @param context 解析上下文
     * @return void
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!buffer.isEmpty()) {
            flush();
        }
    }

    /**
     * 落库当前批次并累积统计
     * @author Will
     * @date: 2026/06/03
     * @return void
     */
    private void flush() {
        int rowNoStart = flushedRows + 1;
        LogisticsReconBatchResultDTO result =
                logisticsReconService.handleReconImportBatch(buffer, headMap, rowNoStart, dto, importCfg, cfgDetails);
        errorList.addAll(result.getErrorList());
        detailCount += result.getDetailCount();
        subCount += result.getSubCount();
        totalAmount = totalAmount.add(result.getTotalAmount());
        currencies.addAll(result.getCurrencies());
        flushedRows += buffer.size();
        buffer.clear();
        updateProgress();
    }

    /**
     * 推导主表币别（多币别返回空）
     * @author Will
     * @date: 2026/06/03
     * @return String
     */
    public String resolveCurrency() {
        return currencies.size() == 1 ? currencies.iterator().next() : "";
    }

    /**
     * 表头是否为空
     * @author Will
     * @date: 2026/06/03
     * @return boolean
     */
    public boolean isHeadEmpty() {
        return headMap.isEmpty();
    }

    /**
     * 更新导入任务进度
     * @author Will
     * @date: 2026/06/03
     * @return void
     */
    private void updateProgress() {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(totalRowCount);
        downloadTaskFeign.updateTask(importResultDTO);
    }
}
