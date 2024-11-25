package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationChangeDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationChangeEntity;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.model.wms.enums.RequisitionChangeTypeEnum;
import com.erp.server.wms.mapper.RequisitionApplicationChangeDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.PickingDetailService;
import com.erp.server.wms.service.RequisitionApplicationChangeDetailService;
import com.erp.server.wms.service.RequisitionApplicationDetailService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 要货申请变更明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
 */
@Slf4j
@Service
public class RequisitionApplicationChangeDetailServiceImpl extends SuperServiceImpl<RequisitionApplicationChangeDetailMapper, RequisitionApplicationChangeDetailEntity> implements RequisitionApplicationChangeDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(RequisitionApplicationChangeEntity entity, RequisitionApplicationChangeDTO.ViewDTO addDTO) {
        this.checkData(addDTO.getViewDetailList());
        List<RequisitionApplicationChangeDetailEntity> detailEntityList = this.buildDetail(addDTO.getViewDetailList(),entity);
        this.saveBatch(detailEntityList);
    }

    private List<RequisitionApplicationChangeDetailEntity> buildDetail(List<RequisitionApplicationChangeDTO.ViewDetailDTO> viewDetailList, RequisitionApplicationChangeEntity entity) {
        List<RequisitionApplicationChangeDetailEntity> list = new ArrayList<>();
        for (RequisitionApplicationChangeDTO.ViewDetailDTO viewDetail : viewDetailList) {
            RequisitionApplicationChangeDetailEntity soDeliveryNoticeChangeDetailEntity = new RequisitionApplicationChangeDetailEntity();
            soDeliveryNoticeChangeDetailEntity.setMainId(entity.getId());
            soDeliveryNoticeChangeDetailEntity.setSkuId(viewDetail.getSkuId());
            soDeliveryNoticeChangeDetailEntity.setSkuNo(viewDetail.getSkuNo());
            soDeliveryNoticeChangeDetailEntity.setSourceDetailId(viewDetail.getSourceDetailId());
            soDeliveryNoticeChangeDetailEntity.setBusinessDetailId(viewDetail.getRequisitionDetailId());
            soDeliveryNoticeChangeDetailEntity.setChangeType(viewDetail.getChangeType());
            soDeliveryNoticeChangeDetailEntity.setOriginQty(viewDetail.getOriginRequisitionQty());
            soDeliveryNoticeChangeDetailEntity.setNewQty(viewDetail.getNewRequisitionQty());
            soDeliveryNoticeChangeDetailEntity.setPlatformSkuNo(viewDetail.getPlatformSku());
            soDeliveryNoticeChangeDetailEntity.setBomVersion(viewDetail.getBomVersion());
            soDeliveryNoticeChangeDetailEntity.setFnSku(viewDetail.getFnSku());
            soDeliveryNoticeChangeDetailEntity.setRemark(viewDetail.getRemark());
            soDeliveryNoticeChangeDetailEntity.setPlatformSkuName(viewDetail.getPlatformSkuName());
            soDeliveryNoticeChangeDetailEntity.setPlatformSpu(viewDetail.getAsin());
            list.add(soDeliveryNoticeChangeDetailEntity);
        }
        return list;
    }

    private void checkData(List<RequisitionApplicationChangeDTO.ViewDetailDTO> details) {
        List<String> requisitionDetailIds = details.stream().map(RequisitionApplicationChangeDTO.ViewDetailDTO::getRequisitionDetailId).collect(Collectors.toList());
        List<PickingDetailEntity> pickingDetailEntityList = pickingDetailService.listPickingDetailBySourceDetailIds(requisitionDetailIds);
        // 收集所有 requisitionDetailId 及其对应的 SKU
        Map<String, List<String>> soDetailIdToSkuMap = details.stream()
                .filter(v->StringUtils.isNotBlank(v.getRequisitionDetailId()))
                .collect(Collectors.groupingBy(
                        RequisitionApplicationChangeDTO.ViewDetailDTO::getRequisitionDetailId,
                        Collectors.mapping(RequisitionApplicationChangeDTO.ViewDetailDTO::getSkuNo, Collectors.toList())
                ));

        // 检查是否存在重复的 requisitionDetailId
        Set<String> duplicateRequisitionDetailId = soDetailIdToSkuMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (!duplicateRequisitionDetailId.isEmpty()) {
            // 收集所有重复的 SKU
            StringBuilder duplicateSkus = new StringBuilder();
            for (String requisitionDetailId : duplicateRequisitionDetailId) {
                List<String> skus = soDetailIdToSkuMap.get(requisitionDetailId).stream().distinct().collect(Collectors.toList());
                duplicateSkus.append(String.join(", ", skus)).append("; ");
            }
            throw new ServiceException("存在重复的明细: " + duplicateSkus);
        }
        for (RequisitionApplicationChangeDTO.ViewDetailDTO viewDetail : details) {
            if(RequisitionChangeTypeEnum.UPDATE.getCode().equals(viewDetail.getChangeType())){
                List<PickingDetailEntity> currentPickList = pickingDetailEntityList.stream().filter(v -> v.getSourceDetailId().equals(viewDetail.getRequisitionDetailId())).collect(Collectors.toList());
                Integer pickedQty = currentPickList.stream().mapToInt(PickingDetailEntity::getQty).sum();
                if(viewDetail.getNewRequisitionQty() < pickedQty){
                    throw new ServiceException("【{}】变更数量不能小于已下推拣货单的应拣数量【{}】",viewDetail.getSkuNo(),pickedQty);
                }
            }else if(RequisitionChangeTypeEnum.ADD.getCode().equals(viewDetail.getChangeType())){

            }else if(RequisitionChangeTypeEnum.DELETE.getCode().equals(viewDetail.getChangeType())){
                if(StringUtils.isBlank(viewDetail.getRequisitionDetailId())){
                    throw new ServiceException("删除变更明细数据，要货申请明细ID不能为空");
                }
                List<PickingDetailEntity> currentPickList = pickingDetailEntityList.stream().filter(v -> v.getSourceDetailId().equals(viewDetail.getRequisitionDetailId())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(currentPickList)){
                    Integer pickedQty = currentPickList.stream().mapToInt(PickingDetailEntity::getPickedQty).sum();
                    throw new ServiceException("【{}】已拣货【{}】，不允许删除",viewDetail.getSkuNo(),pickedQty);
                }
            }else {
                throw new ServiceException("变更类型错误");
            }
        }
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RequisitionApplicationChangeDTO.ViewDTO dto, RequisitionApplicationChangeEntity entity) {
        this.checkData(dto.getViewDetailList());
        List<RequisitionApplicationChangeDetailEntity> dbDetailList = this.listByMains(Collections.singletonList(dto.getId()));
        List<RequisitionApplicationChangeDTO.ViewDetailDTO> detailViewList = dto.getViewDetailList();
        List<String> updateViewIds = detailViewList.stream().map(RequisitionApplicationChangeDTO.ViewDetailDTO::getDetailId).collect(Collectors.toList());
        List<RequisitionApplicationChangeDTO.ViewDetailDTO> addViewList = detailViewList.stream().filter(v-> StringUtils.isBlank(v.getDetailId())).collect(Collectors.toList());

        List<RequisitionApplicationChangeDetailEntity> addList = this.buildDetail(addViewList,entity);
        List<RequisitionApplicationChangeDetailEntity> updateList = new ArrayList<>();
        List<RequisitionApplicationChangeDetailEntity> deleteList = dbDetailList.stream().filter(v->!updateViewIds.contains(v.getId())).collect(Collectors.toList());

        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        for (RequisitionApplicationChangeDTO.ViewDetailDTO viewDetail : detailViewList) {
            RequisitionApplicationChangeDetailEntity dbEntity = dbDetailList.stream().filter(v->v.getId().equals(viewDetail.getDetailId())).findFirst().orElse(null);
            if(Objects.isNull(dbEntity)){
                continue;
            }
            if(!dbEntity.getChangeType().equals(viewDetail.getChangeType())){
                operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("编辑了sku【{}】变更类型从【{}】为【{}】",viewDetail.getSkuNo(), RequisitionChangeTypeEnum.getName(dbEntity.getChangeType()),RequisitionChangeTypeEnum.getName(viewDetail.getChangeType())), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(),dbEntity.getMainId(),"编辑操作"));
            }
            if(!dbEntity.getSkuNo().equals(viewDetail.getSkuNo()) || !dbEntity.getNewQty().equals(viewDetail.getNewRequisitionQty())){
                if(dbEntity.getSkuNo().equals(viewDetail.getSkuNo())){
                    operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("编辑了SKU的新发货通知sku【{}】数量从【{}】为【{}】",dbEntity.getSkuNo(),dbEntity.getNewQty(),viewDetail.getNewRequisitionQty()), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(),dbEntity.getMainId(),"编辑操作"));
                }else{
                    operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("编辑了SKU的新发货通知sku从【{}】为【{}】,数量从【{}】为【{}】",dbEntity.getSkuNo(),viewDetail.getSkuNo(),dbEntity.getNewQty(),viewDetail.getNewRequisitionQty()), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(),dbEntity.getMainId(),"编辑操作"));
                }
            }
            buildUpdateDetail(viewDetail, dbEntity);
            updateList.add(dbEntity);
        }
        addList.forEach(v->{
            operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("新增一行sku{}",v.getSkuNo()), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(),v.getMainId(),"编辑操作"));
        });
        deleteList.forEach(v->{
            operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("删除一行sku{}",v.getSkuNo()), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(),v.getMainId(),"编辑操作"));
        });

        this.update(addList,updateList,deleteList);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(List<RequisitionApplicationChangeDetailEntity> addList, List<RequisitionApplicationChangeDetailEntity> updateList, List<RequisitionApplicationChangeDetailEntity> deleteList) {
        if(CollectionUtils.isNotEmpty(addList)){
            this.saveBatch(addList);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        if(CollectionUtils.isNotEmpty(deleteList)){
            List<String> deleteIds = deleteList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            this.removeByIds(deleteIds);
        }
    }
    private static void buildUpdateDetail(RequisitionApplicationChangeDTO.ViewDetailDTO viewDetail, RequisitionApplicationChangeDetailEntity dbEntity) {
        dbEntity.setSkuId(viewDetail.getSkuId());
        dbEntity.setSkuNo(viewDetail.getSkuNo());
        dbEntity.setSourceDetailId(viewDetail.getSourceDetailId());
        dbEntity.setBusinessDetailId(viewDetail.getRequisitionDetailId());
        dbEntity.setChangeType(viewDetail.getChangeType());
        dbEntity.setOriginQty(viewDetail.getOriginRequisitionQty());
        dbEntity.setNewQty(viewDetail.getNewRequisitionQty());
        dbEntity.setPlatformSkuNo(viewDetail.getPlatformSku());
        dbEntity.setBomVersion(viewDetail.getBomVersion());
        dbEntity.setFnSku(viewDetail.getFnSku());
        dbEntity.setRemark(viewDetail.getRemark());
        dbEntity.setPlatformSkuName(viewDetail.getPlatformSkuName());
        dbEntity.setPlatformSpu(viewDetail.getAsin());
    }

    @Override
    public List<RequisitionApplicationChangeDTO.ExistDTO> checkExist(List<String> sourceDetailIds, List<String> businessDetailIds) {
        if(CollUtil.isEmpty(sourceDetailIds) && CollUtil.isEmpty(businessDetailIds)){
            return new ArrayList<>();
        }
        return baseMapper.checkExist(sourceDetailIds,businessDetailIds);
    }

    @Override
    public List<RequisitionApplicationChangeDetailEntity> listByMains(List<String> mainIds) {
        mainIds = mainIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if(CollUtil.isEmpty(mainIds)){
            return new ArrayList<>();
        }
        return lambdaQuery().in(RequisitionApplicationChangeDetailEntity::getMainId, mainIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByPicking(RequisitionApplicationChangeEntity entity, PickingListsDTO.AddChangeDTO addChangeDTO) {
        List<RequisitionApplicationChangeDetailEntity> detailEntityList = this.buildDetailByPicking(addChangeDTO,entity);
        this.saveBatch(detailEntityList);
    }

    private List<RequisitionApplicationChangeDetailEntity> buildDetailByPicking(PickingListsDTO.AddChangeDTO addChangeDTO, RequisitionApplicationChangeEntity entity) {
        List<RequisitionApplicationChangeDetailEntity> list = new ArrayList<>();
        List<PickingDetailEntity> allList = new ArrayList<>();
        allList.addAll(addChangeDTO.getAddList());
        allList.addAll(addChangeDTO.getUpdateList());
        allList.addAll(addChangeDTO.getRemoveList());
        List<String> requisitionDetailIds = allList.stream().map(PickingDetailEntity::getSourceDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> requisitionApplicationChangeEntities = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(requisitionDetailIds)){
            requisitionApplicationChangeEntities = requisitionApplicationDetailService.listByIds(requisitionDetailIds);
        }
        for (PickingDetailEntity pickingDetailEntity : addChangeDTO.getAddList()) {
            RequisitionApplicationChangeDetailEntity soDeliveryNoticeChangeDetailEntity = new RequisitionApplicationChangeDetailEntity();
            soDeliveryNoticeChangeDetailEntity.setMainId(entity.getId());
            soDeliveryNoticeChangeDetailEntity.setSkuId(pickingDetailEntity.getSkuId());
            soDeliveryNoticeChangeDetailEntity.setSkuNo(pickingDetailEntity.getSkuNo());
            soDeliveryNoticeChangeDetailEntity.setSourceDetailId(pickingDetailEntity.getId());
            soDeliveryNoticeChangeDetailEntity.setBusinessDetailId(pickingDetailEntity.getSourceDetailId());
            soDeliveryNoticeChangeDetailEntity.setChangeType(RequisitionChangeTypeEnum.ADD.getCode());
            soDeliveryNoticeChangeDetailEntity.setOriginQty(0);
            soDeliveryNoticeChangeDetailEntity.setNewQty(pickingDetailEntity.getActualQty());
            soDeliveryNoticeChangeDetailEntity.setPlatformSkuNo(pickingDetailEntity.getPlatformSkuNo());
            RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = requisitionApplicationChangeEntities.stream().filter(v->v.getId().equals(pickingDetailEntity.getSourceDetailId())).findFirst().orElse(null);
            if(Objects.nonNull(requisitionApplicationDetailEntity)){
                soDeliveryNoticeChangeDetailEntity.setBomVersion(requisitionApplicationDetailEntity.getBomVersion());
                soDeliveryNoticeChangeDetailEntity.setFnSku(requisitionApplicationDetailEntity.getPlatformFnSku());
                soDeliveryNoticeChangeDetailEntity.setPlatformSkuName(requisitionApplicationDetailEntity.getPlatformSkuName());
                soDeliveryNoticeChangeDetailEntity.setPlatformSpu(requisitionApplicationDetailEntity.getPlatformSpu());
            }
            list.add(soDeliveryNoticeChangeDetailEntity);
        }
        for (PickingDetailEntity pickingDetailEntity : addChangeDTO.getUpdateList()) {
            RequisitionApplicationChangeDetailEntity soDeliveryNoticeChangeDetailEntity = new RequisitionApplicationChangeDetailEntity();
            soDeliveryNoticeChangeDetailEntity.setMainId(entity.getId());
            soDeliveryNoticeChangeDetailEntity.setSkuId(pickingDetailEntity.getSkuId());
            soDeliveryNoticeChangeDetailEntity.setSkuNo(pickingDetailEntity.getSkuNo());
            soDeliveryNoticeChangeDetailEntity.setSourceDetailId(pickingDetailEntity.getId());
            soDeliveryNoticeChangeDetailEntity.setOriginWarehouseLocation(pickingDetailEntity.getOriginWarehouseLocation());
            soDeliveryNoticeChangeDetailEntity.setBusinessDetailId(pickingDetailEntity.getSourceDetailId());
            soDeliveryNoticeChangeDetailEntity.setChangeType(RequisitionChangeTypeEnum.UPDATE.getCode());
            soDeliveryNoticeChangeDetailEntity.setOriginQty(0);
            soDeliveryNoticeChangeDetailEntity.setNewQty(pickingDetailEntity.getActualQty());
            soDeliveryNoticeChangeDetailEntity.setPlatformSkuNo(pickingDetailEntity.getPlatformSkuNo());
            RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = requisitionApplicationChangeEntities.stream().filter(v->v.getId().equals(pickingDetailEntity.getSourceDetailId())).findFirst().orElse(null);
            if(Objects.nonNull(requisitionApplicationDetailEntity)){
                soDeliveryNoticeChangeDetailEntity.setBomVersion(requisitionApplicationDetailEntity.getBomVersion());
                soDeliveryNoticeChangeDetailEntity.setFnSku(requisitionApplicationDetailEntity.getPlatformFnSku());
                soDeliveryNoticeChangeDetailEntity.setPlatformSkuName(requisitionApplicationDetailEntity.getPlatformSkuName());
                soDeliveryNoticeChangeDetailEntity.setPlatformSpu(requisitionApplicationDetailEntity.getPlatformSpu());
            }
            list.add(soDeliveryNoticeChangeDetailEntity);
        }
        for (PickingDetailEntity pickingDetailEntity : addChangeDTO.getRemoveList()) {
            RequisitionApplicationChangeDetailEntity soDeliveryNoticeChangeDetailEntity = new RequisitionApplicationChangeDetailEntity();
            soDeliveryNoticeChangeDetailEntity.setMainId(entity.getId());
            soDeliveryNoticeChangeDetailEntity.setSkuId(pickingDetailEntity.getSkuId());
            soDeliveryNoticeChangeDetailEntity.setSkuNo(pickingDetailEntity.getSkuNo());
            soDeliveryNoticeChangeDetailEntity.setSourceDetailId(pickingDetailEntity.getId());
            soDeliveryNoticeChangeDetailEntity.setBusinessDetailId(pickingDetailEntity.getSourceDetailId());
            soDeliveryNoticeChangeDetailEntity.setChangeType(RequisitionChangeTypeEnum.DELETE.getCode());
            soDeliveryNoticeChangeDetailEntity.setOriginQty(0);
            soDeliveryNoticeChangeDetailEntity.setNewQty(pickingDetailEntity.getActualQty());
            soDeliveryNoticeChangeDetailEntity.setPlatformSkuNo(pickingDetailEntity.getPlatformSkuNo());
            RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = requisitionApplicationChangeEntities.stream().filter(v->v.getId().equals(pickingDetailEntity.getSourceDetailId())).findFirst().orElse(null);
            if(Objects.nonNull(requisitionApplicationDetailEntity)){
                soDeliveryNoticeChangeDetailEntity.setBomVersion(requisitionApplicationDetailEntity.getBomVersion());
                soDeliveryNoticeChangeDetailEntity.setFnSku(requisitionApplicationDetailEntity.getPlatformFnSku());
                soDeliveryNoticeChangeDetailEntity.setPlatformSkuName(requisitionApplicationDetailEntity.getPlatformSkuName());
                soDeliveryNoticeChangeDetailEntity.setPlatformSpu(requisitionApplicationDetailEntity.getPlatformSpu());
            }
            list.add(soDeliveryNoticeChangeDetailEntity);
        }
        return list;
    }

}
