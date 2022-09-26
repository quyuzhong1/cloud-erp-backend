package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.model.plm.dto.ProductPackShowDTO;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.server.plm.mapper.ProductPackMapper;
import com.erp.server.plm.service.ProductPackService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 产品包装信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 15:35
 **/
@Service
public class ProductPackServiceImpl extends ServiceImpl<ProductPackMapper, ProductPackEntity>
    implements ProductPackService {

    @Resource
    private ProductPackMapper productPackMapper;

    /**
     * @Description 产品包装信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     **/
    @Override
    public List<ProductPackShowDTO> list(String productId) {
        return productPackMapper.list(productId);
    }

    /**
     * @Description 保存/修改产品包装信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productPackDTO 产品包装信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductPackDTO productPackDTO) {
        ProductPackEntity packEntity = new ProductPackEntity();
        BeanMapper.copy(productPackDTO, packEntity);
        return this.saveOrUpdate(packEntity);
    }
}




