package com.erp.server.sys.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;


@Component
public class CfgThirdNoticeQueryHandler extends AbstractQueryHandler {


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
            return "ctn.notice_status ="+ Boolean.TRUE;
        }
        if ("false".equals(value)) {
            return "ctn.notice_status ="+ Boolean.FALSE;
        }
        return "";
    }
}
