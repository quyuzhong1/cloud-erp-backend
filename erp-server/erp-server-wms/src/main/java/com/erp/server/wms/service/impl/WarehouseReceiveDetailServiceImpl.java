package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.annotation.DistributeLocker;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.QcNoticeDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.WarehouseReceiveDetailMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-06
 */
@Service
public class WarehouseReceiveDetailServiceImpl extends SuperServiceImpl<WarehouseReceiveDetailMapper, WarehouseReceiveDetailEntity> implements WarehouseReceiveDetailService {

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PoReturnDetailService poReturnDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private QcResultService qcResultService;

    @Resource
    private QcNoticeDetailService qcNoticeDetailService;

    /**
     * 新增
     *
     * @param dto dto
     * @param id  id:主表id
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 14:43
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(WarehouseReceiveDTO.AddDTO dto, String id) {
        //创建保存详情的集合
        List<WarehouseReceiveDetailEntity> listDetail = new ArrayList<>();
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = dto.getWarehouseReceiveDetailList().stream().map(WarehouseReceiveDetailDTO.AddDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
        //校验sku重复
        checkAddDetailsRepeatSku(purchaseOrderDetailEntities);
        List<WarehouseReceiveDetailEntity> detailEntityList = listWarehouseReceiveByPodIds(orderDetailIds);

        List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listReturnOrderDetailByPodIds(orderDetailIds);

        if(purchaseOrderDetailEntities.stream().anyMatch(v->!ExecutionStatusEnum.CONFIRM.getCode().equals(v.getExecutionStatus()) && !ExecutionStatusEnum.DELIVERY.getCode().equals(v.getExecutionStatus()))){
            throw new ServiceException(ApiError.PO_DETAIL_CONFIRM_OR_DELIVER_CAN_PUSH_RECEIPT);
        }
        //遍历需要保存的采购收货单详情信息，并赋值采购单信息
        List<WarehouseReceiveDetailDTO.AddDTO> warehouseReceiveDetailList = dto.getWarehouseReceiveDetailList();
        for (WarehouseReceiveDetailDTO.AddDTO addDTO : warehouseReceiveDetailList) {

            WarehouseReceiveDetailEntity warehouseReceiveDetailEntity = new WarehouseReceiveDetailEntity();
            warehouseReceiveDetailEntity.setMainId(id);
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(addDTO.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
            warehouseReceiveDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
            warehouseReceiveDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());
            warehouseReceiveDetailEntity.setPlanDeliveryDate(purchaseOrderDetailEntity.getPlanDeliveryDate());

            //退货补货数量
            Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //签收数量
            Integer receiveQty = detailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            //采购数量
            Integer purchaseQty = purchaseOrderDetailEntity.getPurchaseQty();
            if (receiveQty + addDTO.getReceiveQty() > purchaseQty + returnQty) {
                throw new ServiceException(ApiError.PO_RECEIPT_QTY_EXCEEDS_ALLOWED, purchaseOrderDetailEntity.getSkuNo());
            }

            warehouseReceiveDetailEntity.setReceiveQty(addDTO.getReceiveQty());
            warehouseReceiveDetailEntity.setExceedQty(addDTO.getExceedQty());
            warehouseReceiveDetailEntity.setRemark(addDTO.getRemark());
            warehouseReceiveDetailEntity.setPurchaseOrderDetailId(addDTO.getPurchaseOrderDetailId());
            warehouseReceiveDetailEntity.setCreateUserId(dto.getCreateUserId());
            warehouseReceiveDetailEntity.setCreateUserName(dto.getCreateUserId());
            warehouseReceiveDetailEntity.setUpdateUserId(dto.getUpdateUserId());
            warehouseReceiveDetailEntity.setUpdateUserName(dto.getUpdateUserName());
            warehouseReceiveDetailEntity.setSourceDetailId(addDTO.getSourceDetailId());

          /*  Integer receive = detailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            if (addDTO.getReceiveQty() > (purchaseOrderDetailEntity.getPurchaseQty() - receive)) {
                throw new ServiceException(ApiError.ERROR_99013);
            }*/
            listDetail.add(warehouseReceiveDetailEntity);
        }
        //保存详情信息
        boolean saveDetail = this.saveBatch(listDetail);
        if (!saveDetail) {
            throw new ServiceException(ApiError.BILL_SAVE_FAILED);
        }
        //重算待质检数量
        List<String> podIdList = listDetail.stream().map(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<String> detailIdList = listDetail.stream().map(WarehouseReceiveDetailEntity::getId).collect(Collectors.toList());
        recalculateWaitQcQty(podIdList,detailIdList);

        return Boolean.TRUE;
    }

    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeatSku(List<PurchaseOrderDetailEntity> list) {
        /*Map<String, List<PurchaseOrderDetailEntity>> map = list.stream().collect(Collectors.groupingBy(PurchaseOrderDetailEntity::getSkuId));
        for (Map.Entry<String, List<PurchaseOrderDetailEntity>> entry : map.entrySet()) {
            List<PurchaseOrderDetailEntity> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1, "sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }*/
    }

    /**
     * 修改
     *
     * @param dto dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 15:22
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(WarehouseReceiveDTO.UpdateDTO dto) {
        List<String> addList = dto.getWarehouseReceiveDetailList().stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(WarehouseReceiveDetailDTO.UpdateDTO::getId).collect(Collectors.toList());

        //创建保存详情的集合
        List<WarehouseReceiveDetailEntity> listDetail = new ArrayList<>();
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = dto.getWarehouseReceiveDetailList().stream().map(WarehouseReceiveDetailDTO.UpdateDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
        //校验sku重复
        checkAddDetailsRepeatSku(purchaseOrderDetailEntities);
        List<WarehouseReceiveDetailDTO.UpdateDTO> warehouseReceiveDetailList = dto.getWarehouseReceiveDetailList();
        List<WarehouseReceiveDetailEntity> detailEntityList = listWarehouseReceiveByPodIds(orderDetailIds);
        List<PoReturnDetailEntity> returnDetailEntityList = poReturnDetailService.listReturnOrderDetailByPodIds(orderDetailIds);

        //原明细数据
        List<WarehouseReceiveDetailEntity> oldList = this.listDetailByMainIds(Collections.singletonList(dto.getId()));
        List<String> deleteIds = getDeleteIds(dto.getWarehouseReceiveDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<WarehouseReceiveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        for (WarehouseReceiveDetailDTO.UpdateDTO updateDTO : warehouseReceiveDetailList) {
            WarehouseReceiveDetailEntity warehouseReceiveDetailEntity = new WarehouseReceiveDetailEntity();
            warehouseReceiveDetailEntity.setMainId(dto.getId());
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
            warehouseReceiveDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
            warehouseReceiveDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());
            warehouseReceiveDetailEntity.setPlanDeliveryDate(purchaseOrderDetailEntity.getPlanDeliveryDate());
            //退货补货数量
            Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //签收数量
            Integer receiveQty = detailEntityList.stream().filter(obj -> !deleteIds.contains(obj.getId()) && obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            //采购数量
            Integer purchaseQty = purchaseOrderDetailEntity.getPurchaseQty();

            if (CharSequenceUtil.isNotBlank(updateDTO.getId())) {
                Integer receive = detailEntityList.stream().filter(obj -> !deleteIds.contains(obj.getId()) && obj.getSkuId().equals(warehouseReceiveDetailEntity.getSkuId()) && obj.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && !obj.getId().equals(updateDTO.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                if (receive + updateDTO.getReceiveQty() > purchaseQty + returnQty) {
                    throw new ServiceException(ApiError.PO_RECEIPT_QTY_EXCEEDS_ALLOWED, purchaseOrderDetailEntity.getSkuNo());
                }
                warehouseReceiveDetailEntity.setId(updateDTO.getId());
            } else {
                if (receiveQty + updateDTO.getReceiveQty() > purchaseQty + returnQty) {
                    throw new ServiceException(ApiError.PO_RECEIPT_QTY_EXCEEDS_UNDELIVERED, purchaseOrderDetailEntity.getSkuNo());
                }
            }
            warehouseReceiveDetailEntity.setReceiveQty(updateDTO.getReceiveQty());
            warehouseReceiveDetailEntity.setExceedQty(updateDTO.getExceedQty());
            warehouseReceiveDetailEntity.setRemark(updateDTO.getRemark());
            warehouseReceiveDetailEntity.setPurchaseOrderDetailId(updateDTO.getPurchaseOrderDetailId());
            listDetail.add(warehouseReceiveDetailEntity);
            //修改操作日志
/*            if (CharSequenceUtil.isNotBlank(warehouseReceiveDetailEntity.getId())) {
                WarehouseReceiveDetailEntity old = this.getById(warehouseReceiveDetailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old,warehouseReceiveDetailEntity, ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
            }*/
        }
        boolean flag = this.saveOrUpdateBatch(listDetail);

        List<String> podIdList = listDetail.stream().map(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<String> detailIdList = listDetail.stream().map(WarehouseReceiveDetailEntity::getId).collect(Collectors.toList());
        //重算待质检数量
        recalculateWaitQcQty(podIdList,detailIdList);

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<WarehouseReceiveDetailEntity> receiveDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = receiveDetailEntityList.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), addPairList, "编辑操作");
        }
        return flag;
    }

    private List<String> getDeleteIds(List<WarehouseReceiveDetailDTO.UpdateDTO> newList, List<WarehouseReceiveDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(WarehouseReceiveDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(WarehouseReceiveDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 根据主表id删除
     *
     * @param mainIds mainIds
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @Override
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(WarehouseReceiveDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(WarehouseReceiveDetailEntity::getMainId, mainIds)
                .remove();
    }

    /**
     * 根据主表id查询详情表信息
     *
     * @param mainId mainId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     **/
    @Override
    public List<WarehouseReceiveDetailEntity> getDetailByMainId(String mainId) {
        LambdaQueryWrapper<WarehouseReceiveDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WarehouseReceiveDetailEntity::getMainId, mainId);
        queryWrapper.orderByAsc(WarehouseReceiveDetailEntity::getId);
        return this.list(queryWrapper);
    }

    @Override
    public List<WarehouseReceiveDetailEntity> listDetailByPodIds(List<String> podIds) {
        return lambdaQuery().in(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId, podIds).list();
    }


    /**
     * 根据主表集合获取详情
     *
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.WarehouseReceiveDetailEntity>
     * @author yl
     * @date 2023-05-05 17:38
     */
    @Override
    public List<WarehouseReceiveDetailEntity> listDetailByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(WarehouseReceiveDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public List<WarehouseReceiveDetailEntity> listWarehouseReceiveByPodIds(List<String> purchaseDetailIds) {
        if (CollectionUtils.isEmpty(purchaseDetailIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listWarehouseReceiveByPodIds(purchaseDetailIds);
    }

    @Override
    public String getSubArrivalStatus(List<String> podIds, Integer qty) {
        List<WarehouseReceiveDetailEntity> receiveDetailList = this.listWarehouseReceiveByPodIds(podIds);
        Integer receiveQty = MathUtil.ZERO;
        if (CollectionUtils.isNotEmpty(receiveDetailList)) {
            receiveQty = receiveDetailList.stream().map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
        }
        String arrivalStatus = ArrivalStatusEnum.NON_ARRIVAL.getCode();

        if (qty.intValue() > receiveQty.intValue() && receiveQty.intValue() > MathUtil.ZERO ) {
            arrivalStatus = ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode();
        }

        if (receiveQty.intValue() == qty.intValue()) {
            arrivalStatus = ArrivalStatusEnum.ARRIVED.getCode();
        }

        return arrivalStatus;
    }

    /**
     * 根据主表id和skuId删除
     *
     * @param mainId mainId
     * @param skuId skuId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @Override
    public Boolean deleteBySkuId(String mainId, String skuId) {
        return lambdaUpdate().set(WarehouseReceiveDetailEntity::getIsDeleted, Boolean.TRUE)
                .eq(WarehouseReceiveDetailEntity::getMainId, mainId)
                .eq(WarehouseReceiveDetailEntity::getSkuId, skuId)
                .update();
    }

    @Override
    public void updateKingdeeDetailId(JSONArray list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Object obj : list) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            String detailId = (String) jsonObject.get("detailId");
            String kingdeeDetailId = (String) jsonObject.get("kingdeeDetailId");
            this.lambdaUpdate()
                    .set(WarehouseReceiveDetailEntity::getKingdeeDetailId, kingdeeDetailId)
                    .eq(WarehouseReceiveDetailEntity::getId, detailId)
                    .update();
        }
    }

    @Override
    public void updateInfo(WarehouseReceiveDetailEntity wrd) {
        baseMapper.updateInfo(wrd.getId(),wrd.getInStockStatus());
    }

    @Override
    @DistributeLocker(keyName = "podIdList")
    public void recalculateWaitQcQty(List<String> podIdList, List<String> detailIdList) {
        if (CollUtil.isEmpty(podIdList)) {
            return;
        }

        //查询当前收货单明细的待质检数量
        List<WarehouseReceiveDetailEntity> receiveDetailLIst = this.listByIds(detailIdList);
        if (CollUtil.isEmpty(receiveDetailLIst)) {
            throw new ServiceException(ApiError.PO_RECEIPT_NOT_FOUND);
        }

        //查询采购订单下的质检批次合格数量汇总
        List<QcResultDTO.TotalLotQualifiedQtyDTO> totalAllowInstockQtyList = qcResultService.getTotalLotQualifiedQtyByPodId(podIdList);
        Map<String, Integer> totalAllowInstockQtyMap = totalAllowInstockQtyList.stream().collect(Collectors.toMap(QcResultDTO.TotalLotQualifiedQtyDTO::getPurchaseOrderDetailId, QcResultDTO.TotalLotQualifiedQtyDTO::getTotalAllowInstockQty));

        //查询采购订单下的其他收货单的收货数量汇总
        List<WarehouseReceiveDetailDTO.ReceiveQtyDTO> totalReceiveQtyList = baseMapper.getTotalReceiveQty(podIdList);
        Map<String, Integer> totalReceiveQtyMap = totalReceiveQtyList.stream().collect(Collectors.toMap(WarehouseReceiveDetailDTO.ReceiveQtyDTO::getPodId, WarehouseReceiveDetailDTO.ReceiveQtyDTO::getTotalReceiveQty));

        //查询采购订单下的其他收货单的收货数量汇总
        List<WarehouseReceiveDetailDTO.WaitQcQtyDTO> totalWaitQcQtyList = baseMapper.getTotalWaitQcQty(podIdList);

        for (WarehouseReceiveDetailEntity receiveDetailEntity : receiveDetailLIst) {
            //采购订单明细下的批次质检合格数量汇总
            Integer allowInstockQty = totalAllowInstockQtyMap.get(receiveDetailEntity.getPurchaseOrderDetailId());
            //采购订单明细下的收货数量汇总
            Integer totalReceiveQty = totalReceiveQtyMap.get(receiveDetailEntity.getPurchaseOrderDetailId());

            //采购订单明细下收货单的待质检数量汇总（不包括本单）
            Integer totalWaitQcQty = totalWaitQcQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getPodId(), receiveDetailEntity.getPurchaseOrderDetailId()) && !CharSequenceUtil.equals(obj.getDetailId(), receiveDetailEntity.getId())).map(WarehouseReceiveDetailDTO.WaitQcQtyDTO::getTotalWaitQcQty).reduce(MathUtil.ZERO, Integer::sum);
           //待质检量=∑收货数量-质检合格量-∑待质检量,小于0时默认为0
            Integer waitQcQty =  totalReceiveQty - (ObjectUtil.isNull(allowInstockQty) ? MathUtil.ZERO : allowInstockQty) - totalWaitQcQty;
            if (waitQcQty < MathUtil.ZERO) {
                waitQcQty = MathUtil.ZERO;
            }
            receiveDetailEntity.setWaitQcQty(waitQcQty);
        }
        super.updateBatchById(receiveDetailLIst);
    }


    @Override
    public void updateWaitQcQty(List<String> qcIdList,Boolean isFinishQc) {
        List<QcResultDTO.LotQualifiedQtyDTO> allowInstockQtyList = qcResultService.getLotQualifiedQtyByMainIdList(qcIdList);
        if (CollUtil.isEmpty(allowInstockQtyList)) {
            return;
        }
        List<String> sourceDetailIdList = allowInstockQtyList.stream().map(QcResultDTO.LotQualifiedQtyDTO::getSourceDetailId).distinct().collect(Collectors.toList());
        //质检通知单信息
        Map<String, QcNoticeDetailEntity> qcNoticeDetailEntityMap = qcNoticeDetailService.mapByIds(sourceDetailIdList);
        if (ObjectUtil.isNotEmpty(qcNoticeDetailEntityMap)) {
            List<String> qcNoticeSourceDetailIdList = qcNoticeDetailEntityMap.values().stream().map(QcNoticeDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
            sourceDetailIdList.addAll(qcNoticeSourceDetailIdList);
        }
        //采购收货明细信息
        Map<String, WarehouseReceiveDetailEntity> warehouseReceiveDetailEntityMap = this.mapByIds(sourceDetailIdList);

        //需要更新的采购收货明细信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = new ArrayList<>();

        //更新待质检数量
        List<String> distQcIdList = qcIdList.stream().distinct().collect(Collectors.toList());
        for (String distQcId : distQcIdList) {
            QcResultDTO.LotQualifiedQtyDTO lotQualifiedQtyDTO = allowInstockQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getQcId(), distQcId)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(lotQualifiedQtyDTO)) {
                continue;
            }
            //采购收货来源直接取来源明细id
            String sourceDetailId = lotQualifiedQtyDTO.getSourceDetailId();
            //质检通知单来源需要取质检通知单明细的来源明细id
            if (CharSequenceUtil.equals(lotQualifiedQtyDTO.getSourceType(), SourceTypeEnum.QC_NOTICE.getCode())) {
                QcNoticeDetailEntity qcNoticeDetailEntity = qcNoticeDetailEntityMap.get(lotQualifiedQtyDTO.getSourceDetailId());
                if (ObjectUtil.isEmpty(qcNoticeDetailEntity)) {
                    throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE,"质检通知单明细");
                }
                sourceDetailId = qcNoticeDetailEntity.getSourceDetailId();
            }
            WarehouseReceiveDetailEntity receiveDetailEntity = warehouseReceiveDetailEntityMap.get(sourceDetailId);
            if (ObjectUtil.isEmpty(receiveDetailEntity)) {
                throw new ServiceException(ApiError.PO_RECEIPT_NOT_FOUND);
            }
            Integer waitQcQty = MathUtil.ZERO;
            if (isFinishQc) {
                //待质检量=∑收货数量-质检合格量,小于0时默认为0
                waitQcQty = receiveDetailEntity.getWaitQcQty() - lotQualifiedQtyDTO.getAllowInstockQty();
            } else {
                //待质检量=∑收货数量+质检合格量,小于0时默认为0
                waitQcQty = receiveDetailEntity.getWaitQcQty() + lotQualifiedQtyDTO.getAllowInstockQty();
            }
            if (waitQcQty < MathUtil.ZERO) {
                waitQcQty = MathUtil.ZERO;
            }
            receiveDetailEntity.setWaitQcQty(waitQcQty);
            receiveDetailList.add(receiveDetailEntity);
        }
        super.updateBatchById(receiveDetailList);
    }
}
