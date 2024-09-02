package com.erp.server.plm.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 试产/量产单高级查询
 * @date 2024-08-27
 * @author tanmujin
 */
@Component
public class PilotApplicationQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return "";
    }
}
