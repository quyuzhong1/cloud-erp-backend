package com.erp.server.oms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.oms.service.SoChangeService;
import com.erp.server.oms.service.WorkflowProcessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 工作流业务层
 * @date 2023/7/3 15:38
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

    @Resource
    private SoChangeService soChangeService;

    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        String businessKey = dto.getBusinessKey();

        if (SourceTypeEnum.SO_CHANGE.getCode().equals(businessKey)) {
            //销售变更单
            List<SoChangeEntity> list = soChangeService.listByIds(Arrays.asList(dto.getBusinessId()));
            BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
            baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
            baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
            soChangeService.approveEnd(baseApproveParamDTO,list);
        }
        return Boolean.TRUE;
    }
}
