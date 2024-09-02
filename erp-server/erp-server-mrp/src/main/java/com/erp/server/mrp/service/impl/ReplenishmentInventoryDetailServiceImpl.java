package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.InventoryTotalDTO;
import com.erp.model.mrp.entity.ReplenishmentInventoryDetailEntity;
import com.erp.model.mrp.entity.ShopInventoryDetailEntity;
import com.erp.model.mrp.enums.CfgRuleInventoryAllocateTypeEnum;
import com.erp.model.mrp.vo.InventoryDetailVO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.mrp.mapper.ReplenishmentInventoryDetailMapper;
import com.erp.server.mrp.service.ReplenishmentInventoryDetailService;
import com.erp.server.mrp.service.ShopInventoryDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    private ShopInfoFeign shopInfoFeign;

    @Override
    public List<ReplenishmentInventoryDetailEntity> listByReplenishmentDetailIds(List<String> ids) {
        return list(Wrappers.<ReplenishmentInventoryDetailEntity>lambdaQuery().in(ReplenishmentInventoryDetailEntity::getReplenishmentDetailId, ids));
    }

    @Override
    public InventoryDetailVO inventoryDetail(InventoryTotalDTO params) {
        ReplenishmentInventoryDetailEntity inventoryDetail = getOne(Wrappers.<ReplenishmentInventoryDetailEntity>lambdaQuery()
                .eq(ReplenishmentInventoryDetailEntity::getReplenishmentDetailId, params.getId())
                .eq(ReplenishmentInventoryDetailEntity::getInventoryType, params.getType())
        );
        InventoryDetailVO detailVO = InventoryDetailVO.buildInventoryDetailVO(inventoryDetail);
        ApiResult<List<ShopInfoEntity>> allShopInfoResult = shopInfoFeign.list();
        List<ShopInfoEntity> allShopInfo = allShopInfoResult.getData();
        if (CfgRuleInventoryAllocateTypeEnum.AUTO_ALLOCATION.getCode().equals(detailVO.getInventoryAllocateType())) {
            List<ShopInventoryDetailEntity> shopInventoryDetailList = shopInventoryDetailService.list(Wrappers.<ShopInventoryDetailEntity>lambdaQuery()
                    .eq(ShopInventoryDetailEntity::getMainId, inventoryDetail.getId()));
            for (ShopInventoryDetailEntity entity : shopInventoryDetailList) {
                InventoryDetailVO.ShopInventoryDetailVO vo = new InventoryDetailVO.ShopInventoryDetailVO();
                vo.setQty(entity.getQty());
                vo.setShopId(entity.getShopId());
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
        return detailVO;
    }
}
