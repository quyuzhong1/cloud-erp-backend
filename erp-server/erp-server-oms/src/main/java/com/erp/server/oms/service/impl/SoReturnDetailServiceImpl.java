package com.erp.server.oms.service.impl;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.dto.SoReturnDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoReturnDetailMapper;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import com.erp.server.oms.service.SoReturnDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Override
    public Boolean add(SoReturnDTO.Add dto, String id) {
        List<String> detailIds = dto.getDetailList().stream().map(SoReturnDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailBySourceId(detailIds);
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(detailDto.getSourceDetailId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            soReturnDetailEntity.setMainId(id);
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(soReturnDetailEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean update(SoReturnDTO.Update dto) {
        List<String> detailIds = dto.getDetailList().stream().map(SoReturnDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByIds(detailIds);

        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailBySourceId(detailIds);
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(detailDto.getSourceDetailId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            if (actualQty < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(soReturnDetailEntity);
        }
        return this.saveOrUpdateBatch(list);
    }

    @Override
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
        List<String> detailIds = list.stream().map(SoDetailDTO.AddDetailView::getId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailByMainId(id);
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
        List<String> skuIdList = list.stream().map(SoDetailDTO.AddDetailView::getSkuId).distinct().collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        SoInfoEntity soInfoEntity = soInfoService.getById(id);
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        for (SoDetailDTO.AddDetailView addDetailView : list) {
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(addDetailView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            addDetailView.setVariantProperty(productDetailEntity.getVariantProperty());
            addDetailView.setProductName(productDetailEntity.getName());
            WarehouseDTO.UpdateDTO warehouse = warehouseList.stream().filter(req -> req.getOrgId().equals(addDetailView.getInventoryOrgId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            addDetailView.setInventoryOrgName(warehouse.getName());
            //获取退货数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(addDetailView.getId()) && ReturnTypeEnum.REPLENISHMENT.getCode().equals(req.getReturnTypeDict())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //获取已出库数量
            Integer actualQty = soOutstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(addDetailView.getId())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(addDetailView.getSkuId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            addDetailView.setAvailableQty(getAvailableQty(curInventoryQty, addDetailView.getSalesQty()));
            addDetailView.setDeliveryQty(actualQty);
            addDetailView.setUnDeliveryQty(addDetailView.getSalesQty() + returnQty - actualQty);
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
