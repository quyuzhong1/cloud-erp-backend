package com.erp.model.dmp.dto;


import com.common.business.dto.base.BaseSearchDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 15:45
 */
@Data
@NoArgsConstructor
public class DmpRefundInfoSearchDTO extends BaseSearchDTO {

    /**
     * 退款单号
     */
    private String refundId;

    /**
     * 原订单号
     */
    private String platformOrderId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 退款状态
     */
    private Integer refundStatus;

    /**
     * 退款时间-从
     */
    private Date refundTime_begin;

    /**
     * 退款时间-到
     */
    private Date refundTime_end;

    /**
     * 订单时间-从
     */
    private Date orderTime_begin;

    /**
     * 订单时间-到
     */
    private Date orderTime_end;

}
