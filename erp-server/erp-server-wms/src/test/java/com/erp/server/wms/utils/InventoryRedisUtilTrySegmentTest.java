package com.erp.server.wms.utils;

import com.common.core.exception.ServiceException;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link InventoryRedisUtil} TRY 片段解析与 current 汇总校验。
 */
public class InventoryRedisUtilTrySegmentTest {

    /**
     * 二段 TRY（txn@@qty）应正确计入负 TRY 与当前事务 TRY。
     */
    @Test
    public void computeCurrentQtyWithTrySupportsTwoFieldSegment() {
        String redisValue = "100&&txn-a@@-5&&txn-b@@-3";
        int qty = InventoryRedisUtil.computeCurrentQtyWithTry(redisValue, "txn-b");
        Assert.assertEquals(92, qty);
    }

    /**
     * 三段 TRY（txn@@operationId@@qty）数量应取第三段，避免误把 operationId 当数量。
     */
    @Test
    public void computeCurrentQtyWithTrySupportsThreeFieldSegment() {
        String redisValue = "100&&txn-a@@op-1@@-5&&txn-b@@op-2@@-3";
        int qty = InventoryRedisUtil.computeCurrentQtyWithTry(redisValue, "txn-b");
        Assert.assertEquals(92, qty);
    }

    /**
     * 非当前事务的正 TRY 不应计入可用量。
     */
    @Test
    public void computeCurrentQtyWithTryIgnoresOtherPositiveTry() {
        String redisValue = "100&&txn-a@@op-1@@3";
        int qty = InventoryRedisUtil.computeCurrentQtyWithTry(redisValue, "other-txn");
        Assert.assertEquals(100, qty);
    }

    /**
     * 非法 TRY 片段 fail-closed，不得当作 0 忽略。
     */
    @Test(expected = ServiceException.class)
    public void computeCurrentQtyWithTryRejectsInvalidTrySegment() {
        InventoryRedisUtil.computeCurrentQtyWithTry("100&&txn-a@@op-1@@not-a-number", "other-txn");
    }
}
