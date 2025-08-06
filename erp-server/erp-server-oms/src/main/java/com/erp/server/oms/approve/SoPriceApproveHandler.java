package com.erp.server.oms.approve;

import cn.hutool.core.collection.CollUtil;
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
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.entity.SoPriceChangeDetailEntity;
import com.erp.model.oms.entity.SoPriceDetailEntity;
import com.erp.model.oms.entity.SoPriceEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoPriceChangeDetailService;
import com.erp.server.oms.service.SoPriceDetailService;
import com.erp.server.oms.service.SoPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.SO_PRICE)
public class SoPriceApproveHandler extends AbstractApproveHandler {

    @Resource
    private SoPriceService soPriceService;

    @Resource
    private SoPriceDetailService soPriceDetailService;

    @Resource
    private SoPriceChangeDetailService soPriceChangeDetailService;

    @Resource
    private OperateLogService operateLogService;

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
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SoPriceEntity entity = soPriceService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        approveOneDTO.setComment(dto.getComment());
        Boolean approve = soPriceService.approveEnd(approveOneDTO, entity);
        if (Boolean.FALSE.equals(approve)) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        //添加日志
        return operateLogService.addModuleOperateLog(CharSequenceUtil.format("【{}】审核，审核【{}】了一个销售价目表",dto.getApprovePlatformEnum().getName(), ApproveTypeEnum.getName(dto.getApproveStatus().getStatus())), ModuleTypeEnum.SO_PRICE.getCode(),dto.getBusinessId(), "审核操作");
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        dto.getComments().stream().map(obj -> new OperateLogDTO.AddModuleOperateLogDTO(CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(),obj), ModuleTypeEnum.SO_PRICE.getCode(), dto.getId(), "添加评论"))
                .forEach(operateLogList::add);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }
}
