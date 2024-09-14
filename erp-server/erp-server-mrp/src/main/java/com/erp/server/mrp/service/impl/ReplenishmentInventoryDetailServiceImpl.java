package com.erp.server.mrp.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.InventoryTotalDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.ReplenishmentInventoryDetailEntity;
import com.erp.model.mrp.entity.ShopInventoryDetailEntity;
import com.erp.model.mrp.enums.CfgRuleInventoryAllocateTypeEnum;
import com.erp.model.mrp.vo.InventoryDetailVO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.mrp.mapper.ReplenishmentInventoryDetailMapper;
import com.erp.server.mrp.service.ReplenishmentInventoryDetailService;
import com.erp.server.mrp.service.ShopInventoryDetailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 库存详情 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class ReplenishmentInventoryDetailServiceImpl extends SuperServiceImpl<ReplenishmentInventoryDetailMapper, ReplenishmentInventoryDetailEntity> implements ReplenishmentInventoryDetailService {

    @Resource
    private ShopInventoryDetailService shopInventoryDetailService;
    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Override
    public List<ReplenishmentInventoryDetailEntity> listByReplenishmentDetailIds(List<String> ids) {
        return list(Wrappers.<ReplenishmentInventoryDetailEntity>lambdaQuery().in(ReplenishmentInventoryDetailEntity::getReplenishmentDetailId, ids));
    }

    @Override
    public List<InventoryDetailVO> inventoryDetail(InventoryTotalDTO params) {
        List<ReplenishmentInventoryDetailEntity> inventoryDetail = list(Wrappers.<ReplenishmentInventoryDetailEntity>lambdaQuery()
                .eq(ReplenishmentInventoryDetailEntity::getReplenishmentDetailId, params.getDetailId())
                .eq(ReplenishmentInventoryDetailEntity::getInventoryType, params.getType())
        );
        if (CollectionUtils.isEmpty(inventoryDetail)) {
            return Collections.emptyList();
        }
        List<String> ids = inventoryDetail.stream().map(ReplenishmentInventoryDetailEntity::getId).collect(Collectors.toList());
        List<String> warehouseIdList = inventoryDetail.stream().map(ReplenishmentInventoryDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        //虚拟仓信息
        List<String> virtualWarehouseIdList = inventoryDetail.stream().map(ReplenishmentInventoryDetailEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntities = new ArrayList<>();
        List<VirtualWarehouseEntity> virtualWarehouseEntities = new ArrayList<>();
        if (!CollectionUtils.isEmpty(warehouseIdList)) {
            warehouseEntities = FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);
        }
        if (!CollectionUtils.isEmpty(virtualWarehouseIdList)) {
            virtualWarehouseEntities = FeignQuery.getByIds(VirtualWarehouseEntity.class, virtualWarehouseIdList);
        }
        List<ShopInventoryDetailEntity> shopInventoryDetailList = shopInventoryDetailService.list(Wrappers.<ShopInventoryDetailEntity>lambdaQuery()
                .in(ShopInventoryDetailEntity::getMainId, ids));
        ArrayList<InventoryDetailVO> detailVOS = new ArrayList<>();
        for (ReplenishmentInventoryDetailEntity entity : inventoryDetail) {
            InventoryDetailVO detailVO = InventoryDetailVO.buildInventoryDetailVO(entity);
            WarehouseEntity warehouse = warehouseEntities.stream()
                    .filter(e -> e.getId().equals(entity.getWarehouseId()))
                    .findFirst().orElse(new WarehouseEntity());
            detailVO.setWarehouseName(warehouse.getName());
            VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseEntities.stream()
                    .filter(e -> e.getId().equals(entity.getVirtualWarehouseId()))
                    .findFirst().orElse(new VirtualWarehouseEntity());
            detailVO.setVirtualWarehouseName(virtualWarehouseEntity.getName());
            ApiResult<List<ShopInfoEntity>> allShopInfoResult = shopInfoFeign.list();
            List<ShopInfoEntity> allShopInfo = allShopInfoResult.getData();
            if (CfgRuleInventoryAllocateTypeEnum.AUTO_ALLOCATION.getCode().equals(detailVO.getInventoryAllocateType())) {
                List<ShopInventoryDetailEntity> detailEntities = shopInventoryDetailList.stream()
                        .filter(v -> v.getMainId().equals(entity.getId()))
                        .collect(Collectors.toList());
                for (ShopInventoryDetailEntity detail : detailEntities) {
                    InventoryDetailVO.ShopInventoryDetailVO vo = new InventoryDetailVO.ShopInventoryDetailVO();
                    vo.setQty(detail.getQty());
                    vo.setShopId(detail.getShopId());
                    String shopName = allShopInfo.stream().filter(v -> vo.getShopId().equals(v.getId()))
                            .map(ShopInfoEntity::getName).findFirst().orElse("");
                    vo.setShopName(shopName);
                }
            }
            if (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(detailVO.getChannelType())) {
                detailVO.setChannelName(Collections.singletonList("全部店铺"));
            } else {
                List<String> shopNames = allShopInfo.stream().filter(v -> detailVO.getChannelIdJson().contains(v.getId()))
                        .distinct().map(ShopInfoEntity::getName).collect(Collectors.toList());
                detailVO.setChannelName(shopNames);
            }
            detailVOS.add(detailVO);
        }
        return detailVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInventoryDetail(List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> inventoryDetail, String replenishmentDetailId, String calcVersion) {
        List<ReplenishmentInventoryDetailEntity> entities = new ArrayList<>();
        List<ShopInventoryDetailEntity> detailEntities = new ArrayList<>();
        for (ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO dto : inventoryDetail) {
            ReplenishmentInventoryDetailEntity entity = ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO.buildReplenishmentInventoryDetail(dto, replenishmentDetailId, calcVersion);
            entity.setId(IdWorker.getIdStr());
            entities.add(entity);
            for (ReplenishmentResultDTO.ShopInventoryDetailDTO detail : dto.getShopInventoryDetails()) {
                detailEntities.add(ReplenishmentResultDTO.ShopInventoryDetailDTO.buildShopInventoryDetail(detail,entity.getId(), calcVersion));
            }
        }
        SpringUtil.getBean(ReplenishmentInventoryDetailServiceImpl.class).saveBatch(entities);
        shopInventoryDetailService.saveBatch(detailEntities);
    }
}
