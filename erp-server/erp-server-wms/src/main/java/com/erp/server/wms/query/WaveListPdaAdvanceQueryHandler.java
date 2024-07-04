package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 波次列表（PDA）高级查询
 * @date 2024-07-02
 * @author tanmujin
 */
@Component
public class WaveListPdaAdvanceQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
