package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.workflow.WorkflowFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SampleInitialLedgerQueryHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        if(Objects.equals(value, ApproveStatusEnum.APPROVE_ING.getCode())){ //待我审核
            //待我审批流程信息
            ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
            dto.setBusinessKey(SourceTypeEnum.SAMPLE_LEDGER_INIT.getCode());
            dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
            dto.setCurApproveId(UserContext.getNonLoginUser().getUid());
            List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
            List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(ids)){
                super.buildSplicingSQLDTO("sil.id", QueryConditionEnum.IN_LIST, ids, QueryDataTypeEnum.STRING);
            }else {
                super.buildDefaultDTO("sil.id", "-1");
            }
        }
        super.buildDefaultDTO("sil.approve_status", value);
        return super.getSplicingSQL();
    }
}
