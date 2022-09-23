package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.dto.ProductPurchaseDTO;
import com.erp.model.plm.dto.ProductPurchaseShowDTO;
import com.erp.model.plm.entity.ProductPurchaseEntity;

import java.util.List;
/**
* @Description 产品采购信息服务类
* @Author Luo_WG
* @Date 2022/9/23 11:23
**/
public interface ProductPurchaseService extends IService<ProductPurchaseEntity> {

    /**
    * @Description 产品采购信息查询列表
    * @Author Luo_WG
    * @Date 2022/9/23 11:25
    * @param productId:产品信息表id
    * @return java.util.List<com.erp.model.plm.dto.ProductPurchaseShowDTO>
    **/
    List<ProductPurchaseShowDTO> list(String productId);

    /**
    * @Description 保存/修改产品采购信息
    * @Author Luo_WG
    * @Date 2022/9/23 11:36
    * @param productCostDTO:产品采购信息表
    * @return java.lang.Boolean
    **/
    Boolean saveOrUpdate(ProductPurchaseDTO productCostDTO);
}
