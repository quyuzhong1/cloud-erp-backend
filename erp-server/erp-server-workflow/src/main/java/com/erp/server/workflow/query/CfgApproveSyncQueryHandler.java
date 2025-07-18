package com.erp.server.workflow.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.workflow.enums.ProcessDelegateStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;


@Component
public class CfgApproveSyncQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }

    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2024/1/18 19:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        if(value.equals("all") || value.equals("")){
            return "";
        }
        if ("true".equals(value)) {
            return "cas.enable_status ="+ Boolean.TRUE;
        }
        if ("false".equals(value)) {
            return "cas.enable_status ="+ Boolean.FALSE;
        }
        return "";
    }
}
