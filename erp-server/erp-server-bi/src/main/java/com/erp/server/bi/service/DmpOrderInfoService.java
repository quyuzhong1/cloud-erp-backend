package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.IndicatorSaleDTO;
import com.erp.model.bi.vo.IndicatorSaleSumVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;

/**
 * 订单服务类
 */
public interface DmpOrderInfoService extends IService<DmpOrderInfoEntity> {

    /**
     * 统计销售金额
     * @param dto
     * @return
     */
    IndicatorSaleSumVO countSales(IndicatorSaleDTO dto);
}
