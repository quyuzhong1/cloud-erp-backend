package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.mapper.BiReturnOrderItemMapper;
import com.erp.server.bi.service.BiReturnOrderItemService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 中台订单退货服务类
 */
@Service
public class BiReturnOrderItemServiceImpl extends ServiceImpl<BiReturnOrderItemMapper, BiReturnOrderItemEntity>
    implements BiReturnOrderItemService {


    @Override
    public BigDecimal sumReturnAmountBySKu(BiFilterDTO dto) {
        if (Boolean.TRUE.equals(!BiFilterDTO.validOriginalCurrency(dto)) && SettleMethodEnum.ORIGINAL_CURRENCY.getCode().equals(dto.getSettleMethod())) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = baseMapper.sumReturnAmountBySku(dto.getSku(), dto.getSettleMethod());
        return null == amount ? BigDecimal.ZERO : amount;
    }

    @Override
    public List<BiReturnOrderItemEntity> listByReturnOrderId(String returnOrderId) {
        LambdaQueryWrapper<BiReturnOrderItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiReturnOrderItemEntity::getReturnOrderId,returnOrderId);
        return this.list(queryWrapper);
    }
}




