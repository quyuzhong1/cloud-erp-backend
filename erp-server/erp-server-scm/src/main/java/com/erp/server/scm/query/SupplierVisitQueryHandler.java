package com.erp.server.scm.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;


@Component
public class SupplierVisitQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        if("people".equals(field)){
            return getTabSql(value);
        }
        if("sku_id".equals(field)){
            return " sv.id in (select DISTINCT supplier_visit_id from supplier_visit_sku where sku_id " + compareCodeSplicingValueSql+" ";
        }
        return null;
    }


    /**
     * @description: tabSql拼接
     * @author jack
     * @date: 2025-06-26
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        if(value.equals("all") || value.equals("")){
            return "";
        }
        return "sv.result ='"+value+"'";
    }
}
