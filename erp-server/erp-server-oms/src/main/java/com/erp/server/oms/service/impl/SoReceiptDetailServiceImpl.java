package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.AttachDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BillApproveStatusEnum;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReceiptDetailEntity;
import com.erp.model.oms.entity.SoReceiptEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.SoReceiptDetailMapper;
import com.erp.server.oms.service.OmsAttachmentService;
import com.erp.server.oms.service.SoInfoService;
import com.erp.server.oms.service.SoReceiptDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SoReceiptDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 收款单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
@Slf4j
@Service
public class SoReceiptDetailServiceImpl extends SuperServiceImpl<SoReceiptDetailMapper, SoReceiptDetailEntity> implements SoReceiptDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private OmsAttachmentService attachmentService;

    @Resource
    private SoInfoService soInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addDetail(SoReceiptEntity soReceiptEntity, List<SoReceiptDetailDTO.AddDTO> detailList) {

        //付款流水号不能重复
        Set<String> paymentNoSet = new HashSet<>();
        for (SoReceiptDetailDTO.AddDTO dto : detailList) {
            if(!paymentNoSet.add(dto.getPaymentNo())){
                throw new ServiceException("付款流水号不能重复");
            }
        }
        if(CollectionUtils.isNotEmpty(paymentNoSet)){
            List<String> existPayNo = baseMapper.existPaymentNo(paymentNoSet,soReceiptEntity.getId());

            if(CollectionUtils.isNotEmpty(existPayNo)){
                String existPayNoStr = String.join(",", existPayNo);
                throw new ServiceException("付款流水号已存在:"+existPayNoStr);
            }
        }


        //销售单号不能重复
        Set<String> soCodeSet = new HashSet<>();
        for (SoReceiptDetailDTO.AddDTO dto : detailList) {
            if(!soCodeSet.add(dto.getSoCode())){
                throw new ServiceException("销售单号不能重复");
            }
        }

        detailList.forEach(v->v.setMainId(soReceiptEntity.getId()));
        List<SoReceiptDetailEntity> saveList = BeanMapper.copyList(detailList, SoReceiptDetailEntity.class);
        this.saveBatch(saveList);
        List<AttachDTO> allAttachDTOS = new ArrayList<>();
        for (SoReceiptDetailEntity soReceiptDetailEntity : saveList) {
            SoReceiptDetailDTO.AddDTO addDTO = detailList.stream().filter(v -> v.getPaymentNo().equals(soReceiptDetailEntity.getPaymentNo())).findFirst().orElse(new SoReceiptDetailDTO.AddDTO());
            List<AttachDTO> attachDTOS = addDTO.getAttachmentList();
            if(CollectionUtils.isEmpty(attachDTOS)){
                continue;
            }
            attachDTOS.forEach(v->v.setBusinessId(soReceiptDetailEntity.getId()));
            allAttachDTOS.addAll(attachDTOS);
        }
        // 保存附件
        TableName tableName = SoReceiptDetailEntity.class.getDeclaredAnnotation(TableName.class);
        if(CollectionUtils.isNotEmpty(allAttachDTOS)){
            attachmentService.batchSaveOrUpdate(allAttachDTOS, tableName.value());
        }
        return true;
    }

    @Override
    public List<SoReceiptDetailEntity> listByMainIds(List<String> mainIds) {
        if(CollectionUtils.isNotEmpty(mainIds)){
            return super.lambdaQuery().in(SoReceiptDetailEntity::getMainId, mainIds).list();
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(SoReceiptEntity soReceiptEntity, List<SoReceiptDetailDTO.UpdateDTO> detailList,boolean isFromSoUpdate) {
        //处理删除
        List<String> ids = detailList.stream().filter(v -> StrUtil.isNotBlank(v.getId())).map(SoReceiptDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<SoReceiptDetailEntity> dbList = this.lambdaQuery().eq(SoReceiptDetailEntity::getMainId, soReceiptEntity.getId()).list();
        List<SoReceiptDetailEntity> deleteList = dbList.stream().filter(v -> !ids.contains(v.getId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(deleteList) && !isFromSoUpdate){
            List<String> deleteIds = deleteList.stream().map(SoReceiptDetailEntity::getId).collect(Collectors.toList());
            this.removeByIds(deleteIds);
        }
        //付款流水号不能重复
        Set<String> paymentNoSet = new HashSet<>();
        for (SoReceiptDetailDTO.UpdateDTO dto : detailList) {
            String paymentNo = dto.getPaymentNo();
            // 如果是空字符串，跳过重复检查
            if (paymentNo != null && !paymentNo.isEmpty()) {
                if(!paymentNoSet.add(paymentNo)){
                    throw new ServiceException("付款流水号不能重复");
                }
            }
        }
        if(CollectionUtils.isNotEmpty(paymentNoSet)){
            List<String> existPayNo = baseMapper.existPaymentNo(paymentNoSet,soReceiptEntity.getId());

            if(CollectionUtils.isNotEmpty(existPayNo)){
                String existPayNoStr = String.join(",", existPayNo);
                throw new ServiceException("付款流水号已存在:"+existPayNoStr);
            }
        }
        //销售单号不能重复
        Set<String> soCodeSet = new HashSet<>();
        for (SoReceiptDetailDTO.UpdateDTO dto : detailList) {
            if(!soCodeSet.add(dto.getSoCode())){
                throw new ServiceException("销售单号不能重复");
            }
        }
        //新增的
        List<SoReceiptDetailDTO.UpdateDTO> addList = detailList.stream().filter(v -> StrUtil.isBlank(v.getId())).collect(Collectors.toList());
        List<SoReceiptDetailDTO.AddDTO> addDTOList = BeanMapper.copyList(addList, SoReceiptDetailDTO.AddDTO.class);
        this.addDetail(soReceiptEntity, addDTOList);

        //修改的
        List<SoReceiptDetailDTO.UpdateDTO> updateList = detailList.stream().filter(v -> StrUtil.isNotBlank(v.getId())).collect(Collectors.toList());
        List<SoReceiptDetailEntity> updateEntityList = new ArrayList<>();
        for (SoReceiptDetailDTO.UpdateDTO updateDTO : updateList) {
            SoReceiptDetailEntity soReceiptDetailEntity = dbList.stream().filter(v -> v.getId().equals(updateDTO.getId())).findFirst().orElse(new SoReceiptDetailEntity());
            soReceiptDetailEntity.setSoCode(updateDTO.getSoCode());
            soReceiptDetailEntity.setSoId(updateDTO.getSoId());
            soReceiptDetailEntity.setSourceDetailId(updateDTO.getSourceDetailId());
            soReceiptDetailEntity.setPaymentNo(updateDTO.getPaymentNo());
            soReceiptDetailEntity.setRemark(updateDTO.getRemark());
            soReceiptDetailEntity.setReceiptAmount(updateDTO.getReceiptAmount());
            soReceiptDetailEntity.setId(updateDTO.getId());
            updateEntityList.add(soReceiptDetailEntity);
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据明细 ", UserContext.getDefaultLoginUser().getUserName(), soReceiptEntity.getCode(), "收款单");
            operateLogService.addModuleOperateLogByObj(soReceiptDetailEntity, soReceiptEntity, ModuleTypeEnum.SO_RECEIPT.getCode(), soReceiptEntity.getId(), msg);
        }
        if(CollectionUtils.isNotEmpty(updateEntityList)){
            this.updateBatchById(updateEntityList);
        }
        //处理修改的附件
        List<AttachDTO> allAttachDTOS = new ArrayList<>();
        for (SoReceiptDetailEntity soReceiptDetailEntity : updateEntityList) {
            SoReceiptDetailDTO.UpdateDTO addDTO = detailList.stream().filter(v -> v.getPaymentNo().equals(soReceiptDetailEntity.getPaymentNo())).findFirst().orElse(new SoReceiptDetailDTO.UpdateDTO());
            List<AttachDTO> attachDTOS = addDTO.getAttachmentList();
            if(CollectionUtils.isEmpty(attachDTOS)){
                continue;
            }
            attachDTOS.forEach(v->v.setBusinessId(soReceiptDetailEntity.getId()));
            allAttachDTOS.addAll(attachDTOS);
        }
        // 保存附件
        TableName tableName = SoReceiptDetailEntity.class.getDeclaredAnnotation(TableName.class);
        if(CollectionUtils.isNotEmpty(allAttachDTOS)){
            attachmentService.batchSaveOrUpdate(allAttachDTOS, tableName.value());
        }
    }

    @Override
    public void removeByMainId(String id) {
        List<SoReceiptDetailEntity> dbList = this.lambdaQuery().eq(SoReceiptDetailEntity::getMainId, id).list();
        if(CollectionUtils.isNotEmpty(dbList)){
            List<String> deleteIds = dbList.stream().map(SoReceiptDetailEntity::getId).collect(Collectors.toList());
            this.removeByIds(deleteIds);
        }
    }

    @Override
    public List<SoReceiptDetailEntity> listBySoId(String id) {
        if(StrUtil.isNotBlank(id)){
            return super.lambdaQuery().eq(SoReceiptDetailEntity::getSoId, id).list();
        }
        return Collections.emptyList();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoReceiptDetailEntity soReceiptDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
