package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.mrp.dto.InventoryDetailTotalDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.EstimatedPurchaseDetailEntity;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.vo.EstimatedPurchaseVO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.mrp.mapper.EstimatedPurchaseDetailMapper;
import com.erp.server.mrp.service.EstimatedPurchaseDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 预计采购明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class EstimatedPurchaseDetailServiceImpl extends SuperServiceImpl<EstimatedPurchaseDetailMapper, EstimatedPurchaseDetailEntity> implements EstimatedPurchaseDetailService {

    @Override
    public PagingVO<EstimatedPurchaseVO> estimatedPurchase(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        Page<EstimatedPurchaseVO> page = baseMapper.estimatedPurchase(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        List<String> receivingChannelIds = page.getRecords()
                .stream()
                .map(EstimatedPurchaseVO::getReceivingChannel)
                .collect(Collectors.toList());
        List<ShopInfoEntity> shopInfos = FeignQuery.getByIds(ShopInfoEntity.class, receivingChannelIds);
        Map<String, String> shopMap = shopInfos.stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName, (o1, o2) -> o1));
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, receivingChannelIds);
        Map<String, String> warehouseMap = warehouseList.stream()
                .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName, (o1, o2) -> o1));
        for (EstimatedPurchaseVO vo : page.getRecords()) {
            if (CfgRuleInventoryNodeEnum.LOCAL_REPLENISHMENT_PLAN.getCode().equals(params.getParams().getSourceType())) {
                vo.setReceivingChannelName(shopMap.get(vo.getReceivingChannel()));
            }else {
                vo.setReceivingChannelName(warehouseMap.get(vo.getReceivingChannel()));
            }
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<EstimatedPurchaseDetailEntity> getByReplenishmentId(String detailId) {
        return list(Wrappers.<EstimatedPurchaseDetailEntity>lambdaQuery()
                .eq(EstimatedPurchaseDetailEntity::getReplenishmentDetailId, detailId));
    }

    @Override
    public int totalQtyByReplenishment(String detailId) {
        return getByReplenishmentId(detailId).stream()
                .map(EstimatedPurchaseDetailEntity::getQty)
                .reduce(0, Math::addExact);
    }

    @Override
    public int totalQtyByReplenishmentAndSourceType(InventoryDetailTotalDTO params) {
        List<EstimatedPurchaseDetailEntity> list = list(Wrappers.<EstimatedPurchaseDetailEntity>lambdaQuery().eq(EstimatedPurchaseDetailEntity::getReplenishmentDetailId, params.getDetailId())
                .eq(EstimatedPurchaseDetailEntity::getSourceType, params.getSourceType())
        );
        return list.stream()
                .map(EstimatedPurchaseDetailEntity::getShopPreQty)
                .reduce(0 ,Math::addExact);
    }
}
