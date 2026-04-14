package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class QcSamplingPlanQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("qspsr.sku_no".equals(field)) {
            return "EXISTS (SELECT 1 FROM qc_sampling_plan_sku_ref qspsr WHERE qspsr.main_id = qsp.id and qspsr.is_deleted = FALSE " +
                    "AND qspsr.sku_no " + compareCodeSplicingValueSql + ") OR " +
                    "NOT EXISTS (SELECT 1 FROM qc_sampling_plan_sku_ref qspsr WHERE qspsr.main_id = qsp.id and qspsr.is_deleted = FALSE)";
        }
        return null;
    }
}

