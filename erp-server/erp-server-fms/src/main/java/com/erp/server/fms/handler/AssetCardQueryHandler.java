package com.erp.server.fms.handler;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 资产卡片高级查询处理器
 *
 * @author wuht
 * @date 2025-10-31
 */
@Component
public class AssetCardQueryHandler extends AbstractQueryHandler {
    
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    /**
     * 处理标签页查询
     * 
     * @param value 标签页值
     * @return SQL字符串
     */
    public String getTabSql(Object value) {
        if ("all".equals(value) || "".equals(value)) {
            return getQueryAllSql();
        }
        super.buildDefaultDTO("ac.approve_status", value);
        return super.getSplicingSQL();
    }
}

