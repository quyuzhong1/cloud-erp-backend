package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class VirtualInventoryAgeQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("isDiff".equals(field)) {
            if ((Boolean) value) {
                return "(case when vih.virtualQty = 0 then 0.00 else round(vidh.inventoryAgeQty * 1.0 / vih.virtualQty,4) end) != (case when vih.virtualQty = 0 then 0.00 else round(vidh.backInventoryAgeQty / vih.virtualQty,4) end)";
            } else {
                return "(case when vih.virtualQty = 0 then 0.00 else round(vidh.inventoryAgeQty * 1.0 / vih.virtualQty,4) end) = (case when vih.virtualQty = 0 then 0.00 else round(vidh.backInventoryAgeQty / vih.virtualQty,4) end)";
            }
        }
        return null;
    }
}
