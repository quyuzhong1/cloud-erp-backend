package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductLogisticsDTO;
import com.erp.model.plm.dto.ProductLogisticsShowDTO;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.server.plm.mapper.ProductLogisticsMapper;
import com.erp.server.plm.service.ProductLogisticsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 产品物流信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 15:35
 **/
@Service
public class ProductLogisticsServiceImpl extends ServiceImpl<ProductLogisticsMapper, ProductLogisticsEntity>
    implements ProductLogisticsService {

    @Resource
    private ProductLogisticsMapper productLogisticsMapper;

    /**
     * @Description 产品物流信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductLogisticsShowDTO>
     **/
    @Override
    public List<ProductLogisticsShowDTO> list(String productId) {
        return productLogisticsMapper.list(productId);
    }

    /**
     * @Description 保存/修改产品物流信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productLogisticsDTO 产品物流信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductLogisticsDTO productLogisticsDTO) {
        ProductLogisticsEntity logisticsEntity = new ProductLogisticsEntity();
        BeanMapper.copy(productLogisticsDTO, logisticsEntity);
        return this.saveOrUpdate(logisticsEntity);
    }
}




