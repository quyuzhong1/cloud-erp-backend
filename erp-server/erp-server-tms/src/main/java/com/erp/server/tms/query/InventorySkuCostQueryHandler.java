package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @author zdy
 * @ClassName InventorySkuCostQueryHandler
 * @description: TODO
 * @date 2024年08月16日
 * @version: 1.0
 */
@Component
public class InventorySkuCostQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
