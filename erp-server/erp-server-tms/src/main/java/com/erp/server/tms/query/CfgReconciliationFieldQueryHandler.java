package com.erp.server.tms.query;

import com.common.business.constant.SearchType;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 高级查询处理器
 *
 * @author Jim
 * {@code @date:}  2024-03-25
 */
@Component
public class CfgReconciliationFieldQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

        return null;
    }
}

