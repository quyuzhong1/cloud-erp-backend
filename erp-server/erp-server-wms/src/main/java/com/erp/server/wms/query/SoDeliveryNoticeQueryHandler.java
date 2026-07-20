package com.erp.server.wms.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.utils.CollectionUtils;
import com.erp.model.wms.enums.IsAllowOutstockEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SoDeliveryNoticeQueryHandler extends AbstractQueryHandler {

    private static final Set<String> GENERATION_PICK_STATUS_WHITELIST = new HashSet<>(Arrays.asList(
            "未生成", "部分生成", "已生成"
    ));

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)) {
            if ("waitSubmit".equals(value)) {
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getCode());
            }
            if ("toBeApprove".equals(value)) {
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.APPROVE_ING.getCode());
            }
            if ("packingCompleted".equals(value)) {
                super.buildSplicingSQLDTO("sdn.is_allow_outstock", QueryConditionEnum.EQ, IsAllowOutstockEnum.WAIT_NOTICE.getCode(), QueryDataTypeEnum.BOOLEAN);
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.APPROVE.getCode());
                super.buildSplicingSQLDTO("sdn.delivery_status", QueryConditionEnum.EQ, false, QueryDataTypeEnum.BOOLEAN);
            }
            if ("unShipped".equals(value)) {
                super.buildSplicingSQLDTO("sdn.is_allow_outstock", QueryConditionEnum.EQ, IsAllowOutstockEnum.PERMIT.getCode(), QueryDataTypeEnum.BOOLEAN);
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.APPROVE.getCode());
                super.buildSplicingSQLDTO("sdn.delivery_status", QueryConditionEnum.EQ, false, QueryDataTypeEnum.BOOLEAN);
            }
            if ("reject".equals(value)) {
                super.buildDefaultDTO("sdn.approve_status", ApproveStatusEnum.REJECT.getCode());
            }
            if ("completeShipment".equals(value)) {
                super.buildSplicingSQLDTO("sdn.delivery_status", QueryConditionEnum.EQ, true, QueryDataTypeEnum.BOOLEAN);
            }
        }
        //是否装箱
        if("isPacked".equals(field)) {
            if((Boolean) value){
                return " EXISTS (SELECT 1 FROM packing_task pt WHERE pt.source_id = sdn.id AND pt.is_deleted = FALSE and pt.packing_status = '" + PackingTaskStatusEnum.PACKED.getCode() + "')";
            }else{
                return " NOT EXISTS (SELECT 1 FROM packing_task pt WHERE pt.source_id = sdn.id AND pt.is_deleted = FALSE and pt.packing_status = '" + PackingTaskStatusEnum.PACKED.getCode() + "')";
            }
        }
        //拣货单状态：口径与列表 CASE / total JOIN 一致；支持 eq / ne / inList / notInList
        if ("generationPickStatus".equals(field)) {
            return buildGenerationPickStatusSql(value);
        }
        return null;
    }

    private String buildGenerationPickStatusSql(Object value) {
        List<String> statusList = CollectionUtils.convertStrClzToList(value).stream()
                .filter(CharSequenceUtil::isNotBlank)
                .map(String::trim)
                .filter(GENERATION_PICK_STATUS_WHITELIST::contains)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(statusList)) {
            return "1 = 2";
        }

        QueryConditionEnum compare = AdvanceQueryContext.getCompareCode();
        boolean notIn = QueryConditionEnum.NE.equals(compare)
                || QueryConditionEnum.NOT_IN_LIST.equals(compare);
        boolean in = QueryConditionEnum.EQ.equals(compare)
                || QueryConditionEnum.IN_LIST.equals(compare);
        if (!notIn && !in) {
            return "1 = 2";
        }

        String inExpr = statusList.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(","));
        String op = notIn ? "NOT IN" : "IN";
        // 依赖 paging / 导出共用 SQL 中的 total 子查询 JOIN；COALESCE 覆盖拣货数量为 NULL 的未生成场景，避免落入「部分生成」
        return "(CASE WHEN COALESCE(total.total_picked_qty, 0) = 0 THEN '未生成'"
                + " WHEN COALESCE(total.total_delivery_qty, 0) = COALESCE(total.total_picked_qty, 0) THEN '已生成'"
                + " ELSE '部分生成' END) " + op + " (" + inExpr + ")";
    }
}
