package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 物流商对账明细（行级）高级查询
 * @author Will
 * @date 2026/6/1 10:30
 */
@Component
public class LogisticsReconDetailQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("matchStatus".equals(field)) {
            return buildOrCondition(toCodeList(value), this::matchStatusSql);
        }
        if ("keyword".equals(field)) {
            return buildKeywordSql(value);
        }
        return "";
    }

    private String matchStatusSql(String status) {
        if (LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode().equals(status)) {
            return "sub.match_status = 'unmatched'";
        }
        if (LogisticsReconDetailMatchStatusEnum.MATCHING.getCode().equals(status)) {
            return "sub.match_status = 'matching'";
        }
        if (LogisticsReconDetailMatchStatusEnum.MATCHED.getCode().equals(status)) {
            return "sub.match_status = 'matched'";
        }
        if (LogisticsReconDetailMatchStatusEnum.FAILED.getCode().equals(status)) {
            return "sub.match_status = 'failed'";
        }
        return "";
    }

    private String buildKeywordSql(Object value) {
        if (value == null) {
            return "";
        }
        String keyword = String.valueOf(value).trim();
        if (keyword.isEmpty()) {
            return "";
        }
        String escaped = keyword.replace("'", "''");
        return "(d.track_no LIKE '%" + escaped + "%' OR d.transport_no LIKE '%" + escaped + "%' "
                + "OR d.so_code LIKE '%" + escaped + "%' OR d.platform_order_no LIKE '%" + escaped + "%')";
    }

    private List<String> toCodeList(Object value) {
        List<String> list = new ArrayList<>();
        if (value instanceof Collection) {
            for (Object item : (Collection<?>) value) {
                addCode(list, item);
            }
            return list;
        }
        addCode(list, value);
        return list;
    }

    private void addCode(List<String> list, Object value) {
        if (value == null) {
            return;
        }
        String text = Objects.toString(value, "")
                .replace("[", "")
                .replace("]", "")
                .replace("'", "")
                .replace("\"", "");
        for (String item : text.split(",")) {
            String code = item.trim();
            if (!code.isEmpty()) {
                list.add(code);
            }
        }
    }

    private String buildOrCondition(List<String> codes, SqlBuilder builder) {
        List<String> parts = new ArrayList<>();
        for (String code : codes) {
            String sql = builder.build(code);
            if (sql != null && !sql.isEmpty()) {
                parts.add(sql);
            }
        }
        if (parts.isEmpty()) {
            return "";
        }
        return "(" + String.join(" OR ", parts) + ")";
    }

    private interface SqlBuilder {
        String build(String value);
    }
}
