package com.erp.server.bi.service;

import com.common.business.service.SuperService;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.vo.SkuCategoryVO;

import java.util.List;

/**
 * <p>
 * 产品sku表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-21
 */
public interface BiProductDetailService extends SuperService<BiProductDetailEntity> {

    /**
     * 获取分类下面的sku 信息
     * @author yl
     * @date 2023-04-21 10:20
     * @param
     * @return java.util.List<com.erp.model.bi.vo.SkuCategoryVO>
     */
    List<SkuCategoryVO> getSkuCategoryList();


    /**
     * 获取品牌下面的sku 信息
     * @author yl
     * @date 2023-04-21 10:20
     * @param
     * @return java.util.List<com.erp.model.bi.vo.SkuCategoryVO>
     */
    List<SkuCategoryVO> getSkuBrandList(List<String> brandList);

    /**
     * 根据属性 获取到对应的sku 信息
     * @author yl
     * @date 2023-04-21 10:40
     * @param
     * @return java.util.List<com.erp.model.bi.vo.SkuCategoryVO>
     */
    List<SkuCategoryVO> getSkuPropertyList();
}
