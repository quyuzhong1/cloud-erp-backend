package com.erp.model.srm.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ConfigVO
 * @description: TODO
 * @date 2024年01月10日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierConfigVO implements Serializable {
    /**
     * 供应商id
     */
    private String supplierId;
    /**
     *订单接受规则
     */
    private String orderAcceptRule;
    /**
     *退货确认规则
     */
    private String returnConfirmRule;
}
