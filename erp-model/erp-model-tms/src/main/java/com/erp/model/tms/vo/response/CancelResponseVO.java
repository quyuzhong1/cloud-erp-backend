package com.erp.model.tms.vo.response;

import lombok.*;

import java.io.Serializable;

/**
 * 订单取消返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class CancelResponseVO extends LogisticsBaseResponseVO implements Serializable {
    /**
     * 运单号
     */
    String transportNo;
    /**
     * 跟踪单号
     */
    String trackNo;
    /**
     * 发货单号,erp系统
     */
    String deliveryNo;

}
