package com.erp.server.wms.pull.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductSaleEntity;

import java.util.List;

/**
 * @Description 产品销售信息服务类
 * @Author zhangchunlin
 * @Date 2023-05-11 18:22
 **/
public interface ProductSaleService extends IService<ProductSaleEntity> {

    /**
     * 更新PLM同步过来的产品销售数据
     * @Author zhangchunlin
     * @Date 2023-05-11 18:07
     **/
    void saveOrUpdateProductSaleDetail(List<ProductSaleEntity> productSaleEntity);

}
