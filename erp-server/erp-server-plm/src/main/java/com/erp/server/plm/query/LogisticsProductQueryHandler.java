package com.erp.server.plm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.server.plm.service.CommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LogisticsProductQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            if ("all".equals(value)){
                return this.getQueryAllSql();
            }
            List<String> approveStatusList = new ArrayList<>(1);
            //待我审核
            if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
                //需要审核的业务ids
                List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PRODUCT_LOGISTICS.getCode());
                if (CollectionUtils.isEmpty(businessIds)) {
                    return null;
                }
                super.buildDefaultDTO("pl.id", businessIds);
            }
            // 待提交
            if (PageListTypeEnum.WAIT_SUBMIT.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }
            //已审核
            if (PageListTypeEnum.APPROVE.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            }
            //不通过
            if (PageListTypeEnum.REJECT.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.REJECT.getStatus());
            }
            if (!CollectionUtils.isEmpty(approveStatusList)) {
                super.buildDefaultDTO("pl.approve_status", approveStatusList);
            }
            return super.getSplicingSQL();
        }
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        if ("ps.sale_state".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "ps.sale_state is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "ps.sale_state is not null";
            }
            return " ps.sale_state " + compareCodeSplicingValueSql;
        }

        if ("pi.project_status".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "pi.project_status is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "pi.project_status is not null";
            }
            return " pi.project_status " + compareCodeSplicingValueSql;
        }
        return null;
    }

}

