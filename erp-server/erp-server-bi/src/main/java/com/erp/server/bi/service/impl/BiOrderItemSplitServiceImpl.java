package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.BiOrderItemSplitEntity;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.mapper.BiOrderItemSplitMapper;
import com.erp.server.bi.service.BiOrderItemSplitService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单商品详细信息
 * @author Cloud
 */
@Service
public class BiOrderItemSplitServiceImpl extends ServiceImpl<BiOrderItemSplitMapper, BiOrderItemSplitEntity>
    implements BiOrderItemSplitService {


    @Override
    public BigDecimal sumSales(List<String> orderIds, BiFilterDTO dto) {
        QueryWrapper<BiOrderItemSplitEntity> query = new QueryWrapper();

        if (SettleMethodEnum.ORIGINAL_CURRENCY.getCode().equals(dto.getSettleMethod())) {
            if (BiFilterDTO.validOriginalCurrency(dto)){
                query.select("SUM(amount_after) as sell_price");
            }else {
                return BigDecimal.ZERO;
            }
        }else if(SettleMethodEnum.CNY_SETTLE.getCode().equals(dto.getSettleMethod())){
            query.select("SUM(amount_after*cny_settle_rate) as sell_price");
        }else {
            query.select("SUM(amount_after*currency_rate) as sell_price");
        }
        Integer flag = null;
        if (null != dto.getHasNewSign() && dto.getHasNewSign()) {
            flag = 1;
        }
        query.in(CollectionUtils.isNotEmpty(orderIds), "order_id", orderIds)
            .in(CollectionUtils.isNotEmpty(dto.getSku()), "sku_no", dto.getSku())
            .eq(null != flag, "new_sign", flag);
        BiOrderItemSplitEntity biOrderItemSplitEntity = baseMapper.selectOne(query);
        return biOrderItemSplitEntity == null ? BigDecimal.ZERO : biOrderItemSplitEntity.getSellPrice();
    }

    @Override
    public Integer countSalesVolume(List<String> orderIds, List<String> sku) {
        QueryWrapper<BiOrderItemSplitEntity> query = new QueryWrapper();
        query.select("SUM(COALESCE(quantity, 0)) as quantity")
                .in(CollectionUtils.isNotEmpty(orderIds), "order_id", orderIds)
                .in(CollectionUtils.isNotEmpty(sku), "sku_no", sku);
        BiOrderItemSplitEntity biOrderItemSplitEntity = baseMapper.selectOne(query);
        return biOrderItemSplitEntity == null ? 0 : biOrderItemSplitEntity.getQuantity();
    }

    @Override
    public Integer countOrderQuantityBySku(List<String> orderIds, List<String> sku) {
        Integer count = lambdaQuery()
                .in(CollectionUtils.isNotEmpty(orderIds), BiOrderItemSplitEntity::getOrderId, orderIds)
                .in(CollectionUtils.isNotEmpty(sku), BiOrderItemSplitEntity::getSkuNo, sku)
                .count();
        return count;
    }

    @Override
    public List<BiOrderItemSplitEntity> listByOrderInfoIds(List<String> orderInfoIds) {
        if (CollectionUtils.isEmpty(orderInfoIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<BiOrderItemSplitEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BiOrderItemSplitEntity::getOrderId,orderInfoIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<BiOrderItemSplitEntity> listByConditions(List<String> orderIds, Integer newSign, List<String> sku) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return new ArrayList<>();
        }
        List<BiOrderItemSplitEntity> list = lambdaQuery()
                .in(CollectionUtils.isNotEmpty(orderIds), BiOrderItemSplitEntity::getOrderId, orderIds)
                .in(CollectionUtils.isNotEmpty(sku), BiOrderItemSplitEntity::getSkuNo, sku)
                .eq(null != newSign, BiOrderItemSplitEntity::getNewSign, newSign)
                .list();
        return list;
    }

}




