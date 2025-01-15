package com.erp.server.plm.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import org.springframework.stereotype.Component;

@Component
public class ProjectReportFormsQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("sku_no".equals(field)) {
            return "exists ( SELECT id FROM product_detail pd WHERE pd.sku_no "+ compareCodeSplicingValueSql + ")";
        }
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
        return null;
    }

}

