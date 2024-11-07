package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryInterceptStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class SoB2cDeliveryInterceptQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }

    /**
     * @description: tabSql
     * @author Will
     * @date: 2024/2/26 15:55
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        if (SoB2cDeliveryInterceptStatusEnum.WAIT_HANDLE.getCode().equals(value)){
            return "sbdi.handle_status = 'waitHandle'";
        }else if (SoB2cDeliveryInterceptStatusEnum.HANDLE.getCode().equals(value)){
            return "sbdi.handle_status = 'handle'";
        }else if (SoB2cDeliveryInterceptStatusEnum.CANCEL.getCode().equals(value)){
            return "sbdi.handle_status = 'cancel'";
        }
        return super.getSplicingSQL();
    }
}

