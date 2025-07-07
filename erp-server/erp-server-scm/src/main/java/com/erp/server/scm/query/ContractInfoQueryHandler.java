package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.model.scm.entity.ContractInfoEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.workflow.WorkflowFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
public class ContractInfoQueryHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }


    /**
     * @description: tabSql拼接
     * @author jack
     * @date: 2025-06-26
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        if(value.equals("all") || value.equals("")){
            return "";
        }

        if(Objects.equals(value.toString(), ApproveStatusEnum.APPROVE_ING.getCode())){
            //待我审核
            //根据单据id查询审核流程
            LoginUser user = UserContext.getNonLoginUser();
            ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
            dto.setBusinessKey(SourceTypeEnum.CONTRACT_INFO.getCode());
            dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
            dto.setCurApproveId(user.getUid());
            List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
            if (CollectionUtils.isNotEmpty(processTaskManagementList)) {
                List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
                return "ci.id in ('"+String.join("','",ids)+"')";
            }
            return "ci.id ='-1'";
        }else if(Objects.equals(value.toString(), ApproveStatusEnum.REJECT.getCode())){
            return "ci.approve_status ='"+value+"'";
        }
        return "ci.status ='"+value+"'";
    }
}
