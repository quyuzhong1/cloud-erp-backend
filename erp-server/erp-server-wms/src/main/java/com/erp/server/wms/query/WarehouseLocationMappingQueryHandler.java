package com.erp.server.wms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import org.springframework.stereotype.Component;

/**
 * 仓位绑定高级查询扩展字段处理
 */
@Component
public class WarehouseLocationMappingQueryHandler extends AbstractQueryHandler {

    private static final String SYS_WAREHOUSE_NAME = "sysWarehouseName";

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if (SYS_WAREHOUSE_NAME.equals(field)) {
            return buildWarehouseNameSql(compareCodeSplicingValueSql);
        }
        return null;
    }

    private String buildWarehouseNameSql(String compareCodeSplicingValueSql) {
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
            return "sys_warehouse_id in (select w.id from warehouse w where w.is_deleted = false and (w.name = '' or w.name is null))";
        }
        if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
            return "sys_warehouse_id in (select w.id from warehouse w where w.is_deleted = false and w.name != '')";
        }
        String field = QueryConditionEnum.SET_LIKE.contains(queryConditionEnum) ? "LOWER(w.name)" : "w.name";
        return "sys_warehouse_id in (select w.id from warehouse w where w.is_deleted = false and "
                + field + " " + compareCodeSplicingValueSql + ")";
    }
}
