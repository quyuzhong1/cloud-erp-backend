package com.erp.model.tms.vo.request;

import com.erp.model.tms.entity.LogisticsAuthEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName LogisticsQueryVO
 * @description: 查询类
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LogisticsQueryBaseVO implements Serializable {
    /**
     * 发货单号 (erp销售订单code)
     */
    String deliveryNo;
    /**
     * 平台订单号
     */
    String platformCode;
    /**
     * 订单id(erp销售订单id)
     */
    String orderId;
    /**
     * 运单号
     */
    String transportNo;
    /**
     * 跟踪号
     */
    String trackNo;
    /**
     * 授权信息
     */
    Map<String, String> authMap;
}
