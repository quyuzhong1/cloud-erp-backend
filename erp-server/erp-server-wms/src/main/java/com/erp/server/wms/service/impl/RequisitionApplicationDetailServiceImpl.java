package com.erp.server.wms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.RequisitionApplicationConverter;
import com.erp.server.wms.mapper.RequisitionApplicationDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.RequisitionApplicationDetailService;
import com.erp.server.wms.service.VirtualWarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 要货申请单明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class RequisitionApplicationDetailServiceImpl extends SuperServiceImpl<RequisitionApplicationDetailMapper, RequisitionApplicationDetailEntity> implements RequisitionApplicationDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(RequisitionApplicationDTO.AddDTO addDTO, String mainId) {
        List<RequisitionApplicationDetailEntity> list = RequisitionApplicationConverter.INSTANCE.detailConvert(addDTO.getDetailList());

        // 数据处理
        handleData(list, mainId, Boolean.FALSE);

        log.info("开始新增要货申请单明细单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("要货申请单明细单保存失败");
        }

    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(RequisitionApplicationDTO.UpdateDTO updateDTO, String mainId) {
        //原明细数据
        List<RequisitionApplicationDetailEntity> oldList = this.listByMainIds(Arrays.asList(updateDTO.getId()));
        List<String> deleteIds = getDeleteIds(updateDTO.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<RequisitionApplicationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        List<RequisitionApplicationDetailEntity> list = BeanMapper.copyList(updateDTO.getDetailList(), RequisitionApplicationDetailEntity.class);

        // 数据处理
        handleData(list, mainId, Boolean.TRUE);

        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("要货申请单明细单保存失败");
        }
    }

    @Override
    public List<RequisitionApplicationDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(RequisitionApplicationDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public Boolean updateTransferWarehouse(String fromWarehouseId, String fromWarehouseName, String fromVirtualWarehouseId,
                                           String fromVirtualWarehouseName, String toWarehouseId, String toWarehouseName, Integer approveQty, String id) {
        LambdaUpdateChainWrapper<RequisitionApplicationDetailEntity> eq = lambdaUpdate().set(RequisitionApplicationDetailEntity::getFromWarehouseId, fromWarehouseId)
                .set(RequisitionApplicationDetailEntity::getFromWarehouseName, fromWarehouseName)
                .set(RequisitionApplicationDetailEntity::getToWarehouseId, toWarehouseId)
                .set(RequisitionApplicationDetailEntity::getToWarehouseName, toWarehouseName)
                .set(RequisitionApplicationDetailEntity::getApproveQty, approveQty)
                .set(RequisitionApplicationDetailEntity::getVirtualFrozenQty,approveQty)
                .eq(RequisitionApplicationDetailEntity::getId, id);
        if (StringUtils.isNotBlank(fromVirtualWarehouseId)) {
            eq.set(RequisitionApplicationDetailEntity::getFromVirtualWarehouseId, fromVirtualWarehouseId)
                    .set(RequisitionApplicationDetailEntity::getFromVirtualWarehouseName, fromVirtualWarehouseName);
        }
        return eq.update();
    }

    @Override
    public Boolean updateFinishDetailPickingQty(Integer pickingQty, String id) {
        return lambdaUpdate().set(RequisitionApplicationDetailEntity::getPickingQty, pickingQty)
                .eq(RequisitionApplicationDetailEntity::getId, id)
                .update();
    }

    @Override
    public Boolean removeByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(RequisitionApplicationDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public void cleanVirtualWarehouseIdByMianId(String mainId) {
        lambdaUpdate().eq(RequisitionApplicationDetailEntity::getMainId,mainId)
                .set(RequisitionApplicationDetailEntity::getFromVirtualWarehouseId,"")
                .set(RequisitionApplicationDetailEntity::getFromVirtualWarehouseName,"")
                .set(RequisitionApplicationDetailEntity::getVirtualFrozenQty, MathUtil.ZERO)
                .update();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<RequisitionApplicationDetailEntity> list, String mainId, Boolean isUpdate) {
        List<RequisitionApplicationDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));

        //需要新增的数据
        List<RequisitionApplicationDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        List<String> fromVirtualWarehouseIdList = list.stream().map(RequisitionApplicationDetailEntity::getFromVirtualWarehouseId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        Map<String,String> virtualWarehouseNameMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(fromVirtualWarehouseIdList)){
            List<VirtualWarehouseEntity> virtualWarehouseEntities = virtualWarehouseService.listByIds(fromVirtualWarehouseIdList);
            virtualWarehouseNameMap = virtualWarehouseEntities.stream().collect(Collectors.toMap(BaseEntity::getId, VirtualWarehouseEntity::getName));
        }
        //获取sku信息
        List<String> skuIdList = list.stream().map(RequisitionApplicationDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : list) {
            requisitionApplicationDetailEntity.setMainId(mainId);

            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(requisitionApplicationDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            requisitionApplicationDetailEntity.setSkuNo(skuVO.getSkuNo());
            String virtualWarehouseName = virtualWarehouseNameMap.get(requisitionApplicationDetailEntity.getFromVirtualWarehouseId());
            requisitionApplicationDetailEntity.setFromVirtualWarehouseName(StringUtils.isNotBlank(virtualWarehouseName)?virtualWarehouseName:"");
            if(requisitionApplicationDetailEntity.getFromVirtualWarehouseId() == null){
                requisitionApplicationDetailEntity.setFromVirtualWarehouseId("");
            }

            //校验是否是修改，如果是就新增修改日志
            if (StringUtils.isNotBlank(requisitionApplicationDetailEntity.getId())) {
                RequisitionApplicationDetailEntity old = oldList.stream().filter(obj -> obj.getId().equals(requisitionApplicationDetailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_REQUISITION_APPLICATION);
                }
                operateLogService.addModuleOperateLogByObj(old, requisitionApplicationDetailEntity, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), mainId,"", String.format("【%s】", old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), addPairList, "编辑操作");
        }
    }

    private List<String> getDeleteIds(List<RequisitionApplicationDetailDTO.UpdateDTO> newList, List<RequisitionApplicationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> org.apache.commons.lang3.StringUtils.isNotBlank(g.getId())).
                map(RequisitionApplicationDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(RequisitionApplicationDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
