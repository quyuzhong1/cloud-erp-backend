package com.erp.model.tms.vo.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsOrderResponseVO
 * @description: 物流订单响应实体
 * @date 2023年11月06日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(callSuper = true)
public class LogisticsOrderResponseVO extends LogisticsBaseResponseVO implements Serializable {
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

    String remark;

    //国家二字码
    String countryCode;
    /**
     * 是否存在 1对多订单情况
     */
    Boolean more;
    /**
     * 兼容一对多情况 一个订单存在多个运单号 主单和子单
     */
    List<LogisticsOrderResponseVO> logisticsOrderResponseVOS;
}
