package com.erp.server.mrp.handler;


import com.common.business.query.AbstractQueryHandler;
import com.erp.model.mrp.enums.DeliverySuggestTabEnum;
import com.erp.model.mrp.enums.SuggestStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PurchaseSuggestionMergeQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    /**
     * tab
     * @author will
     * @date 2024/12/16 15:33
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 草稿
        if (DeliverySuggestTabEnum.DRAFT.getCode().equals(value)) {
            super.buildDefaultDTO("psm.status", Collections.singletonList(SuggestStatusEnum.DRAFT.getCode()));
            super.buildDefaultDTO("psm.invalid_status", Collections.singletonList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
        }
        //待确认
        if (DeliverySuggestTabEnum.WAIT_CONFIRM.getCode().equals(value)) {
            super.buildDefaultDTO("psm.status", Collections.singletonList(SuggestStatusEnum.WAIT_CONFIRM.getCode()));
            super.buildDefaultDTO("psm.invalid_status", Collections.singletonList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
        }
        //未下推
        if (DeliverySuggestTabEnum.WAIT_PUSH.getCode().equals(value)) {
            return "not EXISTS(select 1 from foreign_purchase_application_detail, jsonb_array_elements(merge_id_json) AS elem where is_deleted = false and elem::text = psm.id) and psm.status = 'finish' and psm.invalid_status = false";
        }
        return super.getSplicingSQL();
    }
}
