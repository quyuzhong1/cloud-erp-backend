package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.WarehouseLocationStatusEnum;
import org.springframework.stereotype.Component;

/**
 * 
 * @date 2024-06-03
 * @author tanmujin
 */
@Component
public class WarehouseLocationInfoQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        // 待提交
        if (WarehouseLocationStatusEnum.IDLE.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WarehouseLocationStatusEnum.IDLE.getCode());
        }
        // 待审核
        if (WarehouseLocationStatusEnum.RECYCLABLE.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WarehouseLocationStatusEnum.IDLE.getCode());
        }
        // 已审核
        if (WarehouseLocationStatusEnum.OCCUPIED.getCode().equals(value)) {
            super.buildDefaultDTO("wl.status", WarehouseLocationStatusEnum.OCCUPIED.getCode());
        }
        return super.getSplicingSQL();
    }
}
