package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.enums.LogisticsReconCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsReconMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 物流商对账单（主表）高级查询
 * @author Will
 * @date 2026/6/1 10:30
 */
@Component
public class LogisticsReconQueryHandler extends AbstractQueryHandler {
    /**
     * 处理物流商对账单列表高级查询扩展字段
     * @author Will
     * @date: 2026/06/02
     * @param field
     * @param value
     * @param compareCodeSplicingValueSql
     * @return String
     */
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("matchStatus".equals(field)) {
            return buildOrCondition(toCodeList(value), this::matchStatusSql);
        }
        if ("reconciliationStatus".equals(field)) {
            return buildOrCondition(toCodeList(value), this::reconciliationStatusSql);
        }
        if(field.equals("tab")){
            return getTabSql(value);
        }
        return "";
    }

    /**
     * tab查询
     * @author Will
     * @date: 2026/06/18
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 导入中
        if (LogisticsReconCheckStatusEnum.IMPORTING.getCode().equals(value)) {
            super.buildDefaultDTO("logistics_recon.check_status", Collections.singletonList(LogisticsReconCheckStatusEnum.IMPORTING.getCode()));
        }
        // 待确认
        if (LogisticsReconCheckStatusEnum.PENDING.getCode().equals(value)) {
            super.buildDefaultDTO("logistics_recon.check_status", Collections.singletonList(LogisticsReconCheckStatusEnum.PENDING.getCode()));
        }
        // 已确认
        if (LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(value)) {
            super.buildDefaultDTO("logistics_recon.check_status", Collections.singletonList(LogisticsReconCheckStatusEnum.CONFIRMED.getCode()));
        }
        return super.getSplicingSQL();
    }


    /**
     * 构建匹配状态派生字段查询 SQL（关联子查询，不依赖 paging 全表聚合 JOIN）
     * @author Will
     * @date: 2026/06/02
     * @param status
     * @return String
     */
    private String matchStatusSql(String status) {
        String stats = validSubStatsSubquery();
        if (LogisticsReconMatchStatusEnum.UNMATCHED.getCode().equals(status)) {
            return "(SELECT COALESCE(t.valid_cost_count, 0) <= 0 OR COALESCE(t.match_count, 0) <= 0 FROM "
                    + stats + " t)";
        }
        if (LogisticsReconMatchStatusEnum.PARTIAL.getCode().equals(status)) {
            return "(SELECT COALESCE(t.match_count, 0) > 0 AND COALESCE(t.match_count, 0) < COALESCE(t.valid_cost_count, 0) FROM "
                    + stats + " t)";
        }
        if (LogisticsReconMatchStatusEnum.MATCHED.getCode().equals(status)) {
            return "(SELECT COALESCE(t.valid_cost_count, 0) > 0 AND COALESCE(t.match_count, 0) >= COALESCE(t.valid_cost_count, 0) FROM "
                    + stats + " t)";
        }
        return "";
    }

    /**
     * 构建对账确认状态派生字段查询 SQL（关联子查询，不依赖 paging 全表聚合 JOIN）
     * @author Will
     * @date: 2026/06/02
     * @param status
     * @return String
     */
    private String reconciliationStatusSql(String status) {
        String stats = validSubStatsSubquery();
        if (LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(status)) {
            return "(SELECT COALESCE(t.reconciliation_total_count, 0) <= 0 "
                    + "OR (COALESCE(t.reconciliation_confirmed_count, 0) <= 0 "
                    + "AND COALESCE(t.reconciliation_partial_count, 0) <= 0) FROM "
                    + stats + " t)";
        }
        if (LogisticsReconReconciliationStatusEnum.PARTIAL_CONFIRM.getCode().equals(status)) {
            return "(SELECT COALESCE(t.reconciliation_partial_count, 0) > 0 "
                    + "OR (COALESCE(t.reconciliation_confirmed_count, 0) > 0 "
                    + "AND COALESCE(t.reconciliation_confirmed_count, 0) < COALESCE(t.reconciliation_total_count, 0)) FROM "
                    + stats + " t)";
        }
        if (LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode().equals(status)) {
            return "(SELECT COALESCE(t.reconciliation_total_count, 0) > 0 "
                    + "AND COALESCE(t.reconciliation_confirmed_count, 0) >= COALESCE(t.reconciliation_total_count, 0) FROM "
                    + stats + " t)";
        }
        return "";
    }

    /**
     * 单主表有效费用项统计子查询（与详情页 / listPagingStatsByMainIds 口径一致）
     */
    private String validSubStatsSubquery() {
        return "(SELECT COUNT(*) AS valid_cost_count, "
                + "COUNT(*) FILTER (WHERE sub.match_status = 'matched') AS match_count, "
                + "COUNT(*) AS reconciliation_total_count, "
                + "COUNT(*) FILTER (WHERE sub.reconciliation_status = 'confirmed') AS reconciliation_confirmed_count, "
                + "COUNT(*) FILTER (WHERE sub.reconciliation_status = 'partialConfirm') AS reconciliation_partial_count "
                + "FROM logistics_recon_detail_sub sub "
                + "INNER JOIN logistics_recon_detail d "
                + "ON d.id = sub.detail_id AND d.is_deleted = false AND d.main_id = sub.main_id "
                + "WHERE sub.is_deleted = false AND sub.main_id = logistics_recon.id)";
    }

    /**
     * 将多选状态 SQL 拼成 OR 条件
     * @author Will
     * @date: 2026/06/02
     * @param values
     * @param builder
     * @return String
     */
    private String buildOrCondition(List<String> values, SqlBuilder builder) {
        List<String> sqlList = new ArrayList<>();
        for (String value : values) {
            String sql = builder.build(value);
            if (sql != null && !sql.isEmpty()) {
                sqlList.add(sql);
            }
        }
        if (sqlList.isEmpty()) {
            return "";
        }
        return "(" + String.join(" OR ", sqlList) + ")";
    }

    /**
     * 将高级查询传入值转换为状态 code 列表
     * @author Will
     * @date: 2026/06/02
     * @param value
     * @return List<String>
     */
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

    /**
     * 添加状态 code 到列表
     * @author Will
     * @date: 2026/06/02
     * @param list
     * @param value
     * @return void
     */
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

    /**
     * 状态 SQL 构造函数
     * @author Will
     * @date: 2026/06/02
     */
    private interface SqlBuilder {
        /**
         * 根据状态值构造 SQL
         * @author Will
         * @date: 2026/06/02
         * @param value
         * @return String
         */
        String build(String value);
    }
}
