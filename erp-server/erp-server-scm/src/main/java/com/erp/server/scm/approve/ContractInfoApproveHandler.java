package com.erp.server.scm.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.ContractInfoEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.server.scm.service.ContractInfoService;
import com.erp.server.scm.service.PurchaseOrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.CONTRACT_INFO)
public class ContractInfoApproveHandler extends AbstractApproveHandler {

    @Resource
    private ContractInfoService contractInfoService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        ContractInfoEntity entity = contractInfoService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到合同管理单数据");
        }
        BatchResultDTO resultDTO = contractInfoService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = contractInfoService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        ContractInfoEntity entity = contractInfoService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到合同管理单数据");
        }
        ApproveOneDTO baseApproveParamDTO = new ApproveOneDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setId(dto.getBusinessId());
        return contractInfoService.approveEnd(baseApproveParamDTO, entity);
    }
}
