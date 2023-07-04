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
        switch (SourceTypeEnum.getByCode(businessKey)) {
            case PURCHASE_PRICE:
                //采购价目
                purchasePriceApproveEnd(dto);
                break;
            case PURCHASE_PRICE_CHANGE:
                //采购调价
                purchasePriceChangeApproveEnd(dto);
                break;
            default:
                break;
        }
        return Boolean.TRUE;
    }

    /**
     * 采购价目审核结束
     * @Author Will
     * @Date 2023/7/4 11:26
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean purchasePriceApproveEnd(EndProcessDTO dto) {
        //销售变更单
        List<PurchasePriceEntity> list = purchasePriceService.listByIds(Arrays.asList(dto.getBusinessId()));
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
        return purchasePriceService.approveEnd(baseApproveParamDTO,list);
    }

    /**
     * 采购调价审核结束
     * @Author Will
     * @Date 2023/7/4 11:26
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean purchasePriceChangeApproveEnd(EndProcessDTO dto) {
        //销售变更单
        List<PurchasePriceChangeEntity> list = purchasePriceChangeService.listByIds(Arrays.asList(dto.getBusinessId()));
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
        return purchasePriceChangeService.approveEnd(baseApproveParamDTO,list);
    }
}
