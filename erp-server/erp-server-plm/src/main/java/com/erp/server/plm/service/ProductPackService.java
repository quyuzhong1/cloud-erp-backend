package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.model.plm.dto.ProductPackShowDTO;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.vo.ProductVO;

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
     * @param skuIds 产品sku明细表id
     * @return java.lang.Boolean
     **/
    Boolean removePack(List<String> skuIds);
    /**
     * @description: 根据skuId查询
     * @author Will
     * @date: 2023/1/12 18:15
     * @param skuId
     * @return ProductPackEntity
     */
    ProductPackEntity getBySkuId(String skuId);

    /**
     * 根据sku id 集合 获取到产品包装信息
     * @author yl
     * @date 2023-04-17 17:43
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.ProductVO.ProductPackVO>
     */
    List<ProductVO.ProductPackVO> getBySkuIds(List<String> skuIds);
}
