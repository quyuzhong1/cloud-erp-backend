package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 中台订单退货服务类
 */
public interface BiReturnOrderItemService extends IService<BiReturnOrderItemEntity> {

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
    List<BiReturnOrderItemEntity> listByReturnOrderId(String returnOrderId);
}
