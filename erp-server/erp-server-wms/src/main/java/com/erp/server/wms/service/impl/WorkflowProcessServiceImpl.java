package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.wms.entity.TransferApplicationEntity;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.wms.service.TransferApplicationService;
import com.erp.server.wms.service.WorkflowProcessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @date 2023/8/2 16:04
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {


    @Resource
    private TransferApplicationService transferApplicationService;


    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        String businessKey = dto.getBusinessKey();
        switch (SourceTypeEnum.getByCode(businessKey)) {
            case TRANSFER_APPLICATION:
                //调拨申请
                transferApplicationApproveEnd(dto);
                break;
            default:
                break;
        }
        return Boolean.TRUE;
    }



    /**
     * @description: 直接调拨单
     * @author Will
     * @date: 2023/8/2 16:13
     * @param dto
     * @return Boolean
     */
    private Boolean transferApplicationApproveEnd(EndProcessDTO dto) {
        //直接调拨单
        List<TransferApplicationEntity> list = transferApplicationService.listByIds(Arrays.asList(dto.getBusinessId()));
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
        return transferApplicationService.approveEnd(baseApproveParamDTO,list);
    }

}
