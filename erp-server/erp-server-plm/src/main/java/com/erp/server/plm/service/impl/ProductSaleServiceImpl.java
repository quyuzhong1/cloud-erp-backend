package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductSaleDTO;
import com.erp.model.plm.dto.ProductSaleShowDTO;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.plm.mapper.ProductSaleMapper;
import com.erp.server.plm.service.ProductSaleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 产品销售信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 14:09
 **/
@Service
public class ProductSaleServiceImpl extends ServiceImpl<ProductSaleMapper, ProductSaleEntity> implements ProductSaleService {

    @Resource
    private ProductSaleMapper productSaleMapper;

    /**
     * @Description 产品销售信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductSaleShowDTO>
     **/
    @Override
    public List<ProductSaleShowDTO> list(String productId){
        return productSaleMapper.list(productId);
    }

    /**
     * @Description 保存/修改产品销售信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productSaleDTO 产品销售信息表请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductSaleDTO productSaleDTO){
        ProductSaleEntity saleEntity = new ProductSaleEntity();
        BeanMapper.copy(productSaleDTO, saleEntity);
        return this.saveOrUpdate(saleEntity);
    }

}




