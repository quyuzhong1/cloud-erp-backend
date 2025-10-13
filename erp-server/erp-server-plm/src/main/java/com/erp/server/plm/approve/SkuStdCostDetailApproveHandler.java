package com.erp.server.plm.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.SkuStdCostDetailEntity;
import com.erp.model.plm.entity.SkuStdCostEntity;
import com.erp.server.plm.service.SkuStdCostDetailService;
import com.erp.server.plm.service.SkuStdCostService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.SKU_STD_COST_DETAIL)
public class SkuStdCostDetailApproveHandler extends AbstractApproveHandler {

    @Resource
    private SkuStdCostDetailService skuStdCostDetailService;
    @Resource
    private SkuStdCostService skuStdCostService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        SkuStdCostDetailEntity entity = skuStdCostDetailService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到sku标准成本单数据");
        }
        SkuStdCostEntity mainEntity = skuStdCostService.getById(entity.getMainId());
        if (ObjectUtil.isEmpty(mainEntity)) {
            throw new ServiceException("未找到sku标准成本单主数据");
        }
        BatchResultDTO resultDTO = skuStdCostDetailService.cancelProcess(dto.getId(), entity, mainEntity);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        SkuStdCostDetailEntity entity = skuStdCostDetailService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到sku标准成本单数据");
        }
        SkuStdCostEntity mainEntity = skuStdCostService.getById(entity.getMainId());
        if (ObjectUtil.isEmpty(mainEntity)) {
            throw new ServiceException("未找到sku标准成本单主数据");
        }
        BatchResultDTO resultDTO = skuStdCostDetailService.disApprove(entity, mainEntity);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SkuStdCostDetailEntity entity = skuStdCostDetailService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(dto.getVariablesMap());
        return skuStdCostDetailService.approveEnd(approveOne,entity);
    }
}
