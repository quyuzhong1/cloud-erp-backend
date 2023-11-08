package com.erp.model.tms.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName LogisticsOrderResponseVO
 * @description: 物流订单响应实体
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogisticsOrderResponseVO implements Serializable {

    /**
     * 发货单号，erp传的
     */
    private String deliveryNo;
    /**
     * 运单号(物流平台返回)
     */
    private String orderNo;
    /**
     * 跟踪单号(物流平台返回)
     */
    private String trackNo;
}
