package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.mapper.DmpReturnOrderItemMapper;
import com.erp.server.bi.service.DmpReturnOrderItemService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 中台订单退货服务类
 */
@Service
public class DmpReturnOrderItemServiceImpl extends ServiceImpl<DmpReturnOrderItemMapper, DmpReturnOrderItemEntity>
    implements DmpReturnOrderItemService {


    @Override
    public BigDecimal sumReturnAmountBySKu(List<String> returnOrderIds, BiFilterDTO dto) {
        QueryWrapper query = new QueryWrapper();
        if (SettleMethodEnum.ORIGINAL_CURRENCY.equals(dto.getSettleMethod())) {
            query.select("SUM(sell_price*quantity) as sell_price");
        }else if(SettleMethodEnum.CNY_SETTLE.equals(dto.getSettleMethod())){
            query.select("SUM(sell_price*quantity) as sell_price");
        }else if (BiFilterDTO.validOriginalCurrency(dto)){
            query.select("SUM(sell_price*quantity) as sell_price");
        }
        query
            .in(CollectionUtils.isNotEmpty(returnOrderIds), "return_order_id", returnOrderIds)
            .in(CollectionUtils.isNotEmpty(dto.getSku()), "sku_no", dto.getSku());
        DmpReturnOrderItemEntity dmpOrderItemEntity = baseMapper.selectOne(query);
        return dmpOrderItemEntity.getSellPrice();
    }
}




