package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class WmsInventoryQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("dimension".equals(field)) {
            // 移除tab查询
            return getQueryAllSql();
        }
        return null;
    }
}

