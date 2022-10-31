package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductPurchaseRemarkDTO;
import com.erp.model.plm.dto.ProductPurchaseRemarkShowDTO;
import com.erp.model.plm.dto.ProductSaleDTO;
import com.erp.model.plm.dto.ProductSaleShowDTO;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;

import java.util.List;

/**
 * @Description 产品采购备注信息服务类
 * @Author Luo_WG
 * @Date 2022/9/26 10:40
 * @param
 * @return
 **/
public interface ProductPurchaseRemarkService extends IService<ProductPurchaseRemarkEntity> {
    /**
     * @Description 产品采购备注信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.entity.ProductPurchaseRemarkEntity>
     **/
    List<ProductPurchaseRemarkEntity> list(String productId);

    /**
     * @Description 保存/修改产品采购备注信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param dto 产品采购备注信息表请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdate(ProductPurchaseRemarkDTO dto);

    /**
     * @Description 保存/修改产品采购备注信息-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param dto 产品采购备注信息表请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductPurchaseRemarkDTO> dto);
}
