package com.erp.model.dmp.dto;

import com.erp.common.dto.base.BaseSearchDTO;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnCreateTime_begin;

    /**
     * 退货时间-到
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnCreateTime_end;

    /**
     * 订单时间-从
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime_begin;

    /**
     * 订单时间-到
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime_end;

}
