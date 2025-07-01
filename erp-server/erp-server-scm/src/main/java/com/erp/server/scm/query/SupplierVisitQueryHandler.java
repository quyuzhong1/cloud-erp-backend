package com.erp.server.scm.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.query.AbstractQueryHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class SupplierVisitQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        if("people".equals(field)){
            return " sv.id in (select DISTINCT svp.id from (select id,unnest(string_to_array(people, ',')) as user_id  from supplier_visit) as svp where svp.user_id "+compareCodeSplicingValueSql+" ) ";
        }
        if("sku_no".equals(field)){
            if(compareCodeSplicingValueSql.contains("not") || compareCodeSplicingValueSql.contains("!=")){
                return " NOT EXISTS ( SELECT 1 FROM supplier_visit_sku svs WHERE svs.supplier_visit_id = sv.ID   AND svs.is_deleted = FALSE AND not ( svs.sku_no "+compareCodeSplicingValueSql+" )) ";
            }else {
                return " sv.id in (select supplier_visit_id from supplier_visit_sku where sku_no "+compareCodeSplicingValueSql+" ) ";
            }
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
