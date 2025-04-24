package com.erp.server.plm.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderTrackingHandler  extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

        if("skuNo".equals(field)){
            return "EXISTS (select 1 from mould_ref_product where is_deleted = false and md.id = mould_detail_id and sku_no " + compareCodeSplicingValueSql + ")";
        }
        return null;
    }
}