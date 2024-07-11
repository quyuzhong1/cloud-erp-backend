package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.vo.SkuCategoryVO;
import com.erp.model.dmp.entity.BiSkuInfoEntity;

import java.util.List;

/**
 * @Classname BiSkuInfoService

 * @Date 2022-12-26 16:34
 * @Created by yl
 */
public interface BiSkuInfoService  extends IService<BiSkuInfoEntity> {

    List<SkuCategoryVO> getSkuCategoryList();

    List<SkuCategoryVO> getSkuBrandList();

    List<SkuCategoryVO> getSkuPropertyList();

    /**
     * 根据sku查询商品信息
     *
     * @param skuNo     商品sku
     * @param companyId
     * @return com.erp.model.dmp.entity.DmpSkuInfoEntity
     **/
    BiSkuInfoEntity getBySkuNo(String skuNo, String companyId);
}
