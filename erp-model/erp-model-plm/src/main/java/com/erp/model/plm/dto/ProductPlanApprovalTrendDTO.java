package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/23 19:46
 */
@Data
@NoArgsConstructor
public class ProductPlanApprovalTrendDTO implements Serializable {

    /**
     * 月份
     */
    private Integer month;

    /**
     * 立项数量
     */
    private Integer approvalCount;

    /**
     * 完成数量
     */
    private Integer completeCount;
}
