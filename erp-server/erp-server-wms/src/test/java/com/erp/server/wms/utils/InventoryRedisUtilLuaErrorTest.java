package com.erp.server.wms.utils;

import com.erp.server.wms.util.InventoryUnallocCheckHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link InventoryRedisUtil} Lua 业务错误识别与 ApiError 映射前置解析。
 */
public class InventoryRedisUtilLuaErrorTest {

    /**
     * 约定前缀直连时应识别为未分配业务错误正文。
     */
    @Test
    public void resolvesDirectUnallocPrefix() {
        String msg = InventoryUnallocCheckHelper.UNALLOC_LUA_ERROR_PREFIX + "sku不足";
        Assert.assertEquals(msg, InventoryRedisUtil.resolveLuaBusinessErrorBody(msg));
    }

    /**
     * Redis {@code ERR } 包裹时应剥离前缀并识别业务正文。
     */
    @Test
    public void resolvesErrPrefixedUnallocMessage() {
        String inner = InventoryUnallocCheckHelper.UNALLOC_LUA_ERROR_PREFIX + "sku不足";
        Assert.assertEquals(inner, InventoryRedisUtil.resolveLuaBusinessErrorBody("ERR " + inner));
    }

    /**
     * 异常链外层无 ERR、内层为约定前缀时应沿 cause 识别。
     */
    @Test
    public void resolvesWrappedExceptionChain() {
        String inner = InventoryUnallocCheckHelper.UNALLOC_LUA_ERROR_PREFIX + "sku不足";
        Exception cause = new RuntimeException(inner);
        Exception wrapper = new RuntimeException("Error in execution", cause);
        Assert.assertEquals(inner, InventoryRedisUtil.resolveLuaBusinessErrorMessage(wrapper));
    }

    /**
     * 非约定 ERR（如 WRONGTYPE）不得误判为 Lua 业务失败。
     */
    @Test
    public void ignoresNonBusinessErrMessage() {
        Assert.assertNull(InventoryRedisUtil.resolveLuaBusinessErrorBody("ERR WRONGTYPE Operation against a key"));
    }

    /**
     * Lua 脚本运行时错误（user_script）应识别为运行时失败摘要。
     */
    @Test
    public void resolvesLuaRuntimeErrorFromUserScript() {
        String raw = "ERR Error running script (null): @user_script:390: attempt to concatenate local 'beforeinv' (a boolean value)";
        Assert.assertEquals(
                "@user_script:390: attempt to concatenate local 'beforeinv' (a boolean value)",
                InventoryRedisUtil.resolveLuaRuntimeErrorMessage(new RuntimeException(raw)));
    }

    /**
     * 普通 Redis 连接异常不得误判为 Lua 运行时错误。
     */
    @Test
    public void ignoresNonLuaRuntimeMessage() {
        Assert.assertNull(InventoryRedisUtil.resolveLuaRuntimeErrorMessage(
                new RuntimeException("RedisConnectionFailureException: Connection reset")));
    }
}
