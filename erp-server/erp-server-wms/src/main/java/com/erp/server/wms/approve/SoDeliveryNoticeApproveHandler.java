package com.erp.server.wms.approve;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoDeliveryNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.SO_DELIVERY_NOTICE)
public class SoDeliveryNoticeApproveHandler extends AbstractApproveHandler {

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private OperateLogService operateLogService;


    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return soDeliveryNoticeService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        SoDeliveryNoticeEntity entity = soDeliveryNoticeService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单");
        }
        BatchResultDTO resultDTO = soDeliveryNoticeService.disApprove(entity);
        return resultDTO.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SoDeliveryNoticeEntity entity = soDeliveryNoticeService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"发货通知单");
        }
        Boolean approve = soDeliveryNoticeService.approveEnd(new ApproveOneDTO(dto.getBusinessId(),dto.getApproveStatus().getStatus(),dto.getComment()),entity);
        if (Boolean.FALSE.equals(approve)) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        //添加日志
        return operateLogService.addModuleOperateLog(CharSequenceUtil.format("【{}】审核，审核【{}】了一个发货通知单",dto.getApprovePlatformEnum().getName(), ApproveTypeEnum.getName(dto.getApproveStatus().getStatus())), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(),dto.getBusinessId(), "审核操作");
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
