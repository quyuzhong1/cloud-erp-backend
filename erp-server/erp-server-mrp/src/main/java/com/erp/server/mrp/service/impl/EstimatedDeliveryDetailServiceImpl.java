package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.mrp.dto.InventoryDetailTotalDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.EstimatedDeliveryDetailEntity;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;
import com.erp.model.mrp.vo.EstimatedDeliveryVO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.mrp.mapper.EstimatedDeliveryDetailMapper;
import com.erp.server.mrp.service.BillShopInventoryDetailService;
import com.erp.server.mrp.service.EstimatedDeliveryDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 预计发货明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class EstimatedDeliveryDetailServiceImpl extends SuperServiceImpl<EstimatedDeliveryDetailMapper, EstimatedDeliveryDetailEntity> implements EstimatedDeliveryDetailService {

    @Resource
    private BillShopInventoryDetailService billShopInventoryDetailService;

    @Override
    public PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        Page<EstimatedDeliveryVO> page = baseMapper.estimatedDelivery(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        if (ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY.getCode().equals(params.getParams().getType())) {
            List<String> ids = page.getRecords()
                    .stream()
                    .map(EstimatedDeliveryVO::getId)
                    .collect(Collectors.toList());
            Map<String, Integer> shopInventoryMap = billShopInventoryDetailService.listByMainIdsAndShopId(ids, params.getParams().getShopId());
            List<String> receivingChannelIds = page.getRecords()
                    .stream()
                    .map(EstimatedDeliveryVO::getReceivingChannel)
                    .collect(Collectors.toList());
            List<ShopInfoEntity> shopInfos = FeignQuery.getByIds(ShopInfoEntity.class, receivingChannelIds);
            Map<String, String> shopMap = shopInfos.stream()
                    .collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName, (o1, o2) -> o1));
            List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, receivingChannelIds);
            Map<String, String> warehouseMap = warehouseList.stream()
                    .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName, (o1, o2) -> o1));
            for (EstimatedDeliveryVO vo : page.getRecords()) {
                if (CfgRuleInventoryNodeEnum.OVERSEAS_REPLENISHMENT_PLAN.getCode().equals(params.getParams().getSourceType())) {
                    vo.setReceivingChannelName(shopMap.get(vo.getReceivingChannel()));
                }else {
                    vo.setReceivingChannelName(warehouseMap.get(vo.getReceivingChannel()));
                }
                vo.setShopPreShipmentQty(shopInventoryMap.get(vo.getId()));
            }
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<EstimatedDeliveryDetailEntity> getByReplenishmentIdAndType(String detailId, ReplenishmentInventoryTypeEnum replenishmentInventoryTypeEnum) {
        return list(Wrappers.<EstimatedDeliveryDetailEntity>lambdaQuery()
                .eq(EstimatedDeliveryDetailEntity::getReplenishmentDetailId, detailId)
                .eq(EstimatedDeliveryDetailEntity::getType, replenishmentInventoryTypeEnum.getCode())
        );
    }

    @Override
    public int totalQtyByReplenishment(String detailId, ReplenishmentInventoryTypeEnum replenishmentInventoryTypeEnum) {
        return getByReplenishmentIdAndType(detailId, replenishmentInventoryTypeEnum).stream()
                .map(EstimatedDeliveryDetailEntity::getQty)
                .reduce(0, Math::addExact);
    }

    @Override
    public int totalQtyByReplenishmentAndSourceType(InventoryDetailTotalDTO params, ReplenishmentInventoryTypeEnum replenishmentInventoryTypeEnum) {
        List<EstimatedDeliveryDetailEntity> list = list(Wrappers.<EstimatedDeliveryDetailEntity>lambdaQuery()
                .eq(EstimatedDeliveryDetailEntity::getReplenishmentDetailId, params.getDetailId())
                .eq(EstimatedDeliveryDetailEntity::getSourceType, params.getSourceType())
                .eq(EstimatedDeliveryDetailEntity::getType, replenishmentInventoryTypeEnum.getCode()));
        if (ReplenishmentInventoryTypeEnum.FBA_ESTIMATED_DELIVERY.equals(replenishmentInventoryTypeEnum)) {
            return list.stream()
                    .map(EstimatedDeliveryDetailEntity::getQty)
                    .reduce(0, Math::addExact);
        }else {
            List<String> ids = list.stream()
                    .map(EstimatedDeliveryDetailEntity::getId)
                    .collect(Collectors.toList());
            return billShopInventoryDetailService.totalByMainIdsAndShopId(ids, params.getShopId());
        }
    }
}
