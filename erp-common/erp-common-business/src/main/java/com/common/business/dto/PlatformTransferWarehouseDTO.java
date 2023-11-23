package com.common.business.dto;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 中抓仓库DTO 所有平台仓库数据通用数据，转换为此类后发送mq统一消费处理
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString
public class PlatformTransferWarehouseDTO extends UniqueDto {

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

    /**
     * 第三方仓对应erp表主键id
     */
    private String providerErpId;

    //物流渠道编码
    private String logisticsChannelCode;

    //物流渠道名称
    private String logisticsChannelName;

    //中转仓编码
    private String transferWarehouseCode;

    //中转仓名称
    private String transferWarehouseName;

    //目的仓编码
    private String destinationWarehouseCode;

    //目的仓名称
    private String destinationWarehouseName;
}
