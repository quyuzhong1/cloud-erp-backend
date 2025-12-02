package com.common.business.dto;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 仓库DTO 所有平台库存数据通用数据，转换为此类后发送mq统一消费处理
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString
public class PlatformInventoryDTO extends UniqueDto {

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

    //SKU
    private String productSku;

    //ERP仓库ID
    private String warehouseId;

    //第三方仓仓库代码
    private String platformWarehouseCode;

    //第三方仓仓库名称
    private String platformWarehouseName;

    //尾程在途数量
    private Integer onway;

    //总尾程在途数量
    private Integer totalOnway;

    //发货在途数量
    private Integer transferOnway;

    //销退在途数量
    private Integer saleReturnInTransitQty;

    //待上架数量
    private Integer pending;

    //可售数量
    private Integer sellable;

    //不合格数量
    private Integer unsellable;

    //备货数量
    private Integer stocking;

    //缺货数量
    private Integer piNoStock;

    //待出库数量
    private Integer reserved;

    //历史出库数量
    private Integer shipped;

    //待确认数量
    private Integer unconfirmed;

    //冻结数量
    private Integer piFreeze;
    /**
     * 数据下载时间
     */
    private LocalDateTime downloadTime;

    /**
     * 库龄信息
     */
    private List<PlatformInventoryAgeDTO> ageInfoList;


    /**
     * 库存信息
     */
    @Data
    @NoArgsConstructor
    public static class PlatformInventoryAgeDTO implements Serializable {

        /**
         * 在库库存
         */
        private Integer inventoryQty;
        /**
         * 上架日期
         */
        private LocalDate putAwayDate;
        /**
         * 拉取日期
         */
        private LocalDate pullDate;

        /**
         * 库龄
         */
        private Integer inventoryAge;
    }

}
