package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductImagesDTO;
import com.erp.model.plm.entity.ProductImagesEntity;

/**
 * @Description 产品图片服务类
 * @Author Luo_WG
 * @Date 2022/9/26 16:30
 **/
public interface ProductImagesService extends IService<ProductImagesEntity> {

    /**
     * @Description 新增产品图片
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productImagesDTO 产品图片信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean insertProductImage(ProductImagesDTO productImagesDTO);

    /**
     * @Description 修改产品图片
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productImagesDTO 产品图片信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean updateProductImage(ProductImagesDTO productImagesDTO);
}
