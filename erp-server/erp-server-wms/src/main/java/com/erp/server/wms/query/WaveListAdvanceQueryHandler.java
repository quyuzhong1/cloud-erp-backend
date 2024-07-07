package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.entity.PickingCartTypeEntity;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.server.wms.service.PickingCartTypeService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 波次列表高级查询
 * @date 2024-06-20
 * @author tanmujin
 */
@Component
public class WaveListAdvanceQueryHandler extends AbstractQueryHandler {

    @Resource
    private PickingCartTypeService pickingCartTypeService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("picking_cart_type")){
            PickingCartTypeEntity entity = pickingCartTypeService.getById((String) value);
            super.buildDefaultDTO("picking_cart_type", entity.getName());
            return super.getSplicingSQL();
        }
        if(field.equals("status")){
            for (WaveStatusEnum statusEnum : WaveStatusEnum.values()) {
                if(value.equals(statusEnum.getCode())){
                    super.buildDefaultDTO("status", value);
                    return super.getSplicingSQL();
                }
            }
            return null;
        }
        return null;
    }
}
