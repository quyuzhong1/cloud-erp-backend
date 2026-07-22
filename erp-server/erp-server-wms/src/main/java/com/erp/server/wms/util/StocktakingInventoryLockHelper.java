package com.erp.server.wms.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;

import java.util.Objects;

/**
 * 盘点库存 Redis 锁维度工具：库位规范化与维度 key 构建（与 {@link com.erp.server.wms.service.impl.InventoryServiceImpl#findInventory} 一致）。
 */
public final class StocktakingInventoryLockHelper {

    private StocktakingInventoryLockHelper() {
    }

    /**
     * 库位规范化：空库位 trim；不控库位状态置空串。
     *
     * @param warehouseLocation 原始库位
     * @param inventoryStatus   库存状态
     * @return 规范化库位
     */
    public static String normalizeWarehouseLocation(String warehouseLocation, String inventoryStatus) {
        InventoryStatusEnum inventoryStatusEnum = InventoryStatusEnum.getByCode(inventoryStatus);
        if (inventoryStatusEnum != null && Objects.equals(Boolean.FALSE, inventoryStatusEnum.getControlLocation())) {
            return "";
        }
        return StrUtils.null2EmptyWithTrim(warehouseLocation);
    }

    /**
     * 构建库存维度去重 key（分隔符 {@code |}，库位已规范化）。
     */
    public static String buildDimensionKey(String orgId, String warehouseId, String warehouseLocation,
                                           String skuId, String dictInventoryStatus) {
        return CharSequenceUtil.format("{}|{}|{}|{}|{}", orgId, warehouseId,
                normalizeWarehouseLocation(warehouseLocation, dictInventoryStatus), skuId, dictInventoryStatus);
    }

    /**
     * 原始库位 trim 后是否与规范化库位不同（迁移期 raw lock 预检/释锁兜底用）。
     */
    public static boolean isRawWarehouseLocationDifferent(String warehouseLocation, String inventoryStatus) {
        return !CharSequenceUtil.equals(
                normalizeWarehouseLocation(warehouseLocation, inventoryStatus),
                StrUtils.null2EmptyWithTrim(warehouseLocation));
    }

    /**
     * 构建 wh+库位+sku+status 维度 key（不含 orgId，释锁兜底反查 org 用）。
     *
     * @param warehouseId       仓库 ID
     * @param warehouseLocation 库位
     * @param skuId             SKU ID
     * @param inventoryStatus   库存状态
     * @return 维度 key
     */
    public static String buildDimensionKeyWithoutOrg(String warehouseId, String warehouseLocation, String skuId,
                                                     String inventoryStatus) {
        return CharSequenceUtil.format("{}|{}|{}|{}", warehouseId,
                normalizeWarehouseLocation(warehouseLocation, inventoryStatus), skuId, inventoryStatus);
    }
}
