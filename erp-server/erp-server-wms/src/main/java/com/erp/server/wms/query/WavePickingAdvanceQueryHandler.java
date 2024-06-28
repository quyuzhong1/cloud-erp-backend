package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 波次拣货高级查询
 * @date 2024-06-20
 * @author tanmujin
 */
@Component
public class WavePickingAdvanceQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
