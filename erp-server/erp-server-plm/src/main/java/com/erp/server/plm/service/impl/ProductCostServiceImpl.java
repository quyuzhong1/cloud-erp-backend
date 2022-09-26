package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.server.plm.mapper.ProductCostMapper;
import com.erp.server.plm.service.ProductCostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 产品成本信息服务类
 * @Author Luo_WG
 * @Date 2022/9/22 16:15
 **/
@Service
public class ProductCostServiceImpl extends ServiceImpl<ProductCostMapper, ProductCostEntity> implements ProductCostService {

    @Resource
    private ProductCostMapper productCostMapper;

    /**
     * @Description 产品成本信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    @Override
    public List<ProductCostShowDTO> list(String productId) {
        return productCostMapper.list(productId);
    }

    /**
     * @Description 保存/修改产品成本信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productCostDTO 产品成本信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductCostDTO productCostDTO) {
        ProductCostEntity costEntity = new ProductCostEntity();
        BeanMapper.copy(productCostDTO, costEntity);
        return this.saveOrUpdate(costEntity);
    }
}




