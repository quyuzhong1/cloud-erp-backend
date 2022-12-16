package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.SalesVO;
import com.erp.model.bi.vo.StatisticalDataVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;

import java.util.List;

/**
 * @Classname SalesOrderService
 * @Description TODO
 * @Date 2022-12-16 11:08
 * @Created by yl
 */
public interface SalesOrderService extends IService<DmpOrderInfoEntity> {

    /**
     * 月度趋势
     * @return
     */
    StatisticalDataVO getMonthSales();

    /**
     * 一级模块 sku 销售额
     * @param dto
     * @return
     */
    List<SalesVO> getBySku(BiFilterDTO dto);
}
