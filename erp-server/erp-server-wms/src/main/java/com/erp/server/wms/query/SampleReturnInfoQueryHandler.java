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
import com.erp.rpc.workflow.WorkflowFeign;
import jodd.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SampleReturnInfoQueryHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        if("sri.approve_user_id".equals(field)){
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
                dto.setBusinessKey(SourceTypeEnum.SAMPLE_RETURN_INFO.getCode());
                dtoList.add(dto);
            }
            ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.batchCurApproverByApprove(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }

            QueryConditionEnum compareCode = AdvanceQueryContext.getCompareCode();
            List<ProcessManagementDTO.CurApproveInfoDTO> data = listApiResult.getData();
            if(CollUtil.isNotEmpty(data)){
                List<String> ids = data.stream().map(ProcessManagementDTO.CurApproveInfoDTO::getBusinessId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
                if(compareCode.equals(QueryConditionEnum.NOT_IN_LIST) || compareCode.equals(QueryConditionEnum.NE) ){
                    super.buildSplicingSQLDTO("sri.id",QueryConditionEnum.NOT_IN_LIST,ids,QueryDataTypeEnum.STRING);
                }else {
                    super.buildSplicingSQLDTO("sri.id",QueryConditionEnum.IN_LIST,ids,QueryDataTypeEnum.STRING);
                }
            }else {
                super.buildSplicingSQLDTO("sri.id",QueryConditionEnum.EQ,"-1",QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        if(Objects.equals(value, ApproveStatusEnum.WAIT_SUBMIT.getCode()+"/"+ApproveStatusEnum.REJECT.getCode())){ //待提交/审核不通过
            return "sri.approve_status in ('"+ApproveStatusEnum.WAIT_SUBMIT.getCode()+"','"+ApproveStatusEnum.REJECT.getCode()+"')";
        }
        super.buildDefaultDTO("sri.approve_status", value);
        return super.getSplicingSQL();
    }
}
