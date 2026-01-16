package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 仓位移动搜索条件
 *
 * @author hyj
 * @date 2024/5/22
 */
@Component
public class AwdShipmentQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("deliveryCode".equals(field)){
            return " EXISTS (SELECT FROM first_mile_delivery fd INNER JOIN first_mile_delivery_detail fdd " +
                    "ON fdd.main_id = fd.id AND fdd.is_deleted = FALSE WHERE fdd.fba_shipment_code = fs.code AND fd.is_deleted = FALSE " +
                    "and fd.code " + compareCodeSplicingValueSql + " )";
        }
        return null;
    }


}
