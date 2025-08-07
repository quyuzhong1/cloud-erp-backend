package com.erp.server.scm.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.server.scm.service.PurchasePriceChangeDetailService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ApproveBusinessKey(SourceTypeEnum.PURCHASE_PRICE)
public class PurchasePriceApproveHandler extends AbstractApproveHandler {

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private PurchasePriceChangeDetailService purchasePriceChangeDetailService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        //采购价目
        PurchasePriceEntity entity = purchasePriceService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        BatchResultDTO resultDTO = purchasePriceService.cancelProcessEntity(entity);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        PurchasePriceEntity entity = purchasePriceService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        List<PurchasePriceDetailEntity> detailList = purchasePriceDetailService.listDetailByMainId(dto.getId());
        List<String> priceDetailIds = detailList.stream().map(PurchasePriceDetailEntity::getId).distinct().collect(Collectors.toList());
        List<PurchasePriceChangeDetailEntity> changeDetailList = purchasePriceChangeDetailService.listByPurchasePriceDetailIds(priceDetailIds);
        BatchResultDTO resultDTO = purchasePriceService.disApprove(entity,detailList,changeDetailList);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //采购价目
        PurchasePriceEntity entity = purchasePriceService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        return purchasePriceService.approveEnd(entity,dto.getApproveStatus().getStatus(),"");
    }
}
