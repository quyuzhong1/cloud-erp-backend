package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 物流商对账明细（行级）高级查询
 * @author Will
 * @date 2026/6/1 10:30
 */
@Component
public class LogisticsReconDetailQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return "";
    }
}
