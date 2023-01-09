package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;

import java.math.BigDecimal;
import java.util.List;

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
    /**
     * @description: 根据退货单id查询
     * @author Will
     * @date: 2023/1/4 17:13
     * @param returnOrderId
     */
    List<DmpReturnOrderItemEntity> listByReturnOrderId(String returnOrderId);
}
