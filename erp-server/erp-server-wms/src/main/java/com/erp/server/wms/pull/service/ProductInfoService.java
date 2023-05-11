package com.erp.server.wms.pull.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductInfoEntity;

public interface ProductInfoService extends IService<ProductInfoEntity> {
    /**
     * 更新PLM同步过来的数据
     * @Author zhangchunlin
     * @Date 2023-05-11 19:18
     **/
    Boolean saveOrUpdateProductInfo(ProductInfoEntity productInfoEntity);
}
