package com.erp.server.plm.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.enums.BomOperationTypeEnum;
import com.erp.server.plm.service.BomOperateLogService;
import com.erp.server.plm.service.ProductChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.PRODUCT_CHANGE)
public class ProductChangeApproveHandler extends AbstractApproveHandler {

    @Resource
    private ProductChangeService productChangeService;

    @Resource
    private BomOperateLogService bomOperateLogService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = productChangeService.cancelProcess(dto);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        log.error("产品变更，id【{}】无反审核功能",dto.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        ProductChangeEntity entity = productChangeService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(dto.getVariablesMap());
        approveOne.setComment(dto.getComment());
        Boolean approve = productChangeService.approveEnd(approveOne, entity);
        if (!approve) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE,SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        //操作记录
        String operateContent = CharSequenceUtil.format("【{}】审核，审核结果：【{}】，审核意见 ：【{}】",dto.getApprovePlatformEnum().getName(),  dto.getApproveStatus().getName(), dto.getComment());
        bomOperateLogService.saveOperate(entity.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        for (String comment : dto.getComments()) {
            //添加评论日志
            String operateContent = CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(), comment);
            bomOperateLogService.saveOperate(dto.getId(), BomOperationTypeEnum.STATE_CHANGE.getType(), operateContent);
        }
    }
}
