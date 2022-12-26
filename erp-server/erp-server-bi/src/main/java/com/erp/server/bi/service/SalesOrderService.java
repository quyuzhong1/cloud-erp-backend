package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.*;
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


    /**
     * 一级模块 spu 销售额
     * @param dto
     * @return
     */
    List<SalesVO> getBySpu(BiFilterDTO dto);

    List<SalesByCountryVO> getByCountry(BiFilterDTO dto);

    /**
     * 一级销售模块更具平台分
     * @param dto
     * @return
     */
    StatisticalDataVO getByPlatform(BiFilterDTO dto);

    /**
     * 一级销售模块 店铺销售额
     * @author yl
     * @date 2022-12-21 10:33
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.ShopSalesVO>
     */
    List<ShopSalesVO> getByShop(BiFilterDTO dto);


    /**
     * 一级销售模块 销售额TOP20店铺
     * @author yl
     * @date 2022-12-21 10:33
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.ShopSalesVO>
     */
    StatisticalDataVO byTopShop(BiFilterDTO dto);

    /**
     * 二级销售模块 店铺-国家销售额
     * @author yl
     * @date 2022-12-26 10:10
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesGroupVO>
     */
    List<SalesGroupVO> byShopCountry(BiFilterDTO dto);

    /**
     * 二级销售模块 店铺的新/老品销售额
     * @author yl
     * @date 2022-12-26 10:10
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesGroupVO>
     */
    List<ShopNewAndOldSalesVO> byShopNewAndOld(BiFilterDTO dto);

    /**
     * 一级模块  国家销售额
     * @param dto
     * @return
     */
    List<SalesCountVO> byCountry(BiFilterDTO dto);

    /**
     * 一级模块  品类销售额
     * @param dto
     * @return
     */
    StatisticalDataVO byCategory(BiFilterDTO dto);

    List<SalesGroupVO> byShopCategory(BiFilterDTO dto);
}
