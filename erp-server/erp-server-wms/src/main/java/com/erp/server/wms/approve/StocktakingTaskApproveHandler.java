package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.server.wms.service.StocktakingTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.STOCKTAKING_TASK)
public class StocktakingTaskApproveHandler extends AbstractApproveHandler {

    @Resource
    private StocktakingTaskService stocktakingTaskService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = stocktakingTaskService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        log.error("盘点任务，id【{}】无反审核功能",dto.getId());
        return Boolean.TRUE;
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        StocktakingTaskEntity entity = stocktakingTaskService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return stocktakingTaskService.approveEnd(approveOne,entity);
    }
}
