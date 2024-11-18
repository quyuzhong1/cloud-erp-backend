package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.service.CommonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yl
 * @Classname CommonServiceImpl

 * @Date 2023-03-15 11:50
 * @Created by yl
 */
@Service
public class CommonServiceImpl  implements CommonService {

    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    public List<String> listProcessCurBusinessIds (String businessKey) {
        //获取当前人需要审核的业务ids
        ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList = new ValidList<>();
        ProcessManagementDTO.ApproveActivityDTO approveActivityDTO = new ProcessManagementDTO.ApproveActivityDTO();
        approveActivityDTO.setCurApproveId(UserContext.getDefaultLoginUser().getUid());
        approveActivityDTO.setBusinessKey(businessKey);
        dtoList.add(approveActivityDTO);
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.batchCurApproverByApprove(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        return listApiResult.getData().stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getBusinessId())).map(ProcessManagementDTO.CurApproveInfoDTO::getBusinessId).collect(Collectors.toList());
    }
    
    @Override
    public LoginUser getUserInfo() {
        return UserContext.getDefaultLoginUser();
    }
}
