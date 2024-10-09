package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.WaveStatusEnum;
import org.springframework.stereotype.Component;

/**
 * 波次列表（PDA）高级查询
 * @date 2024-07-02
 * @author tanmujin
 */
@Component
public class WaveListPdaAdvanceQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("status")){
            for (WaveStatusEnum statusEnum : WaveStatusEnum.values()) {
                if(value.equals(WaveStatusEnum.HANG_UP.getCode()) || value.equals(WaveStatusEnum.PICK_ING.getCode())){
                    return "and status in ('hangUp','pickIng')";
                }else {
                    super.buildDefaultDTO("status", value);
                    return super.getSplicingSQL();
                }
            }
            return null;
        }
        return null;
    }
}
