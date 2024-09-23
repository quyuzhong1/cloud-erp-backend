package com.erp.server.mrp.handler;


import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class ReplenishmentSuggestionQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("category".equals(field)) {

        }
        if("label".equals(field)) {

        }
        if("markType".equals(field)) {

        }
        if("restockDate".equals(field)) {

        }
        if("outOfStockDay".equals(field)) {

        }
        if("suggestDeliveryDate".equals(field)) {

        }
        if("suggestPurchaseDate".equals(field)) {

        }
        return null;
    }
}
