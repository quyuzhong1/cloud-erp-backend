package com.erp.server.wms.query;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class PoInStockQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)) {
            if (CharSequenceUtil.isBlank(value.toString())) {
                return getQueryAllSql();
            }
            this.buildDefaultDTO("psi.approve_status",value);
            this.buildSplicingSQLDTO("psi.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        if("deliveryCode".equals(field)) {
            return "exists (select 1 from po_receive where is_deleted = false and id = psi.source_id  and source_code "+compareCodeSplicingValueSql +")";
        }
        return null;
    }
}
