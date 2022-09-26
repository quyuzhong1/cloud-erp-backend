package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.model.plm.dto.ProductPackShowDTO;
import com.erp.model.plm.entity.ProductPackEntity;

import java.util.List;

/**
 * @Description 产品包装信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 15:35
 **/
public interface ProductPackService extends IService<ProductPackEntity> {
    /**
     * @Description 产品包装信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     **/
    List<ProductPackShowDTO> list(String productId);

    /**
     * @Description 保存/修改产品包装信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productPackDTO 产品包装信息表
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdate(ProductPackDTO productPackDTO);

    /**
     * @Description 保存/修改产品包装信息-批量操作
     * @Author Luo_WG
     * @Date 2022/9/26 18:16
     * @param productPackList 产品包装信息表
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductPackDTO> productPackList);

    /**
     * @Description 删除产品包装信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     * @param skuId 产品sku明细表id
     * @return java.lang.Boolean
     **/
    Boolean remove(String skuId);
}
