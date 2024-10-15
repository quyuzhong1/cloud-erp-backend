package com.erp.server.mrp.handler;


import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
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
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
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
            return "EXISTS (select 1 from replenishment_ref_label where ref_id = rsd.id AND is_deleted = false AND label_id " + compareCodeSplicingValueSql + ")";
        }
        if("markType".equals(field)) {
            return "EXISTS ( SELECT 1 FROM recent_suggestion_detail WHERE replenishment_detail_id = rsd.id AND is_deleted = false AND mark_type " + compareCodeSplicingValueSql + ")";
        }
        if("restockDate".equals(field)) {
            if (QueryConditionEnum.BETWEEN.equals(queryConditionEnum)) {
                String compareCodeSplicingValueSql1 = compareCodeSplicingValueSql.replace("restockDate", "suggest_delivery_date");
                String compareCodeSplicingValueSql2 = compareCodeSplicingValueSql.replace("restockDate", "suggest_purchase_date");
                return "EXISTS (SELECT 1 FROM delivery_suggest WHERE source_id = rsd.id AND is_deleted = FALSE AND suggest_delivery_date " + compareCodeSplicingValueSql1 + ")  OR EXISTS  (SELECT 1 FROM purchase_suggest WHERE source_id = rsd.id AND is_deleted = FALSE AND suggest_purchase_date " + compareCodeSplicingValueSql2 + ")";

            }
            return "EXISTS (SELECT 1 FROM delivery_suggest WHERE source_id = rsd.id AND is_deleted = FALSE AND suggest_delivery_date " + compareCodeSplicingValueSql + ")  OR EXISTS (SELECT 1 FROM purchase_suggest WHERE source_id = rsd.id AND is_deleted = FALSE AND suggest_purchase_date " + compareCodeSplicingValueSql + ")";
        }
        if("outOfStockDay".equals(field)) {
            if (QueryConditionEnum.BETWEEN.equals(queryConditionEnum)) {
                compareCodeSplicingValueSql = compareCodeSplicingValueSql.replace("outOfStockDay", "date");
            }
            return "EXISTS ( SELECT 1 FROM recent_suggestion_detail WHERE replenishment_detail_id = rsd.id AND is_deleted = false AND type = 'RECENT_OUT_OF_STOCK' AND date  " + compareCodeSplicingValueSql + ")";
        }
        if("suggestDeliveryDate".equals(field)) {
            if (QueryConditionEnum.BETWEEN.equals(queryConditionEnum)) {
                compareCodeSplicingValueSql = compareCodeSplicingValueSql.replace("suggestDeliveryDate", "date");
            }
            return "EXISTS ( SELECT 1 FROM recent_suggestion_detail WHERE replenishment_detail_id = rsd.id AND is_deleted = false AND type = 'RECENT_DELIVERY' AND date " + compareCodeSplicingValueSql + ")";
        }
        if("suggestPurchaseDate".equals(field)) {
            if (QueryConditionEnum.BETWEEN.equals(queryConditionEnum)) {
                compareCodeSplicingValueSql = compareCodeSplicingValueSql.replace("suggestPurchaseDate", "date");
            }
            return "EXISTS ( SELECT 1 FROM recent_suggestion_detail WHERE  replenishment_detail_id = rsd.id AND is_deleted = false AND type = 'RECENT_PURCHASE' AND  date " + compareCodeSplicingValueSql + ")";
        }
        return null;
    }
}
