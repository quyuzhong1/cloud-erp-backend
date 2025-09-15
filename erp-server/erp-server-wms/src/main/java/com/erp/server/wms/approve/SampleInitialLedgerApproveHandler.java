package com.erp.server.wms.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.SampleInitialLedgerEntity;
import com.erp.server.wms.service.SampleInitialLedgerService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 样品期初台账审核处理器
 * @author wuhaotian
 * @date 2025-09-15
 */
@Component
@ApproveBusinessKey(SourceTypeEnum.SAMPLE_LEDGER_INIT)
public class SampleInitialLedgerApproveHandler extends AbstractApproveHandler {

    @Resource
    private SampleInitialLedgerService sampleInitialLedgerService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO result = sampleInitialLedgerService.cancelProcess(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO result = sampleInitialLedgerService.disApprove(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SampleInitialLedgerEntity entity = sampleInitialLedgerService.getByIdOpt(dto.getBusinessId()).orElseThrow(() -> new ServiceException("未找到样品期初台账数据"));
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return sampleInitialLedgerService.approveEnd(approveOneDTO, entity);
    }
}
