package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class SupplierPoReturnQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("pro.source_type".equals(field)){
            if ("other".equals(value)) {
                return "pro.source_type != 'qcInfo'";
            } else {
                return "pro.source_type = 'qcInfo'";
            }
        }
        if("tab".equals(field)) {
            if (PoReturnConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(value)) {
                super.buildDefaultDTO("pro.confirm_status", PoReturnConfirmStatusEnum.WAIT_CONFIRM.getCode());
            }
            if (PoReturnConfirmStatusEnum.CONFIRM.getCode().equals(value)) {
                super.buildDefaultDTO("pro.confirm_status", PoReturnConfirmStatusEnum.CONFIRM.getCode());
            }
        }
        return null;
    }
}
