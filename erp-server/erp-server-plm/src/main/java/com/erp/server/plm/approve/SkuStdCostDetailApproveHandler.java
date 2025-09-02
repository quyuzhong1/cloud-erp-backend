package com.erp.server.plm.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
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
import com.erp.model.plm.entity.SysLogEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.model.plm.entity.SkuStdCostEntity;
import com.erp.server.plm.service.SkuStdCostDetailService;
import com.erp.server.plm.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import com.erp.server.plm.service.SkuStdCostService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.SKU_STD_COST_DETAIL)
public class SkuStdCostDetailApproveHandler extends AbstractApproveHandler {

    @Resource
    private SkuStdCostDetailService skuStdCostDetailService;
    @Resource
    private SkuStdCostService skuStdCostService;
    @Resource
    private SysLogService sysLogService;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<SysLogEntity> list = new ArrayList<>();
        for (String comment : dto.getComments()) {
            SysLogEntity sysLogEntity = new SysLogEntity();
            sysLogEntity.setBusinessId(dto.getId());
            sysLogEntity.setOperation("添加评论");
            //添加评论日志
            String operateContent = CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(), comment);
            sysLogEntity.setContent(operateContent);
            list.add(sysLogEntity);
        }
        sysLogService.addSysLogByBatchSave(list);
    }
}
