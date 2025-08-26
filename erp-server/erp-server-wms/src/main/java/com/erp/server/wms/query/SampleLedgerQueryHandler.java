package com.erp.server.wms.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 样品台账统计查询处理器
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Component
public class SampleLedgerQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        // 根据业务需求处理特殊字段的查询逻辑
        // 目前没有特殊逻辑，返回null使用默认处理
        return null;
    }
}
