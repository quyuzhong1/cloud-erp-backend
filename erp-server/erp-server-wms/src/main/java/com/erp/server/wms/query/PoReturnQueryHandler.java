package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class PoReturnQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("returnOrderSource".equals(field)){
            value = "'"+value+"'";
            return "case when  subString ( " + value + ",1,2) != 'PL' then po.source_code "+ compareCodeSplicingValueSql +
                    " else EXISTS ( select pa.id from purchase_application pa left join purchase_application_ref_po parp on parp.is_deleted = false and pa.id = parp.purchase_application_id " +
                    " where parp.purchase_order_id = po.id and pa.code "+ compareCodeSplicingValueSql +" ) end";
        }
        return null;
    }
}
