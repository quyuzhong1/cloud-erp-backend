package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.ProductDetailEntity;


public interface ProductDetailImagesService extends IService<ProductDetailEntity> {
    /**
     * @deprecated 上传SKU图片（主页）
     * @Author jack
     * @Date 2025-07-25
     **/
    Boolean uploadProductImage(ProductDetailDTO.ProductImagesDTO dto);
    /**
     *  异步导入(SKU图片（主页）)
     * @author jack
     * @Date 2025-07-25
     */
    Boolean importZip(BaseDTO.ImportDTO dto);

    void importProductDetailImages(BaseDTO.ImportDTO dto);
}
