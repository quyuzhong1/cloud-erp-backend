package com.erp.server.oms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class OrderCategoryQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("name".equals(field)){
            return "((oc.group_name "+compareCodeSplicingValueSql+") or (oc.id in (SELECT main_id FROM order_category_detail WHERE is_deleted=FALSE AND name " + compareCodeSplicingValueSql + ")))";
        }
        return null;
    }
}

