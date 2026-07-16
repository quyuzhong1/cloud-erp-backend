package com.erp.server.tms.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.enums.LogisticsReconCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
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
     * 匹配状态筛选：用 EXISTS 半连接代替逐行 COUNT 聚合。
     * <ul>
     *   <li>unmatched ≡ 不存在已匹配有效费用项（等价于原 valid&lt;=0 OR match&lt;=0）</li>
     *   <li>partial ≡ 同时存在已匹配与未匹配有效费用项</li>
     *   <li>matched ≡ 存在有效费用项且全部为已匹配</li>
     * </ul>
     */
    private String matchStatusSql(String status) {
        String matched = LogisticsReconDetailMatchStatusEnum.MATCHED.getCode();
        if (LogisticsReconMatchStatusEnum.UNMATCHED.getCode().equals(status)) {
            return "(NOT " + validSubExists("sub.match_status = '" + matched + "'") + ")";
        }
        if (LogisticsReconMatchStatusEnum.PARTIAL.getCode().equals(status)) {
            return "(" + validSubExists("sub.match_status = '" + matched + "'")
                    + " AND " + validSubExists("sub.match_status IS DISTINCT FROM '" + matched + "'") + ")";
        }
        if (LogisticsReconMatchStatusEnum.MATCHED.getCode().equals(status)) {
            return "(" + validSubExists(null)
                    + " AND NOT " + validSubExists("sub.match_status IS DISTINCT FROM '" + matched + "'") + ")";
        }
        return "";
    }

    /**
     * 对账确认状态筛选：用 EXISTS 半连接代替逐行 COUNT 聚合。
     * <ul>
     *   <li>toBeConfirm ≡ 不存在 confirmed/partialConfirm 有效费用项</li>
     *   <li>partialConfirm ≡ 存在 partialConfirm，或存在 confirmed 且存在非 confirmed</li>
     *   <li>confirmed ≡ 存在有效费用项且全部为 confirmed</li>
     * </ul>
     */
    private String reconciliationStatusSql(String status) {
        String confirmed = LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode();
        String partial = LogisticsReconReconciliationStatusEnum.PARTIAL_CONFIRM.getCode();
        if (LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(status)) {
            return "(NOT " + validSubExists(
                    "sub.reconciliation_status IN ('" + confirmed + "', '" + partial + "')") + ")";
        }
        if (LogisticsReconReconciliationStatusEnum.PARTIAL_CONFIRM.getCode().equals(status)) {
            return "(" + validSubExists("sub.reconciliation_status = '" + partial + "'")
                    + " OR (" + validSubExists("sub.reconciliation_status = '" + confirmed + "'")
                    + " AND " + validSubExists("sub.reconciliation_status IS DISTINCT FROM '" + confirmed + "'")
                    + "))";
        }
        if (LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode().equals(status)) {
            return "(" + validSubExists(null)
                    + " AND NOT " + validSubExists(
                    "sub.reconciliation_status IS DISTINCT FROM '" + confirmed + "'") + ")";
        }
        return "";
    }

    /**
     * 有效费用项 EXISTS（detail 归属与 sub.main_id 一致，与统计口径相同）。
     * @param extraAnd 追加到 WHERE 的 AND 条件，可为 null
     */
    private String validSubExists(String extraAnd) {
        StringBuilder sql = new StringBuilder();
        sql.append("EXISTS (SELECT 1 FROM logistics_recon_detail_sub sub ")
                .append("INNER JOIN logistics_recon_detail d ")
                .append("ON d.id = sub.detail_id AND d.is_deleted = false AND d.main_id = sub.main_id ")
                .append("WHERE sub.is_deleted = false AND sub.main_id = logistics_recon.id");
        if (extraAnd != null && !extraAnd.isEmpty()) {
            sql.append(" AND ").append(extraAnd);
        }
        sql.append(')');
        return sql.toString();
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
