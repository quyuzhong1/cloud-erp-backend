package com.erp.server.wms.inventory;

import org.junit.Test;

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
}
