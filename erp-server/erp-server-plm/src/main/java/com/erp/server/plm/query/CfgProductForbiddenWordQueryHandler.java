package com.erp.server.plm.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 产品违禁词库高级搜索。
 */
@Component
public class CfgProductForbiddenWordQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    private String getTabSql(Object value) {
        String tabValue = value == null ? "" : value.toString();
        if ("all".equals(tabValue) || "".equals(tabValue)) {
            return getQueryAllSql();
        }
        if ("f".equals(tabValue)) {
            return " cpfw.disabled = false ";
        }
        if ("t".equals(tabValue)) {
            return " cpfw.disabled = true ";
        }
        // 未知页签值不应退化为查询全部数据。
        return getQueryEmptySql();
    }
}
