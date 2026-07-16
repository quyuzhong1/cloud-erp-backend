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
        if (isMoldNameField(field)) {
            super.buildDefaultDTO("mi.name", value);
            return super.getSplicingSQL();
        }
        return null;
    }

    private boolean isMoldNameField(String field) {
        return "moldName".equals(field) || "cmr.mold_name".equals(field);
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        super.buildDefaultDTO("cmr.disabled", value);
        return super.getSplicingSQL();
    }
}
