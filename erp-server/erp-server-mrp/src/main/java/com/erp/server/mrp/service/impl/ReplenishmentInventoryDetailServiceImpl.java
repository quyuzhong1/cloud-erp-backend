package com.erp.server.mrp.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.InventoryTotalDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.ReplenishmentInventoryDetailEntity;
import com.erp.model.mrp.entity.ShopInventoryDetailEntity;
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
    public PagingVO<InventoryDetailVO> inventoryDetail(PagingDTO<InventoryTotalDTO> params) {
        Page<InventoryDetailVO> page = baseMapper.inventoryDetail(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        ApiResult<List<ShopInfoEntity>> allShopInfoResult = shopInfoFeign.list();
        List<ShopInfoEntity> allShopInfo = allShopInfoResult.getData();
        List<String> warehouseIdList = page.getRecords().stream().map(InventoryDetailVO::getWarehouseId).distinct().collect(Collectors.toList());
        //虚拟仓信息
        List<String> virtualWarehouseIdList = page.getRecords().stream().map(InventoryDetailVO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntities = new ArrayList<>();
        List<VirtualWarehouseEntity> virtualWarehouseEntities = new ArrayList<>();
        if (!CollectionUtils.isEmpty(warehouseIdList)) {
            warehouseEntities = FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);
        }
        if (!CollectionUtils.isEmpty(virtualWarehouseIdList)) {
            virtualWarehouseEntities = FeignQuery.getByIds(VirtualWarehouseEntity.class, virtualWarehouseIdList);
        }
        for (InventoryDetailVO detailVO : page.getRecords()) {
            WarehouseEntity warehouse = warehouseEntities.stream()
                    .filter(e -> e.getId().equals(detailVO.getWarehouseId()))
                    .findFirst().orElse(new WarehouseEntity());
            detailVO.setWarehouseName(warehouse.getName());
            VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseEntities.stream()
                    .filter(e -> e.getId().equals(detailVO.getVirtualWarehouseId()))
                    .findFirst().orElse(new VirtualWarehouseEntity());
            detailVO.setVirtualWarehouseName(virtualWarehouseEntity.getName());
            ShopInfoEntity shopInfo = allShopInfo.stream()
                    .filter(e -> e.getId().equals(detailVO.getShopId()))
                    .findFirst().orElse(new ShopInfoEntity());
            detailVO.setShopName(shopInfo.getName());
            if (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(detailVO.getChannelType())) {
                detailVO.setChannelName(Collections.singletonList("全部店铺"));
            } else {
                List<String> shopNames = allShopInfo.stream().filter(v -> detailVO.getChannelIdJson().contains(v.getId()))
                        .distinct().map(ShopInfoEntity::getName).collect(Collectors.toList());
                detailVO.setChannelName(shopNames);
            }
        }
        return new PagingVO<>(page);
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
