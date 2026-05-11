package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 仓位售后推荐表搜索条件
 *
 * @author liuchao
 * @since 2026-04-30
 */
@Component
public class AfterSalesWarehouseLocationSuggestQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
