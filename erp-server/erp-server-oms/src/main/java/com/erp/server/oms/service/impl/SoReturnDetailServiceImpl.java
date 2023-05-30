package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.dto.SoReturnDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.server.oms.mapper.SoReturnDetailMapper;
import com.erp.server.oms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 退货单详情 服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnDetailServiceImpl extends SuperServiceImpl<SoReturnDetailMapper, SoReturnDetailEntity> implements SoReturnDetailService {

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private InventoryFeign inventoryFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoReturnReceiveFeign soReturnReceiveFeign;

    @Resource
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoReturnService soReturnService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnDTO.Add dto, String id) {
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(Arrays.asList(dto.getSourceId()));
        //获取退货单明细表ids
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        //获取退货详情
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByIds(returnDetailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailBySourceId(returnDetailIds);
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(soDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }

            soReturnDetailEntity.setMainId(id);
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(soReturnDetailEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnDetailDTO.Update::getId).collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(Arrays.asList(dto.getSourceId()));
        //获取退货单明细表ids
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        //获取退货详情
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByIds(returnDetailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        //原明细数据
        List<SoReturnDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailBySourceId(returnDetailIds);
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(soDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            if (StringUtils.isNotBlank(detailDto.getId())) {
                soReturnDetailEntity.setId(detailDto.getId());
                returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            if (actualQty < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            soReturnDetailEntity.setMainId(dto.getId());
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(soReturnDetailEntity);
            detailDto.setSkuNo(soDetailEntity.getSkuNo());
            //修改操作日志
            if (StringUtils.isNotBlank(soReturnDetailEntity.getId())) {
                SoReturnDetailEntity old = this.getById(soReturnDetailEntity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                operateLogService.addModuleOperateLogByObj(old,soReturnDetailEntity, ModuleTypeEnum.SO_RETURN.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }
        boolean falg = this.saveOrUpdateBatch(list);
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnDetailEntity> returnDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnDetailEntityList.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return falg;
    }

    private List<String> getDeleteIds(List<SoReturnDetailDTO.Update> newList, List<SoReturnDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoReturnDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoReturnDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoReturnDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoReturnDetailEntity> listDetailByMainId(String mainId) {
        return lambdaQuery().eq(SoReturnDetailEntity::getMainId, mainId).list();
    }

    @Override
    public List<SoReturnDetailEntity> listDetailByMainIds(List<String> mainIds) {
        return lambdaQuery().in(SoReturnDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public List<SoReturnDetailEntity> listDetailBySourceId(List<String> sourceIds) {
        return baseMapper.listDetailBySourceId(sourceIds);
    }

    @Override
    public List<SoReturnDetailEntity> listDetailByIds(List<String> ids) {
        return baseMapper.listDetailByIds(ids);
    }

    @Override
    public List<SoDetailDTO.AddDetailView> listAddDetailView(String id) {
        List<SoDetailDTO.AddDetailView> list = baseMapper.listAddDetailView(id);
        List<String> soIds = list.stream().map(SoDetailDTO.AddDetailView::getSourceId).distinct().collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailByMainId(id);
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
        List<String> skuIdList = list.stream().map(SoDetailDTO.AddDetailView::getSkuId).distinct().collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        SoReturnEntity soReturnEntity = soReturnService.getById(id);
        SoInfoEntity soInfoEntity = soInfoService.getById(soReturnEntity.getSourceId());
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        //获取退货单id
        List<String> returnMainIds = list.stream().map(SoDetailDTO.AddDetailView::getMainId).distinct().collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveFeign.listDetailBySourceIds(returnMainIds);

        for (SoDetailDTO.AddDetailView addDetailView : list) {
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(addDetailView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            addDetailView.setVariantProperty(productDetailEntity.getVariantProperty());
            addDetailView.setProductName(productDetailEntity.getName());
            WarehouseDTO.UpdateDTO warehouse = warehouseList.stream().filter(req -> req.getOrgId().equals(addDetailView.getInventoryOrgId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            addDetailView.setInventoryOrgName(warehouse.getName());
            //获取退货数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(addDetailView.getId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //获取已出库数量
            Integer actualQty = soOutstockDetailEntities.stream().filter(req -> addDetailView.getSoId().equals(req.getSoId()) && req.getSkuId().equals(addDetailView.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(addDetailView.getSkuId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            addDetailView.setAvailableQty(getAvailableQty(curInventoryQty, addDetailView.getSalesQty()));
            addDetailView.setDeliveryQty(actualQty);
            addDetailView.setUnDeliveryQty(addDetailView.getSalesQty() - actualQty);
            addDetailView.setReturnQty(returnQty);
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> addDetailView.getId().equals(req.getSourceDetailId()) && req.getSkuId().equals(addDetailView.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            addDetailView.setReceiveQty(receiveQty);
            addDetailView.setMustQty(returnQty);
        }
        return list;
    }

    /**
     * 获取可用数量
     *
     * @param curInventoryQty 即时库存数量
     * @param salesQty        销售数量
     * @return java.lang.Integer
     * @Author Luo_WG
     * @Date 2023/5/17 11:06
     **/
    private Integer getAvailableQty(Integer curInventoryQty, Integer salesQty) {
        /**
         * 可出数量
         * 根据可用即时库存计算可出数量，
         * 当可用即时库存数量大于销售数量时 可出数量=销售数量；
         * 若可用即时库存数量小于销售数量，可出数量=即时可用库存数量
         */
        Integer availableQty = 0;
        Boolean isGre = curInventoryQty > salesQty;
        if (isGre) {
            availableQty = salesQty;
        } else {
            availableQty = curInventoryQty;
        }
        return availableQty;
    }
}
