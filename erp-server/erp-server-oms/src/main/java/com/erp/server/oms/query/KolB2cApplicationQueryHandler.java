package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import com.erp.model.oms.enums.KolB2bApplicationTableEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.workflow.WorkflowFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jack
 * @date 2025-12-04
 */
@Component
public class KolB2cApplicationQueryHandler extends AbstractQueryHandler {


    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    private String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        if(Objects.equals(value, KolB2bApplicationTableEnum.WAIT_SHIPPED.getCode())){ //待发货
            super.buildDefaultDTO("kba.approve_status", ApproveStatusEnum.APPROVE.getCode());
            super.buildDefaultDTO("ksba.delivery_status", KolB2bApplicationTableEnum.WAIT_SHIPPED.getCode());

        }else if(Objects.equals(value, KolB2bApplicationTableEnum.SHIPPED.getCode())){ //已发货
            super.buildDefaultDTO("kba.approve_status", ApproveStatusEnum.APPROVE.getCode());
            super.buildDefaultDTO("ksba.delivery_status", KolB2bApplicationTableEnum.SHIPPED.getCode());
        }else if(Objects.equals(value, ApproveStatusEnum.APPROVE_ING.getCode())){ //待我审核
            super.buildDefaultDTO("kba.approve_status", value);
            //待我审批流程信息
            ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
            dto.setBusinessKey(SourceTypeEnum.KOL_B2C_APPLICATION.getCode());
            dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
            dto.setCurApproveId(UserContext.getNonLoginUser().getUid());
            List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
            List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(ids)){
                super.buildSplicingSQLDTO("kba.id", QueryConditionEnum.IN_LIST, ids, QueryDataTypeEnum.STRING);
            }else {
                super.buildDefaultDTO("kba.id", "-1");
            }
        }else  {
            super.buildDefaultDTO("kba.approve_status", value);
        }
        return super.getSplicingSQL();
    }
}

