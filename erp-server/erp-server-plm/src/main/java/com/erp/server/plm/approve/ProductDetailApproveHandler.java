package com.erp.server.plm.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.OperateLogEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.ProductDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.PRODUCT_DETAIL)
public class ProductDetailApproveHandler extends AbstractApproveHandler {

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = productDetailService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        ProductDetailEntity entity = productDetailService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        BatchResultDTO resultDTO = productDetailService.disApprove(entity);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        ProductDetailEntity entity = productDetailService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(dto.getVariablesMap());
        approveOne.setComment(dto.getComment());
        Boolean approve = productDetailService.approveEnd(approveOne, entity);
        if (!approve) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        OperateLogEntity sysLogEntity = new OperateLogEntity();
        sysLogEntity.setBusinessId(dto.getBusinessId());
        sysLogEntity.setOperation("审核操作");
        //添加评论日志
        String operateContent = CharSequenceUtil.format("【{}】审核，审核结果：【{}】，审核意见 ：【{}】",dto.getApprovePlatformEnum().getName(),  dto.getApproveStatus().getName(), dto.getComment());
        sysLogEntity.setContent(operateContent);
        operateLogService.addSysLogByOther(sysLogEntity);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<OperateLogEntity> list = new ArrayList<>();
        for (String comment : dto.getComments()) {
            OperateLogEntity sysLogEntity = new OperateLogEntity();
            sysLogEntity.setBusinessId(dto.getId());
            sysLogEntity.setOperation("添加评论");
            //添加评论日志
            String operateContent = CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(), comment);
            sysLogEntity.setContent(operateContent);
            list.add(sysLogEntity);
        }
        operateLogService.addSysLogByBatchSave(list);
    }
}
