package com.erp.server.scm.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.OperateLogDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchasePriceChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.PURCHASE_PRICE_CHANGE)
public class PurchasePriceChangeApproveHandler extends AbstractApproveHandler {

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private ModuleOperateLogService operateLogService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return purchasePriceChangeService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        log.error("采购调价表，id【{}】无反审核功能",dto.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //采购调价
        PurchasePriceChangeEntity entity = purchasePriceChangeService.getById(dto.getBusinessId());
        BatchResultDTO resultDTO = purchasePriceChangeService.approveEnd(entity, dto.getApproveStatus().getStatus(), dto.getComment(), null);
        if (Boolean.FALSE.equals(resultDTO.getSuccess())) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        //添加日志
        return operateLogService.addModuleOperateLog(CharSequenceUtil.format("【{}】审核，审核【{}】了一个采购调价表",dto.getApprovePlatformEnum().getName(), ApproveTypeEnum.getName(dto.getApproveStatus().getStatus())), ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(),dto.getBusinessId(), "审核操作");
    }


    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        dto.getComments().stream().map(obj -> new OperateLogDTO.AddModuleOperateLogDTO(CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(),obj), ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), dto.getId(), "添加评论"))
                .forEach(operateLogList::add);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }
}
