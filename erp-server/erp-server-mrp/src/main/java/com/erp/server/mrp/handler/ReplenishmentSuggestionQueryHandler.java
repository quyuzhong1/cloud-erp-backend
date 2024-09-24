package com.erp.server.mrp.handler;


import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ReplenishmentSuggestionQueryHandler extends AbstractQueryHandler {

    @Resource
     private PlmTaskFeign plmTaskFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("category_id".equals(field)) {
            List<String> skuIds = plmTaskFeign.getCategoryByQuerySql(compareCodeSplicingValueSql);
            if(CollectionUtils.isEmpty(skuIds)){
                return this.getQueryEmptySql();
            }
            super.buildSplicingSQLDTO("rs.sku_id", QueryConditionEnum.IN_LIST,skuIds, QueryDataTypeEnum.STRING);
        }
        if ("brand_id".equals(field)) {
            List<String> skuIds = plmTaskFeign.getBrandByQuerySql(compareCodeSplicingValueSql);
            if(CollectionUtils.isEmpty(skuIds)){
                return this.getQueryEmptySql();
            }
            super.buildSplicingSQLDTO("rs.sku_id", QueryConditionEnum.IN_LIST,skuIds, QueryDataTypeEnum.STRING);
        }
        if("label".equals(field)) {
            return "EXISTS (select ref_id from replenishment_ref_label where is_deleted = false AND label_id " + compareCodeSplicingValueSql + ")";
        }
        if("markType".equals(field)) {
            return "EXISTS ( SELECT replenishment_detail_id FROM recent_suggestion_detail WHERE is_deleted = false AND mark_type " + compareCodeSplicingValueSql + ")";
        }
        if("restockDate".equals(field)) {
            return "EXISTS ((SELECT source_id FROM delivery_suggest WHERE is_deleted = FALSE AND suggest_delivery_date " + compareCodeSplicingValueSql + ")  OR (SELECT source_id FROM purchase_suggest WHERE is_deleted = FALSE AND suggest_purchase_date " + compareCodeSplicingValueSql + "))";
        }
        if("outOfStockDay".equals(field)) {
            return "EXISTS ( SELECT replenishment_detail_id FROM recent_suggestion_detail WHERE is_deleted = false AND type = 'RECENT_OUT_OF_STOCK' AND date  " + compareCodeSplicingValueSql + ")";
        }
        if("suggestDeliveryDate".equals(field)) {
            return "EXISTS ( SELECT replenishment_detail_id FROM recent_suggestion_detail WHERE is_deleted = false AND type = 'RECENT_DELIVERY' AND date " + compareCodeSplicingValueSql + ")";
        }
        if("suggestPurchaseDate".equals(field)) {
            return "EXISTS ( SELECT replenishment_detail_id FROM recent_suggestion_detail WHERE is_deleted = false AND type = 'RECENT_PURCHASE' AND  date " + compareCodeSplicingValueSql + ")";
        }
        return null;
    }
}
