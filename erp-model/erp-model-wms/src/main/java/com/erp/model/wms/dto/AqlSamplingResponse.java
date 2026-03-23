package com.erp.model.wms.dto;

import lombok.Data;

/**
 * @author zdy
 * @ClassName AqlSamplingResponse
 * @description: TODO
 * @date 2026年03月19日
 * @version: 1.0
 */
@Data
public class AqlSamplingResponse {
    /**
     * 批量范围（如2~8）
     */
    private String lotRange;
    /**
     * 批量下限
     */
    private Integer minLotQty;
    /**
     * 批量上限
     */
    private Integer maxLotQty;
    /**
     * 样本量字码（如A/B/C...）
     */
    private String sampleQtyCode;
    /**
     * 样本量（如2/3/5...）
     */
    private Integer sampleQty;
    /**
     * 接收数Ac
     */
    private Integer acceptQty;
    /**
     * 拒收数Re
     */
    private Integer rejectQty;
    /**
     * 错误信息
     */
    private String errorMsg;
    /**
     * 提示信息（非必填）
     */
    private String tips;
}
