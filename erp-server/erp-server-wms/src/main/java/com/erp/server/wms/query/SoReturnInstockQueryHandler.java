package com.erp.server.wms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.entity.CustomerInfoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class SoReturnInstockQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            if(StringUtils.isBlank(value.toString())){
                return this.getQueryAllSql();
            }
            super.buildDefaultDTO("srr.approve_status",value);
            super.buildSplicingSQLDTO("srr.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        return null;
    }
}

