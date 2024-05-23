package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.DmpOrderItemSplitEntity;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.mapper.DmpOrderItemSplitMapper;
import com.erp.server.bi.service.DmpOrderItemSplitService;
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
public class DmpOrderItemSplitServiceImpl extends ServiceImpl<DmpOrderItemSplitMapper, DmpOrderItemSplitEntity>
    implements DmpOrderItemSplitService {


    @Override
    public BigDecimal sumSales(List<String> orderIds, BiFilterDTO dto) {
        QueryWrapper query = new QueryWrapper();

        if (SettleMethodEnum.ORIGINAL_CURRENCY.equals(dto.getSettleMethod())) {
            if (BiFilterDTO.validOriginalCurrency(dto)){
                query.select("SUM(amount_after) as sell_price");
            }else {
                return BigDecimal.ZERO;
            }
        }else if(SettleMethodEnum.CNY_SETTLE.equals(dto.getSettleMethod())){
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
        DmpOrderItemSplitEntity dmpOrderItemSplitEntity = baseMapper.selectOne(query);
        return dmpOrderItemSplitEntity == null ? BigDecimal.ZERO : dmpOrderItemSplitEntity.getSellPrice();
    }

    @Override
    public Integer countSalesVolume(List<String> orderIds, List<String> sku) {
        QueryWrapper query = new QueryWrapper();
        query.select("SUM(COALESCE(quantity, 0)) as quantity")
                .in(CollectionUtils.isNotEmpty(orderIds), "order_id", orderIds)
                .in(CollectionUtils.isNotEmpty(sku), "sku_no", sku);
        DmpOrderItemSplitEntity dmpOrderItemSplitEntity = baseMapper.selectOne(query);
        return dmpOrderItemSplitEntity == null ? 0 : dmpOrderItemSplitEntity.getQuantity();
    }

    @Override
    public Integer countOrderQuantityBySku(List<String> orderIds, List<String> sku) {
        Integer count = lambdaQuery()
                .in(CollectionUtils.isNotEmpty(orderIds), DmpOrderItemSplitEntity::getOrderId, orderIds)
                .in(CollectionUtils.isNotEmpty(sku), DmpOrderItemSplitEntity::getSkuNo, sku)
                .count();
        return count;
    }

    @Override
    public List<DmpOrderItemSplitEntity> listByOrderInfoIds(List<String> orderInfoIds) {
        if (CollectionUtils.isEmpty(orderInfoIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DmpOrderItemSplitEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DmpOrderItemSplitEntity::getOrderId,orderInfoIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<DmpOrderItemSplitEntity> listByConditions(List<String> orderIds, Integer newSign, List<String> sku) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return new ArrayList<>();
        }
        List<DmpOrderItemSplitEntity> list = lambdaQuery()
                .in(CollectionUtils.isNotEmpty(orderIds), DmpOrderItemSplitEntity::getOrderId, orderIds)
                .in(CollectionUtils.isNotEmpty(sku), DmpOrderItemSplitEntity::getSkuNo, sku)
                .eq(null != newSign, DmpOrderItemSplitEntity::getNewSign, newSign)
                .list();
        return list;
    }

}




