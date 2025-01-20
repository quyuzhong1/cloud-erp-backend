package com.erp.server.plm.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.plm.enums.ApprovalStatusEnum;
import com.erp.model.plm.enums.ProjectReportStatusEnum;
import com.erp.model.plm.enums.ProjectStateEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectReportFormsQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("sku_no".equals(field)) {
            return " pi.id IN ( SELECT product_id FROM product_detail pd WHERE pd.sku_no "+ compareCodeSplicingValueSql + " GROUP BY product_id)";
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

