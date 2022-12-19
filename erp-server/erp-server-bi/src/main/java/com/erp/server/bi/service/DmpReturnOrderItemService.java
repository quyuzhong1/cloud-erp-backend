package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;

import java.math.BigDecimal;

/**
 * 中台订单退货服务类
 */
public interface DmpReturnOrderItemService extends IService<DmpReturnOrderItemEntity> {

    /**
     * 退货金额统计
     *
     * @param sku
     * @return
     */
    BigDecimal sumReturnAmountBySKu(BiFilterDTO sku);
}
