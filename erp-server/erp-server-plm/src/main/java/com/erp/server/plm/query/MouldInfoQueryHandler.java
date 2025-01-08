package com.erp.server.plm.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class MouldInfoQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

        if("supplierId".equals(field)){
            return "EXISTS (select 1 from supplier where md.supplier_id = id  and id " + compareCodeSplicingValueSql + ")";
        }
        if("productName".equals(field)){
            return "EXISTS (select 1 from mould_product where md.id = mould_detail_id and product_name " + compareCodeSplicingValueSql + ")";
        }
        return null;
    }
}
