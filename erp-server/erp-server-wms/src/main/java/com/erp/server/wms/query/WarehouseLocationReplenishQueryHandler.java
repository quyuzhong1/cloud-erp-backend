package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 仓位库存预警 高级查询处理器
 * @date 2024-06-24
 * @author tanmujin
 */
@Component
public class WarehouseLocationReplenishQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
