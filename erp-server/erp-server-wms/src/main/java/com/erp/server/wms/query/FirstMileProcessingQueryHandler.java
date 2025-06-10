package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class FirstMileProcessingQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //发货状态
        if("deliveryApproveStatus".equals(field)){
            return "(fmp.delivery_approve_status " + compareCodeSplicingValueSql + " or exists (select 0 from first_mile_processing_detail where is_deleted = false and delivery_approve_status " + compareCodeSplicingValueSql + " and outstock_order_id !='' and main_id = fmp.id)) ";
        }
        //头程发货单编码
        if("firstMileDeliveryCode".equals(field)){
            return "(fmp.first_mile_delivery_code " + compareCodeSplicingValueSql + " or exists (select 0 from first_mile_processing_detail where is_deleted = false and first_mile_delivery_code " + compareCodeSplicingValueSql + " and outstock_order_id !='' and main_id = fmp.id)) ";
        }
        return null;
    }
}

