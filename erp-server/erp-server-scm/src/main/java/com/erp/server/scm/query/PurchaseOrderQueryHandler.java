package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.PoTableFlagEnum;
import com.erp.server.scm.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class PurchaseOrderQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("so.code".equals(field) || "po.source_code".equals(field)) {
            return buildSourceCodeSql(compareCodeSplicingValueSql);
        }
        if("tab".equals(field)){
           return getTabSql(value);
        }
        return null;
    }

    /**
     * 来源单号：与列表展示一致，覆盖采购申请(ref表)、主表source_code(退货/委外)、委外单联表兜底。
     * 子查询内不使用表别名.字段，避免「包含」条件下 wrapFieldsWithLower 对 boolean 字段执行 LOWER() 导致 SQL 报错。
     */
    private String buildSourceCodeSql(String compareCodeSplicingValueSql) {
        String subcontractSourceType = SourceTypeEnum.SUBCONTRACT_ORDER.getCode();
        return "("
                + "po.source_code " + compareCodeSplicingValueSql
                + " OR po.id IN ("
                + " SELECT purchase_order_id FROM purchase_application_ref_po"
                + " WHERE is_deleted IS NOT TRUE"
                + " AND purchase_application_id IN ("
                + " SELECT id FROM purchase_application WHERE is_deleted IS NOT TRUE AND code " + compareCodeSplicingValueSql
                + " )"
                + " )"
                + " OR (po.source_type = '" + subcontractSourceType + "'"
                + " AND po.source_id IN ("
                + " SELECT id FROM subcontract_order WHERE is_deleted IS NOT TRUE AND code " + compareCodeSplicingValueSql
                + " ))"
                + ")";
    }


    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2024/1/18 19:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 待提交
        if (PoTableFlagEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("po.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
        }
        //待我审核
        if (PoTableFlagEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("po.approve_status",Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
            //需要审核的业务ids
            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PURCHASE_ORDER.getCode());
            if (CollectionUtils.isNotEmpty(businessIds)) {
                super.buildDefaultDTO("po.id", businessIds);
            }else{
                //返回空结果
                return this.getQueryEmptySql();
            }
        }
        //待确认
        if (PoTableFlagEnum.TO_BE_CONFIRM.getCode().equals(value)) {
            super.buildDefaultDTO("po.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
            super.buildDefaultDTO("pod.execution_status", ExecutionStatusEnum.TO_BE_CONFIRM.getCode());
        }
        //已确认
        if (PoTableFlagEnum.CONFIRM.getCode().equals(value)) {
            super.buildDefaultDTO("po.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
            super.buildDefaultDTO("pod.execution_status", ExecutionStatusEnum.CONFIRM.getCode());
        }
        //已拒绝
        if (PoTableFlagEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("pod.execution_status", Arrays.asList(ExecutionStatusEnum.REJECT.getCode()));
        }
        //送货中
        if (PoTableFlagEnum.DELIVERY.getCode().equals(value)) {
            super.buildDefaultDTO("pod.execution_status", Arrays.asList(ExecutionStatusEnum.DELIVERY.getCode()));
        }
        //已完成
        if (PoTableFlagEnum.FINISH.getCode().equals(value)) {
            super.buildDefaultDTO("pod.execution_status", Arrays.asList(ExecutionStatusEnum.FINISH.getCode()));
        }
        //已关闭
        if (PoTableFlagEnum.CLOSED.getCode().equals(value)) {
            super.buildDefaultDTO("pod.execution_status", Arrays.asList(ExecutionStatusEnum.CLOSED.getCode()));
        }
        //不通过
        if (PoTableFlagEnum.APPROVE_REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("po.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        return super.getSplicingSQL();
    }
}

