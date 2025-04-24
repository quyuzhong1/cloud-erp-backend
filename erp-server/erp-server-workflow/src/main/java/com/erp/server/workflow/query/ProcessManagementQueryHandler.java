package com.erp.server.workflow.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.rpc.oms.feign.ShopSysUserAuthFeign;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class ProcessManagementQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        if ("pm.process_version".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "pm.process_version is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "pm.process_version is not null";
            }
            return " pm.process_version " + compareCodeSplicingValueSql;
        }
        return null;
    }
}
