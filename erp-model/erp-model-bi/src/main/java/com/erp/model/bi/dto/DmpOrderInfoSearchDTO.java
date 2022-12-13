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
public class DmpOrderInfoSearchDTO extends BaseSearchDTO {

    /**
     * 订单号
     */
    private String refundOrder;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 订单时间-从
     */
    private Date createTime_begin;

    /**
     * 订单时间-到
     */
    private Date createTime_end;

    /**
     * 发货时间-从
     */
    private Date deliveryDate_begin;

    /**
     * 发货时间-到
     */
    private Date deliveryDate_end;
}
