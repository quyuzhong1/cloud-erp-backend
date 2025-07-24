package com.erp.server.sys.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;


@Component
public class TemplateManagementQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }

    /**
     * @description: tabSql拼接
     * @author jack
     * @date: 2025-07-24
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        if(value.equals("all") || value.equals("")){
            return "";
        }
        if ("false".equals(value)) {
            return "tm.disabled ="+ Boolean.TRUE;
        }
        if ("true".equals(value)) {
            return "tm.disabled ="+ Boolean.FALSE;
        }
        return "";
    }
}
