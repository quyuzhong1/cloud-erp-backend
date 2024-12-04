package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationChangeDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationChangeEntity;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.model.wms.enums.RequisitionChangeTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(RequisitionApplicationChangeEntity entity, RequisitionApplicationChangeDTO.ViewDTO addDTO) {
        this.checkData(addDTO.getViewDetailList());
        addDTO.getViewDetailList().forEach(v -> v.setSourceDetailId(v.getRequisitionDetailId()));
        List<RequisitionApplicationChangeDetailEntity> detailEntityList = this.buildDetail(addDTO.getViewDetailList(), entity);
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
                .filter(v -> StringUtils.isNotBlank(v.getRequisitionDetailId()))
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
        // 找出所有 platformSku 中存在的重复项
        Map<String, Long> skuFrequency = details.stream()
                .map(RequisitionApplicationChangeDTO.ViewDetailDTO::getPlatformSku)
                .filter(Objects::nonNull) // 确保排除 null 值
                .collect(Collectors.groupingBy(sku -> sku, Collectors.counting()));

        // 提取重复的 platformSku
        List<String> duplicatePlatformSkus = skuFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!duplicatePlatformSkus.isEmpty()) {
            throw new ServiceException("存在相同的三方SKU{}",duplicatePlatformSkus);
        }

        List<String> skuIds = details.stream().map(RequisitionApplicationChangeDTO.ViewDetailDTO::getSkuId).collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> allBomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);

        for (RequisitionApplicationChangeDTO.ViewDetailDTO viewDetail : details) {
            List<BomChildrenSkuDTO> currentBomList = allBomChildrenSkuList.stream().filter(v->v.getParentSkuId().equals(viewDetail.getSkuId())).collect(Collectors.toList());
            if (RequisitionChangeTypeEnum.UPDATE.getCode().equals(viewDetail.getChangeType())) {
                if (StringUtils.isBlank(viewDetail.getRequisitionDetailId())) {
                    throw new ServiceException("修改变更明细数据，要货申请明细ID不能为空");
                }
                if(viewDetail.getNewRequisitionQty().equals(viewDetail.getOriginRequisitionQty())){
                    throw new ServiceException("修改明细数据，变更数量不能等于原数量");
                }
                List<PickingDetailEntity> currentPickList = pickingDetailEntityList.stream().filter(v -> v.getSourceDetailId().equals(viewDetail.getRequisitionDetailId())).collect(Collectors.toList());
                Integer pickedQty;
                if(CollectionUtils.isNotEmpty(currentBomList) && CollectionUtils.isNotEmpty(currentPickList)){
                    Integer bomQty = currentBomList.stream().filter(v->v.getSkuId().equals(currentPickList.get(0).getSkuId())).findFirst().map(BomChildrenSkuDTO::getQuantity).orElse(0);
                    pickedQty = bomQty * currentPickList.get(0).getQty();
                }else{
                    pickedQty = currentPickList.stream().mapToInt(PickingDetailEntity::getQty).sum();
                }

                if (viewDetail.getNewRequisitionQty() < pickedQty) {
                    throw new ServiceException("【{}】变更数量不能小于已下推拣货单的应拣数量【{}】", viewDetail.getSkuNo(), pickedQty);
                }
            } else if (RequisitionChangeTypeEnum.ADD.getCode().equals(viewDetail.getChangeType())) {

            } else if (RequisitionChangeTypeEnum.DELETE.getCode().equals(viewDetail.getChangeType())) {
                if (StringUtils.isBlank(viewDetail.getRequisitionDetailId())) {
                    throw new ServiceException("删除变更明细数据，要货申请明细ID不能为空");
                }
                List<PickingDetailEntity> currentPickList = pickingDetailEntityList.stream().filter(v -> v.getSourceDetailId().equals(viewDetail.getRequisitionDetailId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(currentPickList)) {
                    Integer pickedQty = currentPickList.stream().mapToInt(PickingDetailEntity::getPickedQty).sum();
                    throw new ServiceException("【{}】已拣货，不允许删除", viewDetail.getSkuNo(), pickedQty);
                }
            } else {
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
        List<RequisitionApplicationChangeDTO.ViewDetailDTO> addViewList = detailViewList.stream().filter(v -> StringUtils.isBlank(v.getDetailId())).collect(Collectors.toList());

        List<RequisitionApplicationChangeDetailEntity> addList = this.buildDetail(addViewList, entity);
        List<RequisitionApplicationChangeDetailEntity> updateList = new ArrayList<>();
        List<RequisitionApplicationChangeDetailEntity> deleteList = dbDetailList.stream().filter(v -> !updateViewIds.contains(v.getId())).collect(Collectors.toList());

        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        for (RequisitionApplicationChangeDTO.ViewDetailDTO viewDetail : detailViewList) {
            RequisitionApplicationChangeDetailEntity dbEntity = dbDetailList.stream().filter(v -> v.getId().equals(viewDetail.getDetailId())).findFirst().orElse(null);
            if (Objects.isNull(dbEntity)) {
                continue;
            }
            if (!dbEntity.getChangeType().equals(viewDetail.getChangeType())) {
                operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("编辑了sku【{}】变更类型从【{}】为【{}】", viewDetail.getSkuNo(), RequisitionChangeTypeEnum.getName(dbEntity.getChangeType()), RequisitionChangeTypeEnum.getName(viewDetail.getChangeType())), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), dbEntity.getMainId(), "编辑操作"));
            }
            if (!dbEntity.getSkuNo().equals(viewDetail.getSkuNo()) || !dbEntity.getNewQty().equals(viewDetail.getNewRequisitionQty())) {
                if (dbEntity.getSkuNo().equals(viewDetail.getSkuNo())) {
                    operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("编辑了SKU的新发货通知sku【{}】数量从【{}】为【{}】", dbEntity.getSkuNo(), dbEntity.getNewQty(), viewDetail.getNewRequisitionQty()), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), dbEntity.getMainId(), "编辑操作"));
                } else {
                    operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("编辑了SKU的新发货通知sku从【{}】为【{}】,数量从【{}】为【{}】", dbEntity.getSkuNo(), viewDetail.getSkuNo(), dbEntity.getNewQty(), viewDetail.getNewRequisitionQty()), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), dbEntity.getMainId(), "编辑操作"));
                }
            }
            buildUpdateDetail(viewDetail, dbEntity);
            updateList.add(dbEntity);
        }
        addList.forEach(v -> {
            operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("新增一行sku{}", v.getSkuNo()), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), v.getMainId(), "编辑操作"));
        });
        deleteList.forEach(v -> {
            operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("删除一行sku{}", v.getSkuNo()), ModuleTypeEnum.REQUISITION_APPLICATION_CHANGE.getCode(), v.getMainId(), "编辑操作"));
        });

        this.update(addList, updateList, deleteList);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(List<RequisitionApplicationChangeDetailEntity> addList, List<RequisitionApplicationChangeDetailEntity> updateList, List<RequisitionApplicationChangeDetailEntity> deleteList) {
        if (CollectionUtils.isNotEmpty(addList)) {
            this.saveBatch(addList);
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }
        if (CollectionUtils.isNotEmpty(deleteList)) {
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
        if (CollUtil.isEmpty(sourceDetailIds) && CollUtil.isEmpty(businessDetailIds)) {
            return new ArrayList<>();
        }
        return baseMapper.checkExist(sourceDetailIds, businessDetailIds);
    }

    @Override
    public List<RequisitionApplicationChangeDetailEntity> listByMains(List<String> mainIds) {
        mainIds = mainIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (CollUtil.isEmpty(mainIds)) {
            return new ArrayList<>();
        }
        return lambdaQuery().in(RequisitionApplicationChangeDetailEntity::getMainId, mainIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByPicking(RequisitionApplicationChangeEntity entity, PickingListsDTO.AddChangeDTO addChangeDTO) {
        List<RequisitionApplicationChangeDetailEntity> detailEntityList = this.buildDetailByPicking(addChangeDTO, entity);
        this.saveBatch(detailEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVirtualWarehouse(List<RequisitionApplicationChangeDTO.ApproveView> approveViewList, ApproveTypeEnum approveType) {
        if (CollUtil.isEmpty(approveViewList)) {
            return;
        }
        List<String> ids = approveViewList.stream().map(RequisitionApplicationChangeDTO.ApproveView::getDetailId).collect(Collectors.toList());

        List<RequisitionApplicationChangeDetailEntity> requisitionApplicationChangeDetailEntities = this.listByIds(ids);
        List<RequisitionApplicationChangeDetailEntity> updateList = new ArrayList<>();
        for (RequisitionApplicationChangeDTO.ApproveView approveView : approveViewList) {
            RequisitionApplicationChangeDetailEntity requisitionApplicationChangeDetailEntity = requisitionApplicationChangeDetailEntities.stream().filter(v -> v.getId().equals(approveView.getDetailId())).findFirst().orElse(null);
            if (Objects.isNull(requisitionApplicationChangeDetailEntity)) {
                continue;
            }
            if(Objects.equals(approveType, ApproveTypeEnum.PASS) && approveView.getChangeType().equals(RequisitionChangeTypeEnum.ADD.getCode())
            && StringUtils.isBlank(approveView.getFromVirtualWarehouseId())){
                throw new ServiceException("新增的明细必须指定虚拟仓，{}",approveView.getSkuNo());
            }
            if (!requisitionApplicationChangeDetailEntity.getFromVirtualWarehouseId().equals(approveView.getFromVirtualWarehouseId())) {
                requisitionApplicationChangeDetailEntity.setFromVirtualWarehouseId(approveView.getFromVirtualWarehouseId());
                requisitionApplicationChangeDetailEntity.setFromVirtualWarehouseName(approveView.getFromVirtualWarehouseName());
                updateList.add(requisitionApplicationChangeDetailEntity);
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }
    }

    private List<RequisitionApplicationChangeDetailEntity> buildDetailByPicking(PickingListsDTO.AddChangeDTO addChangeDTO, RequisitionApplicationChangeEntity entity) {
        List<RequisitionApplicationChangeDetailEntity> list = new ArrayList<>();
        List<PickingDetailEntity> allList = new ArrayList<>();
        allList.addAll(addChangeDTO.getAddList());
        allList.addAll(addChangeDTO.getUpdateList());
        allList.addAll(addChangeDTO.getRemoveList());
        List<String> requisitionDetailIds = allList.stream().map(PickingDetailEntity::getSourceDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> requisitionApplicationChangeEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(requisitionDetailIds)) {
            requisitionApplicationChangeEntities = requisitionApplicationDetailService.listByIds(requisitionDetailIds);
        }
        Map<String, List<PickingDetailEntity>> businessMap = allList.stream().collect(Collectors.groupingBy(PickingDetailEntity::getSourceDetailId));
        List<RequisitionApplicationDetailEntity> finalRequisitionApplicationChangeEntities = requisitionApplicationChangeEntities;
        List<String> skuIds = requisitionApplicationChangeEntities.stream().map(RequisitionApplicationDetailEntity::getSkuId).collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> allBomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        businessMap.forEach((requisitionDetailId, pickingDetailEntityList) -> {
            RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = finalRequisitionApplicationChangeEntities.stream().filter(v -> v.getId().equals(requisitionDetailId)).findFirst().orElseThrow(() -> new ServiceException("{}要货申请明细为空", pickingDetailEntityList.get(0).getSkuNo()));
            RequisitionApplicationChangeDetailEntity soDeliveryNoticeChangeDetailEntity = new RequisitionApplicationChangeDetailEntity();
            soDeliveryNoticeChangeDetailEntity.setMainId(entity.getId());
            soDeliveryNoticeChangeDetailEntity.setSkuId(requisitionApplicationDetailEntity.getSkuId());
            soDeliveryNoticeChangeDetailEntity.setSkuNo(requisitionApplicationDetailEntity.getSkuNo());
            soDeliveryNoticeChangeDetailEntity.setSourceDetailId(requisitionApplicationDetailEntity.getId());
            soDeliveryNoticeChangeDetailEntity.setOriginWarehouseLocation(requisitionApplicationDetailEntity.getId());
            soDeliveryNoticeChangeDetailEntity.setBusinessDetailId(requisitionApplicationDetailEntity.getId());
            soDeliveryNoticeChangeDetailEntity.setChangeType(RequisitionChangeTypeEnum.UPDATE.getCode());
            soDeliveryNoticeChangeDetailEntity.setOriginQty(requisitionApplicationDetailEntity.getRequisitionQty());
            Integer newQty = requisitionApplicationDetailEntity.getApproveQty();
            List<BomChildrenSkuDTO> bomChildrenList = allBomChildrenSkuList.stream().filter(req -> req.getParentSkuId().equals(requisitionApplicationDetailEntity.getSkuId()) && BomTypeEnum.COMBINATION.getType().equals(req.getType())).collect(Collectors.toList());
            Map<String, Integer> actualQtyMap = new HashMap<>();
            //计算数量
            for (PickingDetailEntity pickingDetailEntity : pickingDetailEntityList) {
                Integer nowQty = actualQtyMap.getOrDefault(pickingDetailEntity.getSkuNo(),0);
                if (RequisitionChangeTypeEnum.ADD.getCode().equals(pickingDetailEntity.getChangeType())) {
                    newQty = newQty + pickingDetailEntity.getActualQty();
                }else if (RequisitionChangeTypeEnum.UPDATE.getCode().equals(pickingDetailEntity.getChangeType())) {
                    newQty = newQty + (pickingDetailEntity.getActualQty() - pickingDetailEntity.getQty());
                }else if (RequisitionChangeTypeEnum.DELETE.getCode().equals(pickingDetailEntity.getChangeType())) {
                    newQty = newQty - pickingDetailEntity.getActualQty();
                }
                nowQty = nowQty + pickingDetailEntity.getActualQty();
                actualQtyMap.put(pickingDetailEntity.getSkuNo(),nowQty);
            }
            if(CollectionUtils.isNotEmpty(bomChildrenList)){
                String skuNo = pickingDetailEntityList.get(0).getSkuNo();
                int bomQuantity = bomChildrenList.get(0).getQuantity();
                int actualQty = Optional.ofNullable(actualQtyMap.get(skuNo))
                        .map(quantity -> new BigDecimal(quantity).divide(new BigDecimal(bomQuantity), 0, RoundingMode.HALF_UP).intValue())
                        .orElse(0);
                soDeliveryNoticeChangeDetailEntity.setNewQty(actualQty);
            }else{
                soDeliveryNoticeChangeDetailEntity.setNewQty(newQty);
            }

            soDeliveryNoticeChangeDetailEntity.setPlatformSkuNo(requisitionApplicationDetailEntity.getPlatformSku());
            soDeliveryNoticeChangeDetailEntity.setBomVersion(requisitionApplicationDetailEntity.getBomVersion());
            soDeliveryNoticeChangeDetailEntity.setFnSku(requisitionApplicationDetailEntity.getPlatformFnSku());
            soDeliveryNoticeChangeDetailEntity.setPlatformSkuName(requisitionApplicationDetailEntity.getPlatformSkuName());
            soDeliveryNoticeChangeDetailEntity.setPlatformSpu(requisitionApplicationDetailEntity.getPlatformSpu());
            list.add(soDeliveryNoticeChangeDetailEntity);
        });
        return list;
    }

}
