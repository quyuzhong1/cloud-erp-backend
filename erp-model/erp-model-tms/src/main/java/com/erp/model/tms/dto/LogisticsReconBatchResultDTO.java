package com.erp.model.tms.dto;

import com.erp.model.tms.dto.excel.LogisticsReconImportExcelDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 物流商对账单单批导入落库结果
 *
 * @author Will
 * @since 2026-06-03
 */
@Getter
@AllArgsConstructor
public class LogisticsReconBatchResultDTO {

    /**
     * 校验失败行
     */
    private final List<LogisticsReconImportExcelDTO> errorList;

    /**
     * 成功落库的对账明细行数
     */
    private final int detailCount;

    /**
     * 成功落库的费用项条数
     */
    private final int subCount;

    /**
     * 当前批次累计金额
     */
    private final BigDecimal totalAmount;

    /**
     * 出现的币别集合
     */
    private final Set<String> currencies;
}
