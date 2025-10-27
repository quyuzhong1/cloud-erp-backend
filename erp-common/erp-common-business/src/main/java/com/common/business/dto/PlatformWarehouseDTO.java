package com.common.business.dto;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 仓库DTO 所有平台仓库数据通用数据，转换为此类后发送mq统一消费处理
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString
public class PlatformWarehouseDTO extends UniqueDto {

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

    //仓库代码
    private String warehouseCode;

    //仓库名称
    private String warehouseName;

    //仓库所在国家/地区代码
    private String countryCode;

    //仓库所在国家名称
    private String countryName;

    private String type;

    //仓库状态 0:不可用;1:可用;2:停用
    private String platformWarehouseStatus;

    private String platformWarehouseType;
}
