package com.erp.server.wms.inventory;

import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link VirtualInventoryUnallocCheckHelper} 与 try.lua 约定一致性校验。
 */
public class VirtualInventoryUnallocCheckHelperTest {

    /**
     * try.lua {@code unalloc_lua_error_prefix} 必须与 Java {@link VirtualInventoryUnallocCheckHelper#UNALLOC_LUA_ERROR_PREFIX} 一致。
     */
    @Test
    public void tryLuaUnallocPrefixMatchesJavaConstant() {
        VirtualInventoryUnallocCheckHelper.assertTryLuaUnallocPrefixSynced();
    }

    /**
     * try.lua {@code inventory_lua_biz_prefix} 必须与 Java {@link VirtualInventoryUnallocCheckHelper#INVENTORY_LUA_BIZ_ERROR_PREFIX} 一致。
     */
    @Test
    public void tryLuaInventoryBizPrefixMatchesJavaConstant() {
        VirtualInventoryUnallocCheckHelper.assertTryLuaInventoryBizPrefixSynced();
    }

    /**
     * 仓+SKU 分组 key 使用分隔符，避免 skuId 与 warehouseId 直接拼接碰撞。
     */
    @Test
    public void warehouseSkuGroupKeyUsesDelimiter() {
        String key = VirtualInventoryUnallocCheckHelper.buildWarehouseSkuGroupKey("sku-a", "wh-b");
        Assert.assertEquals("sku-a" + VirtualInventoryUnallocCheckHelper.WAREHOUSE_SKU_GROUP_KEY_DELIMITER + "wh-b", key);
        Assert.assertNotEquals(
                VirtualInventoryUnallocCheckHelper.buildWarehouseSkuGroupKey("ab", "c"),
                VirtualInventoryUnallocCheckHelper.buildWarehouseSkuGroupKey("a", "bc"));
    }

    /**
     * 非法预占片段 fail-closed，不得当作 0 参与 pending 汇总。
     */
    @Test(expected = ServiceException.class)
    public void sumPendingReserveRejectsInvalidQtySegment() {
        VirtualInventoryUnallocCheckHelper.sumPendingReserve("0&&txn@@not-a-number", null, null);
    }

    /**
     * 三字段 reserve 片段（transaction@@operationId@@qty）应正确汇总 pending。
     */
    @Test
    public void sumPendingReserveSupportsOperationIdSegment() {
        int pending = VirtualInventoryUnallocCheckHelper.sumPendingReserve(
                "0&&txn-a@@op-1@@3&&txn-b@@op-2@@5", "exclude-txn", "exclude-op");
        Assert.assertEquals(8, pending);
    }

    /**
     * 同事务不同 operation 的预占在预检时不应被整笔 transactionId 排除。
     */
    @Test
    public void sumPendingReserveCountsSameTransactionOtherOperations() {
        int pending = VirtualInventoryUnallocCheckHelper.sumPendingReserve(
                "0&&txn-a@@op-1@@3&&txn-a@@op-2@@5", null, null);
        Assert.assertEquals(8, pending);
    }

    /**
     * operationId 由本批流水 ID 排序后拼接，同批重试幂等、不同批可区分。
     */
    @Test
    public void resolveTryOperationIdUsesSortedFlowIds() {
        InventoryTransactionDTO first = new InventoryTransactionDTO();
        first.setId("flow-b");
        InventoryTransactionDTO second = new InventoryTransactionDTO();
        second.setId("flow-a");
        String operationId = VirtualInventoryUnallocCheckHelper.resolveTryOperationId(Arrays.asList(first, second));
        Assert.assertEquals("flow-a,flow-b", operationId);
    }

    /**
     * 指定虚拟仓的明细不参与实体仓未分配 TRY。
     */
    @Test
    public void filterNeedEntityUnallocCheckExcludesVirtualWarehouseId() {
        InventoryTransactionDTO withVirtual = new InventoryTransactionDTO();
        withVirtual.setQty(-1);
        withVirtual.setInventoryStatus(com.erp.model.wms.enums.inventory.InventoryStatusEnum.USABLE.getCode());
        withVirtual.setSourceType(com.erp.model.wms.enums.inventory.InventorySourceTypeEnum.OTHER_OUTSTOCK.getCode());
        withVirtual.setVirtualWarehouseId("vw-1");

        InventoryTransactionDTO withoutVirtual = new InventoryTransactionDTO();
        withoutVirtual.setQty(-1);
        withoutVirtual.setInventoryStatus(com.erp.model.wms.enums.inventory.InventoryStatusEnum.USABLE.getCode());
        withoutVirtual.setSourceType(com.erp.model.wms.enums.inventory.InventorySourceTypeEnum.OTHER_OUTSTOCK.getCode());

        List<InventoryTransactionDTO> entityOnly = VirtualInventoryUnallocCheckHelper.filterNeedEntityUnallocCheck(
                Arrays.asList(withVirtual, withoutVirtual));
        Assert.assertEquals(1, entityOnly.size());
        Assert.assertNull(entityOnly.get(0).getVirtualWarehouseId());
    }

    /**
     * 批量基量映射汇总应与逐条一致。
     */
    @Test
    public void sumEntityBaseQtyFromMapAggregatesByInventoryId() {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        map.put("inv-1", 10);
        map.put("inv-2", 20);
        int total = VirtualInventoryUnallocCheckHelper.sumEntityBaseQtyFromMap(Arrays.asList("inv-1", "inv-2", "inv-3"), map);
        Assert.assertEquals(30, total);
    }

    /**
     * 指定虚拟仓明细应参与仓+SKU 共享锁，但不参与实体未分配 TRY。
     */
    @Test
    public void buildUnallocLockKeysIncludesVirtualWarehouseOutbound() {
        InventoryTransactionDTO withVirtual = new InventoryTransactionDTO();
        withVirtual.setQty(-5);
        withVirtual.setSkuId("sku-1");
        withVirtual.setWarehouseId("wh-1");
        withVirtual.setInventoryStatus(com.erp.model.wms.enums.inventory.InventoryStatusEnum.USABLE.getCode());
        withVirtual.setSourceType(com.erp.model.wms.enums.inventory.InventorySourceTypeEnum.OTHER_OUTSTOCK.getCode());
        withVirtual.setVirtualWarehouseId("vw-1");

        List<String> keys = VirtualInventoryUnallocCheckHelper.buildUnallocLockKeys(
                Collections.singletonList(withVirtual));
        Assert.assertEquals(1, keys.size());
        Assert.assertTrue(keys.get(0).contains("wh-1"));
        Assert.assertTrue(keys.get(0).contains("sku-1"));
    }

    /**
     * virtualQty&gt;0 且 inventoryIds 为空时应 fail-closed。
     */
    @Test(expected = ServiceException.class)
    public void assertUnallocTryItemsHaveInventoryIdsRejectsEmptyList() {
        VirtualInventoryUnallocCheckHelper.UnallocTryItem item = new VirtualInventoryUnallocCheckHelper.UnallocTryItem(
                "wh-1", "sku-1", "SKU001", "WH-A", 5, 10, Collections.emptyList());
        VirtualInventoryUnallocCheckHelper.assertUnallocTryItemsHaveInventoryIds(Collections.singletonList(item));
    }

    /**
     * 虚拟仓分货/调拨交易应生成与实体出库相同的仓+SKU 未分配共享锁 key。
     */
    @Test
    public void buildUnallocLockKeysForVirtualStockGroupsByWarehouseSku() {
        com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO.InventoryTransactionDTO first =
                new com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO.InventoryTransactionDTO();
        first.setSkuId("sku-1");
        first.setWarehouseId("wh-1");
        com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO.InventoryTransactionDTO second =
                new com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO.InventoryTransactionDTO();
        second.setSkuId("sku-1");
        second.setWarehouseId("wh-1");
        List<String> keys = VirtualInventoryUnallocCheckHelper.buildUnallocLockKeysForVirtualStock(
                Arrays.asList(first, second));
        Assert.assertEquals(1, keys.size());
        Assert.assertTrue(keys.get(0).contains("wh-1"));
        Assert.assertTrue(keys.get(0).contains("sku-1"));
    }
}
