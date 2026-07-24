package com.erp.oms.aliexpress.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 速卖通官方仓货品关系及库存流水 DTO。
 */
public final class AliExpressWarehouseInventoryDTO {

    /**
     * 工具型 DTO 容器不允许实例化。
     */
    private AliExpressWarehouseInventoryDTO() {
    }

    /**
     * 店铺全托管货品关系。
     */
    @Data
    public static class ShopItemRelationDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String channelSellerId;
        private String channel;
        private String businessType;
        private List<ScItemDTO> scItemList;
    }

    /**
     * 速卖通货品。
     */
    @Data
    public static class ScItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String scItemId;
        private String supplierSkuCode;
        private String itemCode;
        private String whcBarCode;
        private List<ScItemRelationDTO> relationList;
    }

    /**
     * 平台商品与货品绑定关系。
     */
    @Data
    public static class ScItemRelationDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String itemId;
        private String skuId;
        private String bindStatus;
    }

    /**
     * 官方仓库存流水。
     */
    @Data
    public static class InventoryLogDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer inventoryType;
        private String whOrderCode;
        private String bizType;
        private String bizTradeId;
        private Long operateTime;
        private String bizSubTradeId;
        private Integer changeQuantity;
        private ScItemInfoDTO scItemInfo;
        private StoreInfoDTO storeInfo;
    }

    /**
     * 库存流水货品信息。
     */
    @Data
    public static class ScItemInfoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String scItemId;
        private String scItemCode;
        private String scItemName;
        private String scItemBarcode;
    }

    /**
     * 库存流水仓库信息。
     */
    @Data
    public static class StoreInfoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String storeCode;
        private String storeName;
        private String storeType;
    }
}
