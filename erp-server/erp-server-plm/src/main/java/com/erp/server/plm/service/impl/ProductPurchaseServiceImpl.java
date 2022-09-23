package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductPurchaseDTO;
import com.erp.model.plm.dto.ProductPurchaseShowDTO;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.server.plm.mapper.ProductPurchaseMapper;
import com.erp.server.plm.service.ProductPurchaseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 产品采购信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 11:23
 **/
@Service
public class ProductPurchaseServiceImpl extends ServiceImpl<ProductPurchaseMapper, ProductPurchaseEntity> implements ProductPurchaseService {

    @Resource
    private ProductPurchaseMapper productPurchaseMapper;

    /**
     * @Description 产品采购信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 11:48
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductPurchaseShowDTO>
     **/
    @Override
    public List<ProductPurchaseShowDTO> list(String productId) {
        return productPurchaseMapper.list(productId);
    }

    /**
    * @Description 保存/修改产品采购信息
    * @Author Luo_WG
    * @Date 2022/9/23 11:48
    * @param productPurchaseDTO 产品采购信息表请求参数
    * @return java.lang.Boolean 
    **/
    @Override
    public Boolean saveOrUpdate(ProductPurchaseDTO productPurchaseDTO) {
        ProductPurchaseEntity purchaseEntity = new ProductPurchaseEntity();
        BeanMapper.copy(productPurchaseDTO, purchaseEntity);
        return this.saveOrUpdate(purchaseEntity);
    }
}




