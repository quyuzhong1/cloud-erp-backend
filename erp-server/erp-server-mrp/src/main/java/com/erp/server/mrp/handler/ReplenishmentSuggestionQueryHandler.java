package com.erp.server.mrp.handler;


import com.common.business.query.AbstractQueryHandler;
import com.erp.server.mrp.service.ReplenishmentRefLabelService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ReplenishmentSuggestionQueryHandler extends AbstractQueryHandler {

    @Resource
     private ReplenishmentRefLabelService replenishmentRefLabelService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("category".equals(field)) {

        }
        if ("brand".equals(field)) {

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
