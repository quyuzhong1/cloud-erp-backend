package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 
 * @date 2024-06-21
 * @author tanmujin
 */
@Component
public class WarehouseLocationSafetyInventoryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
