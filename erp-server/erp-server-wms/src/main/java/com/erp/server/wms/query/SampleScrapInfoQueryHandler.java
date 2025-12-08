package com.erp.server.wms.query;

import cn.hutool.core.collection.CollUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.workflow.WorkflowFeign;
import jodd.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SampleScrapInfoQueryHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if("ssi.approve_user_id".equals(field)){
            ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList = new ValidList<>();

            List<String> list = new ArrayList<>();
            if (value instanceof List) {
                list = (List<String>) value;
            }else {
                list.add(value.toString());
            }

            for (String curApproveId : list) {
                ProcessManagementDTO.ApproveActivityDTO dto = new ProcessManagementDTO.ApproveActivityDTO();
                dto.setCurApproveId(curApproveId);
                dto.setBusinessKey(SourceTypeEnum.SAMPLE_SCRAP_INFO.getCode());
                dtoList.add(dto);
            }
            ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.batchCurApproverByApprove(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.getCode(), listApiResult.getMsg()));
            }

            QueryConditionEnum compareCode = AdvanceQueryContext.getCompareCode();
            List<ProcessManagementDTO.CurApproveInfoDTO> data = listApiResult.getData();
            if(CollUtil.isNotEmpty(data)){
                List<String> ids = data.stream().map(ProcessManagementDTO.CurApproveInfoDTO::getBusinessId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
                if(compareCode.equals(QueryConditionEnum.NOT_IN_LIST) || compareCode.equals(QueryConditionEnum.NE) ){
                    super.buildSplicingSQLDTO("ssi.id",QueryConditionEnum.NOT_IN_LIST,ids,QueryDataTypeEnum.STRING);
                }else {
                    super.buildSplicingSQLDTO("ssi.id",QueryConditionEnum.IN_LIST,ids,QueryDataTypeEnum.STRING);
                }
            }else {
                super.buildSplicingSQLDTO("ssi.id",QueryConditionEnum.EQ,"-1",QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        if(Objects.equals(value, ApproveStatusEnum.WAIT_SUBMIT.getCode()+"/"+ApproveStatusEnum.REJECT.getCode())){ //待提交/审核不通过
            return "ssi.approve_status in ('"+ApproveStatusEnum.WAIT_SUBMIT.getCode()+"','"+ApproveStatusEnum.REJECT.getCode()+"')";
        }else if(Objects.equals(value, ApproveStatusEnum.APPROVE_ING.getCode())){ //待我审核
            //待我审批流程信息
            ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
            dto.setBusinessKey(SourceTypeEnum.SAMPLE_SCRAP_INFO.getCode());
            dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
            dto.setCurApproveId(UserContext.getNonLoginUser().getUid());
            List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
            List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(ids)){
                super.buildSplicingSQLDTO("ssi.id", QueryConditionEnum.IN_LIST, ids, QueryDataTypeEnum.STRING);
            }else {
                super.buildDefaultDTO("ssi.id", "-1");
            }
        }
        super.buildDefaultDTO("ssi.approve_status", value);
        return super.getSplicingSQL();
    }
}
