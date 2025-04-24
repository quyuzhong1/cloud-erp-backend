package com.erp.server.scm.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *
 */
@Component
public class SupplierReportQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("qcType".equals(field)){
            if("all".equals(value)){
                return getQueryAllSql();
            }
            if("inside_qc".equals(value)){
                super.buildSplicingSQLDTO("qrf.is_inside", QueryConditionEnum.EQ,true, QueryDataTypeEnum.BOOLEAN);
            }
            if("outside_qc".equals(value)){
                super.buildSplicingSQLDTO("qrf.is_inside", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
            }
            return super.getSplicingSQL();
        }
        return null;
    }
}

