package com.common.business.query.impl;

import com.common.business.query.IQueryHandler;
import org.springframework.stereotype.Component;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class PurchaseOrderQueryHandler implements IQueryHandler {

    @Override
    public String splicingSQL(String field, String compareCode, String value, String compareCodeSplicingValueSql) {
        if("so.code".equals(field)){
            return "case when  subString ( " + value + ",1,2) != 'PL' then po.source_code "+ compareCodeSplicingValueSql +
                    " else EXISTS ( select pa.id from purchase_application pa left join purchase_application_ref_po parp on parp.is_deleted = false and pa.id = parp.purchase_application_id " +
                    " where parp.purchase_order_id = po.id and pa.code "+ compareCodeSplicingValueSql +" ) end";
        }
        return null;
    }
}

