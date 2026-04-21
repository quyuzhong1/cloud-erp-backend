package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.SoB2bDeliveryInterceptStatusEnum;
import org.springframework.stereotype.Component;

/**
 * B2B发货拦截单查询处理
 */
@Component
public class SoB2bDeliveryInterceptQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    private String getTabSql(Object value) {
        if (SoB2bDeliveryInterceptStatusEnum.WAIT_HANDLE.getCode().equals(value)) {
            return "coalesce(nullif(sbdi.handle_status, ''), 'waitHandle') = 'waitHandle'";
        } else if (SoB2bDeliveryInterceptStatusEnum.HANDLE.getCode().equals(value)) {
            return "coalesce(nullif(sbdi.handle_status, ''), 'waitHandle') = 'handle'";
        }
        return super.getSplicingSQL();
    }
}
