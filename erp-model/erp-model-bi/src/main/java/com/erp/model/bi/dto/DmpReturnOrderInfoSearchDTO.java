package com.erp.model.bi.dto;

import com.erp.common.dto.base.BaseSearchDTO;
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
public class DmpReturnOrderInfoSearchDTO extends BaseSearchDTO {

    /**
     * 退货订单号
     */
    private String returnOrderId;

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
     * 状态
     */
    private Integer status;

    /**
     * 退货时间-从
     */
    private Date returnCreateTime_begin;

    /**
     * 退货时间-到
     */
    private Date returnCreateTime_end;

    /**
     * 订单时间-从
     */
    private Date orderTime_begin;

    /**
     * 订单时间-到
     */
    private Date orderTime_end;

}
