package com.erp.server.plm.service.impl;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.plm.entity.PilotApplicationEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.plm.service.LogisticsProductService;
import com.erp.server.plm.service.PilotApplicationService;
import com.erp.server.plm.service.ProductLogisticsService;
import com.erp.server.plm.service.WorkflowProcessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @date 2023/8/2 16:04
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

    @Resource
    private LogisticsProductService logisticsProductService;

    @Resource
    private ProductLogisticsService productLogisticsService;
    @Resource
    private PilotApplicationService pilotApplicationService;
    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        String businessKey = dto.getBusinessKey();
        switch (SourceTypeEnum.getByCode(businessKey)) {
            case PRODUCT_LOGISTICS:
                //产品物流
                productLogisticsApproveEnd(dto);
                break;
            case PILOT_APPLICATION:
                //试产量产单
                pilotApplicationApproveEnd(dto);
            default:
                break;
        }
        return Boolean.TRUE;
    }

    /**
     * 盘盈盘亏单审核通过
     * @param dto
     */
    private Boolean productLogisticsApproveEnd(EndProcessDTO dto) {
        ProductLogisticsEntity entity = productLogisticsService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return logisticsProductService.approveEnd(approveOne,entity);
    }

    /**
     * 试产量产单审核通过
     */
    private boolean pilotApplicationApproveEnd(EndProcessDTO dto) {
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        PilotApplicationEntity entity = new PilotApplicationEntity();
        entity.setId(dto.getBusinessId());
        return pilotApplicationService.approveEnd(approveOne, entity);
    }

}
