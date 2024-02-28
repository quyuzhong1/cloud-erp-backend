package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import org.springframework.stereotype.Component;

@Component
public class SupplierDeliveryQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("do2.tab".equals(field)){
            if(DeliveryOrderEnum.SearchTypeEnum.ALL.getCode().equals(value)){
                return super.getQueryAllSql();
            }
            if(DeliveryOrderEnum.SearchTypeEnum.WAIT_RECEIVE.getCode().equals(value)){
                return " do2.receipt_status != 'confirmed' ";
            }
            if(DeliveryOrderEnum.SearchTypeEnum.RECEIVED.getCode().equals(value)){
                return " do2.receipt_status = 'confirmed' ";
            }
            if(DeliveryOrderEnum.SearchTypeEnum.QTY_DIFFERENCE.getCode().equals(value)){
                return " do2.receipt_status = 'confirmed' and doe.delivery_qty != doe.receive_qty";
            }
        }
        return null;
    }
}
