package com.erp.server.tms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class LogisticsLargeQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("ll.reconciliation_month".equals(field)) {
            super.buildDefaultDTO("TO_CHAR(ll.reconciliation_month, 'yyyy-MM')", value);
        }
        return super.getSplicingSQL();
    }


}
