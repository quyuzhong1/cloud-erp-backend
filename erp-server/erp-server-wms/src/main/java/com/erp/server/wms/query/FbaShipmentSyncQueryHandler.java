package com.erp.server.wms.query;

import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class FbaShipmentSyncQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("name".equals(field)) {
            super.buildDefaultDTO("si.name", value);
            super.buildDefaultDTO("si.dict_platform", PlatformDictEnum.AMAZON.getCode());
        }
        return null;
    }


}
