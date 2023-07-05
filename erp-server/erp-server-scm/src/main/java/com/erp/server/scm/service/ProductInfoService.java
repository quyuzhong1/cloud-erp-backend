package com.erp.server.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductInfoEntity;

import java.util.List;

public interface ProductInfoService extends IService<ProductInfoEntity> {
    /**
     * 根据主键Id查询产品表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param ids
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    List<ProductInfoEntity> ListProductInfoByIds(List<String> ids);

    /**
     * 更新PLM同步过来的数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    Boolean saveOrUpdateProductInfo(List<ProductInfoEntity> productInfoEntity);
}
