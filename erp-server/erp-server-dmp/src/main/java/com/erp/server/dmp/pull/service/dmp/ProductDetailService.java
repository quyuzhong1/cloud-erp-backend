package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductDetailEntity;

public interface ProductDetailService extends IService<ProductDetailEntity> {
    /**
     * 根据主键Id查询产品Sku表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param id
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    ProductDetailEntity getProductDetailById(String id);

    /**
     * 更新PLM同步过来的数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    Boolean saveOrUpdateProductDetail(ProductDetailEntity productDetailEntity);
}
