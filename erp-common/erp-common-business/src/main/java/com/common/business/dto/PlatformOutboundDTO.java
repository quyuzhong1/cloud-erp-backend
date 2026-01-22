package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *  平台入库单DTO,所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformOutboundDTO extends UniqueDto {

    /**
     * 仓库平台类型
     * {@link WarehousePlatformTypeEnum}
     */
    private String warehousePlatformType;
    /**
     * 供应商
     * {@link OmsPlatformEnum}
     */
    private String provider;

    //第三方订单号
    private String orderCode;

    //erp参考号
    private String referenceNo;

    //erp订单状态
    private String orderStatus;

    //接单状态 1截单中 2截单成功 3截单失败
    private String interceptStatus;
    //第三方订单状态
    private String thirdOrderStatus;

    //出库时间
    private LocalDateTime outBoundTime;

    /**
     * 跟踪号
     */
    private String trackNo;

    /**
     * 异常原因
     */
    private String abnormalProblemReason;

    /**
     *平台订单号
     */
    private String swOrderNumber;

    /**
     *仓库代码
     */
    private String warehouseCode;

    /**
     *运输方式
     */
    private String shippingMethod;

    /**
     *承运商
     */
    private String carrierName;
}
