package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;


@Component
public class TmsB2BDeclareQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("supplier".equals(field)) {
            // 通过中间表把 B2B 报关单关联到发货通知单（source_type='soDeliveryNotice'），再比对承运商 id
            return " exists (" +
                    "select 1 from delivery_declare_detail_mid m " +
                    "inner join so_delivery_notice sdn on sdn.id = m.source_id " +
                    "where m.declare_id = db.id and m.is_deleted = false " +
                    "and m.source_type = 'soDeliveryNotice' " +
                    "and sdn.carrier_id " + compareCodeSplicingValueSql +
                    " )";
        }
        return null;
    }
}
