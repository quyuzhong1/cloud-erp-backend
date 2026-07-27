package com.erp.server.oms.query;

/**
 * 预入库-关联售后单：高级查询上下文
 * <p>该分页接口的单据类型、仓库等条件均通过高级查询传入，且 B2B / B2C 仓库分属不同表。
 * 高级查询处理类在处理 billType 字段时把单据类型写入本上下文，供 warehouseId 字段判定仓库表别名；
 * Service 亦据此路由 B2B / B2C mapper，并在查询结束后清理，避免线程复用产生脏值。</p>
 *
 * @author nie
 * @date 2026/7/3
 */
public class LinkAfterSaleQueryContext {

    private LinkAfterSaleQueryContext() {
    }

    private static final ThreadLocal<String> BILL_TYPE_HOLDER = new ThreadLocal<>();

    public static void setBillType(String billType) {
        BILL_TYPE_HOLDER.set(billType);
    }

    public static String getBillType() {
        return BILL_TYPE_HOLDER.get();
    }

    public static void remove() {
        BILL_TYPE_HOLDER.remove();
    }
}
