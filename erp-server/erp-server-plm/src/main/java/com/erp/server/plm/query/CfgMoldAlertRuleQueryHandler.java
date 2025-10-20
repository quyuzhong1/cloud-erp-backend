package com.erp.server.plm.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.workflow.WorkflowFeign;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CfgMoldAlertRuleQueryHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        super.buildDefaultDTO("cmr.disabled", value);
        return super.getSplicingSQL();
    }
}
