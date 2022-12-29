package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.mapper.DmpReturnOrderItemMapper;
import com.erp.server.bi.service.DmpReturnOrderItemService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 中台订单退货服务类
 */
@Service
public class DmpReturnOrderItemServiceImpl extends ServiceImpl<DmpReturnOrderItemMapper, DmpReturnOrderItemEntity>
    implements DmpReturnOrderItemService {


    @Override
    public BigDecimal sumReturnAmountBySKu(BiFilterDTO dto) {
        if (!BiFilterDTO.validOriginalCurrency(dto) && SettleMethodEnum.ORIGINAL_CURRENCY.equals(dto.getSettleMethod())) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = baseMapper.sumReturnAmountBySku(dto.getSku(), dto.getSettleMethod());
        return null == amount ? BigDecimal.ZERO : amount;
    }
}




