package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.WaveStatusEnum;
import org.springframework.stereotype.Component;

/**
 * 波次列表高级查询
 * @date 2024-06-20
 * @author tanmujin
 */
@Component
public class WaveListAdvanceQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("wl.status".equals(field)){
            for (WaveStatusEnum statusEnum : WaveStatusEnum.values()) {
                if(value.equals(statusEnum.getCode())){
                    super.buildDefaultDTO("wl.status", value);
                    return super.getSplicingSQL();
                }
            }
            return null;
        }
        return null;
    }
}
