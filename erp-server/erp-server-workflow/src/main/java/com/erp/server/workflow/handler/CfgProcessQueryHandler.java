package com.erp.server.workflow.handler;

/**
 * @description: 流程配置高级查询handler
 * @author: hcg
 * @date: 2025/5/12 17:15
 */

import com.common.business.query.AbstractQueryHandler;

import java.util.Arrays;

/**
 * @Author: hcg
 * @CreateTime: 2025-05-12
 * @Description:
 * @Version: 1.0
 */
public class CfgProcessQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            if ("all".equals(value)) {
                return this.getQueryAllSql();
            }
            if ("".equals(value)) {
                super.buildDefaultDTO("pp.product_status", Arrays.asList("", "1"));
            }
            if ("2".equals(value)) {
                super.buildDefaultDTO("pp.product_status", Arrays.asList("3", "4", "5", "6"));
            }
            if ("3".equals(value)) {
                super.buildDefaultDTO("pp.product_status", Arrays.asList("4", "5", "6"));
            }
            return super.getSplicingSQL();
        }
        return null;
    }{
    }
}
