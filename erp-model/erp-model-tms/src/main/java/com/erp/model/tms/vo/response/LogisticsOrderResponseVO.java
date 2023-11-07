package com.erp.model.tms.vo.response;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class LogisticsOrderResponseVO implements Serializable {
    /**
     * 运单号
     */
    String orderNo;
    /**
     * 跟踪单号
     */
    String trackNo;
}
