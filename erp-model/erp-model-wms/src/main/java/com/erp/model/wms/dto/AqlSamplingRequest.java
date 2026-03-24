package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zdy
 * @ClassName AqlSamplingRequest
 * @description: 抽样方案请求VO
 * @date 2026年03月19日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AqlSamplingRequest {
    /**
     * 批量数
     */
    private Integer lotQty;
    /**
     * 检验水平（S-1/S-2/S-3/S-4/I/II/III）
     * QcLevelEnum
     */
    private String qcLevel;
    /**
     * AQL值（0.010/0.015/.../100）
     * AqlValueEnum
     */
    private String aqlValue;
}
