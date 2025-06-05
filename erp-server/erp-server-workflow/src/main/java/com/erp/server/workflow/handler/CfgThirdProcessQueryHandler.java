package com.erp.server.workflow.handler;


import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-26
 *@Description:
 *@Version: 1.0
 */
@Component

public class CfgThirdProcessQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            if(value.equals("all") || value.equals("")){
                return "";
            }
            if ("true".equals(value)) {
                return "ctp.enable_status ="+ Boolean.TRUE;
            }
            if ("false".equals(value)) {
                return "ctp.enable_status ="+ Boolean.FALSE;
            }
            return "";
        }
        return null;
    }
}
