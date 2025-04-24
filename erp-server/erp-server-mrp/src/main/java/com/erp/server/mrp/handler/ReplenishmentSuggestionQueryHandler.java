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

    private static final String FIELD_NAME = "restockDate";

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        List<String> skuIds;
        switch (field) {
            case "category_id":
            case "brand_id":
                skuIds = "category_id".equals(field)
                        ? plmTaskFeign.getCategoryByQuerySql(compareCodeSplicingValueSql)
                        : plmTaskFeign.getBrandByQuerySql(compareCodeSplicingValueSql);
                if (CollectionUtils.isEmpty(skuIds)) {
                    return getQueryEmptySql();
                }
                super.buildSplicingSQLDTO("rs.sku_id", QueryConditionEnum.IN_LIST, skuIds, QueryDataTypeEnum.STRING);
                return null;
            case "label":
                return "EXISTS (select 1 from replenishment_ref_label where ref_id = rs.id AND is_deleted = false AND label_id " + compareCodeSplicingValueSql + ")";
            case "markType":
                return "EXISTS ( SELECT 1 FROM recent_suggestion_detail WHERE replenishment_detail_id = rsd.id AND is_deleted = false AND mark_type " + compareCodeSplicingValueSql + ")";
            case FIELD_NAME:
                return handleDateConditions(queryConditionEnum, compareCodeSplicingValueSql);
            case "outOfStockDay":
                return buildExistsSql(handleBetweenCondition(queryConditionEnum, compareCodeSplicingValueSql, "outOfStockDay"), "RECENT_OUT_OF_STOCK");
            case "suggestDeliveryDate":
                return buildExistsSql(handleBetweenCondition(queryConditionEnum, compareCodeSplicingValueSql, "suggestDeliveryDate"), "RECENT_DELIVERY");
            case "suggestPurchaseDate":
                return buildExistsSql(handleBetweenCondition(queryConditionEnum, compareCodeSplicingValueSql, "suggestPurchaseDate"), "RECENT_PURCHASE");
            default:
                return null;

        }

    }

    private String handleBetweenCondition(QueryConditionEnum queryConditionEnum, String compareSql, String originalField) {
        return QueryConditionEnum.BETWEEN.equals(queryConditionEnum) ? compareSql.replace(originalField, "date") : compareSql;
    }

    private String buildExistsSql(String compareSql, String type) {
        return "EXISTS (SELECT 1 FROM recent_suggestion_detail WHERE replenishment_detail_id = rsd.id AND is_deleted = false AND type = '" + type + "' AND date " + compareSql + ")";
    }

    private String handleDateConditions(QueryConditionEnum queryConditionEnum, String compareCodeSplicingValueSql) {
        if (QueryConditionEnum.BETWEEN.equals(queryConditionEnum)) {
            String compareCodeSplicingValueSql1 = compareCodeSplicingValueSql.replace(FIELD_NAME, "suggest_delivery_date");
            String compareCodeSplicingValueSql2 = compareCodeSplicingValueSql.replace(FIELD_NAME, "suggest_purchase_date");
            return "(EXISTS (SELECT 1 FROM delivery_suggest WHERE source_id = rs.id AND is_deleted = FALSE AND suggest_delivery_date " + compareCodeSplicingValueSql1 + ")" +
                    "  OR EXISTS  (SELECT 1 FROM purchase_suggest WHERE source_id = rs.id AND is_deleted = FALSE AND suggest_purchase_date " + compareCodeSplicingValueSql2 + " ))";
        }
        return "(EXISTS (SELECT 1 FROM delivery_suggest WHERE source_id = rs.id AND is_deleted = FALSE AND suggest_delivery_date " + compareCodeSplicingValueSql + ") " +
                " OR EXISTS (SELECT 1 FROM purchase_suggest WHERE source_id = rs.id AND is_deleted = FALSE AND suggest_purchase_date " + compareCodeSplicingValueSql + "))";
    }
}
