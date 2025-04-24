package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *
 */
@Component
public class ReportFormsManageQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("bill_date".equals(field)){
            return getQueryAllSql();
        }
        return null;
    }
}

