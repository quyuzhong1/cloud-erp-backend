package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.BillShopInventoryDetailEntity;
import com.erp.server.mrp.mapper.BillShopInventoryDetailMapper;
import com.erp.server.mrp.service.BillShopInventoryDetailService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 店铺库存明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-31
 */
@Service
public class BillShopInventoryDetailServiceImpl extends SuperServiceImpl<BillShopInventoryDetailMapper, BillShopInventoryDetailEntity> implements BillShopInventoryDetailService {

    @Override
    public Map<String, Integer> listByMainIdsAndShopId(List<String> ids, String shopId) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<BillShopInventoryDetailEntity> list = list(Wrappers.<BillShopInventoryDetailEntity>lambdaQuery()
                .in(BillShopInventoryDetailEntity::getMainId, ids)
                .eq(BillShopInventoryDetailEntity::getShopId, shopId)
        );
        return list.stream()
                .collect(Collectors.toMap(BillShopInventoryDetailEntity::getMainId, BillShopInventoryDetailEntity::getQty, (o1,o2) -> o1));
    }

    @Override
    public int totalByMainIdsAndShopId(List<String> ids, String shopId) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        List<BillShopInventoryDetailEntity> list = list(Wrappers.<BillShopInventoryDetailEntity>lambdaQuery()
                .in(BillShopInventoryDetailEntity::getMainId, ids)
                .eq(BillShopInventoryDetailEntity::getShopId, shopId)
        );
        return list.stream()
                .map(BillShopInventoryDetailEntity::getQty)
                .reduce(0, Math::addExact);
    }
}
