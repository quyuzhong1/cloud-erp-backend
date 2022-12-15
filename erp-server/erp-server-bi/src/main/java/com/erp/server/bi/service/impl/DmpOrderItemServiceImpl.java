package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.dto.TargetSaleDTO;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.server.bi.enums.TargetSettleMethodEnum;
import com.erp.server.bi.mapper.DmpOrderItemMapper;
import com.erp.server.bi.service.DmpOrderItemService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单商品详细信息
 * @author Cloud
 */
@Service
public class DmpOrderItemServiceImpl extends ServiceImpl<DmpOrderItemMapper, DmpOrderItemEntity>
    implements DmpOrderItemService {


    @Override
    public BigDecimal sumSales(List<String> orderIds, TargetSaleDTO dto) {
        QueryWrapper query = new QueryWrapper();
        // TODO 添加汇率

        if (TargetSettleMethodEnum.ORIGINAL_CURRENCY.equals(dto.getSettleMethod())) {
            query.select("SUM(sell_price*quantity) as sell_price");
        }else if(TargetSettleMethodEnum.CNY_SETTLE.equals(dto.getSettleMethod())){
            query.select("SUM(sell_price*quantity) as sell_price");
        }else if (TargetSaleDTO.validOriginalCurrency(dto)){
            query.select("SUM(sell_price*quantity) as sell_price");
        }

        query.in(CollectionUtils.isNotEmpty(orderIds), "order_id", orderIds)
            .in(CollectionUtils.isNotEmpty(dto.getSku()), "sku_no", dto.getSku())
            .eq(ObjectUtils.isNotEmpty(dto.getHasNewSign()), "new_sign", dto.getHasNewSign() ? 1 : 0);
        DmpOrderItemEntity dmpOrderItemEntity = baseMapper.selectOne(query);
        return dmpOrderItemEntity.getSellPrice();
    }

    @Override
    public Integer countSalesVolume(List<String> orderIds, List<String> sku) {
        QueryWrapper query = new QueryWrapper();
        query.select("SUM(NULLIF(quantity, 0)) as quantity")
                .in(CollectionUtils.isNotEmpty(orderIds), "order_id", orderIds)
                .in(CollectionUtils.isNotEmpty(sku), "sku_no", sku);
        DmpOrderItemEntity dmpOrderItemEntity = baseMapper.selectOne(query);
        return dmpOrderItemEntity.getQuantity();
    }

    @Override
    public Integer countOrderQuantityBySku(List<String> orderIds, List<String> sku) {
        QueryWrapper<DmpOrderItemEntity> query = new QueryWrapper();
        query.select("order_id")
                .in(CollectionUtils.isNotEmpty(orderIds), "order_id", orderIds)
                .in(CollectionUtils.isNotEmpty(sku), "sku_no", sku);
        List<DmpOrderItemEntity> list = baseMapper.selectList(query);
        if(CollectionUtils.isNotEmpty(list)){
            return 0;
        }
        long count = list.stream().map(DmpOrderItemEntity::getOrderId).distinct().count();
        return new Long(count).intValue();
    }

}




