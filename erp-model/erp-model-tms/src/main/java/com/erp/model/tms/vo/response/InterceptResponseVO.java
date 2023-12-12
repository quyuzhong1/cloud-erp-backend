package com.erp.model.tms.vo.response;

import lombok.*;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName InterceptResponseVO
 * @description: 订单拦截结果
 * @date 2023年11月09日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class InterceptResponseVO extends LogisticsBaseResponseVO implements Serializable {
    /**
     * 运单号
     */
    String transportNo;
    /**
     * 跟踪单号
     */
    String trackNo;
    /**
     * 发货单号，erp传的
     */
    String deliveryNo;

}
