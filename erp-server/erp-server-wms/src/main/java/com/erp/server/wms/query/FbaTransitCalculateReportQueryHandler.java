package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zdy
 * @date 2024年12月12日 9:54
 */
@Component
public class FbaTransitCalculateReportQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}

