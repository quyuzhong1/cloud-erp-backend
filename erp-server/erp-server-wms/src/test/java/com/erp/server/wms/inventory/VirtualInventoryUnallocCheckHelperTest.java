package com.erp.server.wms.inventory;

import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

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
     * virtualQty&gt;0 且 inventoryIds 为空时应 fail-closed。
     */
    @Test(expected = ServiceException.class)
    public void assertUnallocTryItemsHaveInventoryIdsRejectsEmptyList() {
        VirtualInventoryUnallocCheckHelper.UnallocTryItem item = new VirtualInventoryUnallocCheckHelper.UnallocTryItem(
                "wh-1", "sku-1", "SKU001", "WH-A", 5, 10, Collections.emptyList());
        VirtualInventoryUnallocCheckHelper.assertUnallocTryItemsHaveInventoryIds(Collections.singletonList(item));
    }
}
