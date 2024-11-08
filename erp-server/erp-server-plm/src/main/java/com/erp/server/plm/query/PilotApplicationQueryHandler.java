package com.erp.server.plm.query;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.model.tms.enums.PilotApplicationTabEnum;
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

/**
 * 试产/量产单高级查询
 * @date 2024-08-27
 * @author tanmujin
 */
@Component
public class PilotApplicationQueryHandler extends AbstractQueryHandler {
    @Resource
    private WorkflowFeign workflowFeign;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        LoginUser user = UserContext.getNonLoginUser();
        if(field.equals("tab")){
            if(value.equals("all") || value.equals("")){
                return "";
            }
            if(value.equals("waitSubmit")){
                return "(pa.approve_status = 'waitSubmit')";
            }
            if(value.equals("waitMeApprove")){
                //待我审批流程信息
                ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
                dto.setBusinessKey(SourceTypeEnum.PILOT_APPLICATION.getCode());
                dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
                dto.setCurApproveId(user.getUid());
                List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
                List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(ids)){
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < ids.size(); i++) {
                        sb.append("'");
                        sb.append(ids.get(i));
                        if(i == ids.size() - 1){
                            sb.append("'");
                        } else {
                            sb.append("',");
                        }
                    }
                    return "(pa.id in (" + sb.toString() + "))";
                }else {
                    return "(pa.id = '-1')";
                }
            }
            if(value.equals("reject")){
                return "(pa.approve_status = 'reject')";
            }
            if(value.equals("notOrder")){
                return "(pa.approve_status = 'approve' and pad.order_status = 'notOrder')";
            }
            if(value.equals("order")){
                return "(pa.approve_status = 'approve' and pad.order_status = 'order')";
            }
        }
        return "";
    }
}
