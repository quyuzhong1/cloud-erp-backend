package com.erp.model.dmp.dto;


import com.common.business.dto.base.BaseSearchDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
     * 订单状态
     */
    private Integer orderState;

    /**
     * 订单时间-从
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime_begin;

    /**
     * 订单时间-到
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime_end;

    /**
     * 发货时间-从
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryTime_begin;

    /**
     * 发货时间-到
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryTime_end;
}
