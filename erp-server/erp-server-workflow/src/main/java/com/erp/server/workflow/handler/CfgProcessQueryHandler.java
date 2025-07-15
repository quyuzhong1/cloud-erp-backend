package com.erp.server.workflow.handler;

/**
 * @description: 流程配置高级查询handler
 * @author: hcg
 * @date: 2025/5/12 17:15
 */

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @Author: hcg
 * @CreateTime: 2025-05-12
 * @Description:
 * @Version: 1.0
 */
@Component
public class CfgProcessQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            if(value.equals("all") || value.equals("")){
                return "";
            }
            if ("t".equals(value)) {
                return "r.disabled ="+ Boolean.TRUE;
            }
            if ("f".equals(value)) {
                return "r.disabled ="+ Boolean.FALSE;
            }
            return "";
        }
        return null;
    }
}
