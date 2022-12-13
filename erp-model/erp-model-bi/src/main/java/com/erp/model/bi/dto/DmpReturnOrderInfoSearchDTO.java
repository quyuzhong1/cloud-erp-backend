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
     * 退货时间-从
     */
    private Date refundTime_begin;

    /**
     * 退或时间-到
     */
    private Date refundTime_end;

}
