package com.erp.server.bi.service;

import com.common.business.service.SuperService;
import com.erp.model.bi.dto.SkuSalesDTO;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.vo.SkuCategoryVO;
import com.erp.model.bi.vo.SkuDetailVO;

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
    List<SkuCategoryVO> getSkuCategoryList(List<String> categoryIdList);


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

    /**
     * 根据sku 获取详情信息
     * @param skuNo
     * @return
     */
    BiProductDetailEntity getBySkuNo(String skuNo);

    /**
     * 根据sku no list 获取信息
     * @param skuNoList
     * @return
     */
    List<BiProductDetailEntity> listBySkuNoList(List<String> skuNoList);

    /**
     * 通过编码获取skuId集合
     * @param skuNos
     * @return
     */
    List<SkuDetailVO> getSkuIdBySkuNo(List<String> skuNos);

    /**
     * 根据sku id 获取到对应数据
     * @param skuIdList
     * @return
     */
    List<SkuSalesDTO.ProductSkuDTO> listProductSkuBySkuIdList(List<String> skuIdList);
}
