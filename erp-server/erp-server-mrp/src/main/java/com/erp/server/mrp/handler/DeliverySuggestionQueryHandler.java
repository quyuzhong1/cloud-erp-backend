package com.erp.server.mrp.handler;


import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class DeliverySuggestionQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("isPush".equals(field) && (Boolean) value){
            return "EXISTS(select 1 from wms_delivery_plan_detail, jsonb_array_elements(source_json) AS elem where is_deleted = false and elem ->>'sourceId' = ds.id)";
        }
        if("isPush".equals(field) && !(Boolean) value){
            return "not EXISTS(select 1 from wms_delivery_plan_detail, jsonb_array_elements(source_json) AS elem where is_deleted = false and elem ->>'sourceId' = ds.id)";
        }
        if("deliveryPlanCode".equals(field)){

            return "ds.id in (select jsonb_array_elements(source_json)->>'sourceId' from wms_delivery_plan_detail, jsonb_array_elements(source_json) AS elem where is_deleted = false and elem ->>'sourceCode' "+compareCodeSplicingValueSql+")";
        }
        return null;
    }
}
