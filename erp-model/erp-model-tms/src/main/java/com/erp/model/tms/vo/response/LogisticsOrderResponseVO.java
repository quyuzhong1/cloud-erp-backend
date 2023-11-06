package com.erp.model.tms.vo.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName LogisticsOrderResponseVO
 * @description: 物流订单响应实体
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
public class LogisticsOrderResponseVO implements Serializable {
    /**
     * 订单编号
     */
    String orderNo;
    /**
     * 运单号
     */
    String trackNo;
}
