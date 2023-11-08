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
    /**
     * 物流渠道号码
     */
    String logisticsChannelNo;
    /**
     *ODA标识(偏远地址：Y 非偏远地址：N)
     */
    String odaResultSign;
}
