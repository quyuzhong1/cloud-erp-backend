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
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.plm.entity.OperateLogEntity;
import com.erp.server.plm.service.LogisticsProductService;
import com.erp.server.plm.service.ProductLogisticsService;
import com.erp.server.plm.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.PRODUCT_LOGISTICS)
public class ProductLogisticsApproveHandler extends AbstractApproveHandler {

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private LogisticsProductService logisticsProductService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = logisticsProductService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = logisticsProductService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        ProductLogisticsEntity entity = productLogisticsService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setComment(dto.getComment());
        Boolean approve = logisticsProductService.approveEnd(approveOne, entity);
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
        return  Boolean.TRUE;
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
