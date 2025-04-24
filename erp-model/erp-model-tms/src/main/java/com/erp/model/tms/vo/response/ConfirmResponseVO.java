package com.erp.model.tms.vo.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 订单取消返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ConfirmResponseVO extends LogisticsBaseResponseVO implements Serializable {
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
    /**
     * 是否存在 1对多订单情况
     */
    boolean more;
    /**
     * 兼容一对多情况 一个订单存在多个运单号
     */
    private List<ConfirmResponseVO> confirmResponseVOS;

}
