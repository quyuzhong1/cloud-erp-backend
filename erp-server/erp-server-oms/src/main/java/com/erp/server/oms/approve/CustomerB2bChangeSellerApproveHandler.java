package com.erp.server.oms.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.CustomerB2bSellerChangeService;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.CUSTOMER_B2B_CHANGE_SELLER)
public class CustomerB2bChangeSellerApproveHandler extends AbstractApproveHandler {

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerB2bSellerChangeService customerB2bSellerChangeService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        List<BatchResultDTO> batchResultDTOS = customerB2bSellerChangeService.batchCancel(new ApproveDTO.BatchCancelProcessDTO(dto));
        return CollUtil.isEmpty(batchResultDTOS) ? Boolean.TRUE : batchResultDTOS.get(0).getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        log.error("b2b客户销售员变更，id【{}】无反审核功能",dto.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //销售变更单
        CustomerInfoEntity customerInfo = customerInfoService.getById(dto.getBusinessId());
        CustomerB2bSellerChangeEntity entity = customerB2bSellerChangeService.getByMainId(customerInfo.getId());
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Collections.singletonList(dto.getBusinessId()));
        baseApproveParamDTO.setComment(dto.getComment());
        Boolean approve = customerB2bSellerChangeService.approveEnd(baseApproveParamDTO, entity, new BatchResultDTO(), customerInfo);
        if (Boolean.FALSE.equals(approve)) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        //添加日志
        return operateLogService.addModuleOperateLog(CharSequenceUtil.format("【{}】审核，审核【{}】了一个B2B客户表变更销售员",dto.getApprovePlatformEnum().getName(), ApproveTypeEnum.getName(dto.getApproveStatus().getStatus())), ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(),dto.getBusinessId(), "审核操作");
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        dto.getComments().stream().map(obj -> new OperateLogDTO.AddModuleOperateLogDTO(CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(),obj), ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), dto.getId(), "添加评论"))
                .forEach(operateLogList::add);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }
}
