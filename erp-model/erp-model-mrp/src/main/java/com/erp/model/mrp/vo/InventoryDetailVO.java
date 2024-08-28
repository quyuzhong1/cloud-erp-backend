package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InventoryDetailVO {

    /**
     * 类型   海外仓可用/海外仓在途/预计发货/本地仓可用/本地仓在途/预计采购
     */
    private String inventoryType;

    /**
     * 实体仓id
     */
    private Integer warehouseId;

    /**
     * 虚拟仓id
     */
    private Integer virtualWarehouseId;

    /**
     * 仓库类型，local本地，overseas海外
     */
    private String warehouseType;

    /**
     * 关联店铺类型，platform按平台，shop按店铺
     */
    private String channelType;

    /**
     * 店铺id的json
     */
    private String channelIdJson;

    /**
     * 库存分配类型
     */
    private String inventoryAllocateType;

    /**
     * 总数量
     */
    private Integer totalQty;
    /**
     * 店铺库存明细
     */
    private List<ShopInventoryDetailVO> shopInventoryDetails;

    @Getter
    @Setter
    public static class ShopInventoryDetailVO {
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名字
         */
        private String shopName;
        /**
         * 数量
         */
        private Integer qty;
    }
}
