package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class SoDeliveryNoticeChangeHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("isAllPush".equals(field)){
            if((Boolean) value){
                return "(select  COALESCE(sum(a.delivery_qty),0) from so_delivery_notice_detail a where a.source_detail_id = sd.id and a.is_deleted = false) = sd.qty";
            }else{
                return "(select  COALESCE(sum(a.delivery_qty),0) from so_delivery_notice_detail a where a.source_detail_id = sd.id and a.is_deleted = false) != sd.qty";
            }
        }
        return null;
    }

}
