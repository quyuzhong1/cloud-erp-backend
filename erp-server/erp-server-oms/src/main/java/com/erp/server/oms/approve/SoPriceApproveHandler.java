package com.erp.server.oms.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoPriceChangeDetailEntity;
import com.erp.model.oms.entity.SoPriceDetailEntity;
import com.erp.model.oms.entity.SoPriceEntity;
import com.erp.server.oms.service.SoPriceChangeDetailService;
import com.erp.server.oms.service.SoPriceDetailService;
import com.erp.server.oms.service.SoPriceService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ApproveBusinessKey(SourceTypeEnum.SO_PRICE)
public class SoPriceApproveHandler extends AbstractApproveHandler {

    @Resource
    private SoPriceService soPriceService;

    @Resource
    private SoPriceDetailService soPriceDetailService;

    @Resource
    private SoPriceChangeDetailService soPriceChangeDetailService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        SoPriceEntity entity = soPriceService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_SO_PRICE);
        }
        BatchResultDTO resultDTO = soPriceService.cancelProcess(entity);
        return resultDTO.getSuccess();
    }


    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        SoPriceEntity entity = soPriceService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_SO_PRICE);
        }
        List<SoPriceDetailEntity> detailList = soPriceDetailService.listDetailByMainId(dto.getId());
        List<String> priceDetailIds = detailList.stream().map(SoPriceDetailEntity::getId).distinct().collect(Collectors.toList());
        List<SoPriceChangeDetailEntity> changeDetailList = soPriceChangeDetailService.listBySoPriceDetailIds(priceDetailIds);
        BatchResultDTO resultDTO = soPriceService.disApprove(entity, changeDetailList);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SoPriceEntity entity = soPriceService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        approveOneDTO.setComment(dto.getComment());
        return soPriceService.approveEnd(approveOneDTO,entity);
    }
}
