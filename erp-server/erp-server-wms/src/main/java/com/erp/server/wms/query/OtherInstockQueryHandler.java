package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @description: 其他入库查询
 * @author Will
 * @date: 2024/2/26 14:56
 */
@Component
public class OtherInstockQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

        return null;
    }
}

