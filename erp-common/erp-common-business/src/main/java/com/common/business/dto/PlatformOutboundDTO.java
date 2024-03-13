package com.common.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    //第三方订单状态
    private String thirdOrderStatus;

    //出库时间
    private LocalDateTime outBoundTime;

}
