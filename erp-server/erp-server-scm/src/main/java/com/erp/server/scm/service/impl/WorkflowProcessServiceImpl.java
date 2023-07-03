package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.scm.service.PurchasePriceChangeService;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.WorkflowProcessService;
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
    private PurchasePriceService purchasePriceService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        String businessKey = dto.getBusinessKey();

        if (SourceTypeEnum.PURCHASE_PRICE.getCode().equals(businessKey)) {
            //采购价目信息
            List<PurchasePriceEntity> list = purchasePriceService.listByIds(Arrays.asList(dto.getBusinessId()));

            BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
            baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
            baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
            purchasePriceService.approveEnd(baseApproveParamDTO,list);
        }
        if (SourceTypeEnum.PURCHASE_PRICE_CHANGE.getCode().equals(businessKey)) {
            //采购价目调价信息
            List<PurchasePriceChangeEntity> list = purchasePriceChangeService.listByIds(Arrays.asList(dto.getBusinessId()));

            BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
            baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
            baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
            purchasePriceChangeService.approveEnd(baseApproveParamDTO,list);
        }
        return Boolean.TRUE;
    }
}
