package com.erp.server.wms.handler;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import org.springframework.stereotype.Component;

@Component
public class InventoryQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        if ("ps.sale_state".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "ps.sale_state is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "ps.sale_state is not null";
            }
            return " ps.sale_state " + compareCodeSplicingValueSql;
        }
        if ("pd.status".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "pd.status is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "pd.status is not null";
            }
            return " pd.status " + compareCodeSplicingValueSql;
        }
        if ("pi.pirate_risk".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "pi.pirate_risk is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "pi.pirate_risk is not null";
            }
            return " pi.pirate_risk " + compareCodeSplicingValueSql;
        }
        return null;
    }

}

