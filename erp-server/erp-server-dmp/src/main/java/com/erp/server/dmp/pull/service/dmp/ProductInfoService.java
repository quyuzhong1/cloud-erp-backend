package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductInfoEntity;

public interface ProductInfoService extends IService<ProductInfoEntity> {
    /**
     * 更新PLM同步过来的数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    Boolean saveOrUpdateProductInfo(ProductInfoEntity productInfoEntity);
}
