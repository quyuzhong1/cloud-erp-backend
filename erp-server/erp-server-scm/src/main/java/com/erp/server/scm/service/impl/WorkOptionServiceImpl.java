package com.erp.server.scm.service.impl;

import com.common.business.enums.SourceTypeEnum;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.mapper.WorkOptionMapper;
import com.erp.server.scm.service.CommonService;
import com.erp.server.scm.service.WorkOptionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 工作台服务类
 * @Author Luo_WG
 * @Date 2023/4/21 15:52
 **/
@Service
public class WorkOptionServiceImpl implements WorkOptionService {

    @Resource
    private WorkOptionMapper workOptionMapper;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CommonService commonService;

    @Override
    public List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        for (WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO : myWorkOptionDTOList) {
            if (!myWorkOptionDTO.getIsApprovalWorkflow()) {
                myWorkOptionDTO.setModuleCode(SourceTypeEnum.getByCode(myWorkOptionDTO.getModuleCode()).getTableName());
                myWorkOptionDTO.setTableNumber(workOptionMapper.getTableNum(myWorkOptionDTO));
            } else {
                //获取当前人需要审核的业务ids
                ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList = new ValidList<>();
                ProcessManagementDTO.ApproveActivityDTO approveActivityDTO = new ProcessManagementDTO.ApproveActivityDTO();
                approveActivityDTO.setCurApproveId(commonService.getUserInfo().getUid());
                approveActivityDTO.setBusinessKey(myWorkOptionDTO.getModuleCode());
                dtoList.add(approveActivityDTO);
                ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.batchCurApproverByApprove(dtoList);
                myWorkOptionDTO.setModuleCode(SourceTypeEnum.getByCode(myWorkOptionDTO.getModuleCode()).getTableName());
                myWorkOptionDTO.setTableNumber(listApiResult.getData().size());
            }
        }
        return myWorkOptionDTOList;
    }
}
