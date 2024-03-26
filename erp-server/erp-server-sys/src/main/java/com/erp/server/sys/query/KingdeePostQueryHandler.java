package com.erp.server.sys.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.server.sys.service.KingdeeDepartmentService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Lambda
 * @Classname KingdeeDepartmentQueryHandler
 * @Description TODO
 * @Date 2024-03-11 18:04
 * @Created by yl
 */
@Component
public class KingdeePostQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}
