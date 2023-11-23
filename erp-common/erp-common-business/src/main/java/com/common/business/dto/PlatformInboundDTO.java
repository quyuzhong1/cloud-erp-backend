package com.common.business.dto;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 *  平台入库单DTO,所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformInboundDTO extends UniqueDto {

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

    //入库单号
    private String receivingCode;

    //ERP入库单状态
    private String receivingStatus;

    //入库明细
    private List<Item> items;

    @Data
    @ToString
    public static class Item {

        //SKU
        private String productSku;

        //收货数
        private Integer receivedQuantity;

        //上架数量
        private Integer putawayQuantity;

        //箱号
        private String boxNo;
    }
}
