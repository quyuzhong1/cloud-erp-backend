package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.dmp.mapper.ProductDetailMapper;
import com.erp.server.dmp.pull.service.dmp.ProductDetailService;
import org.springframework.stereotype.Service;

@Service
public class ProductDetailServiceImpl extends ServiceImpl<ProductDetailMapper, ProductDetailEntity> implements ProductDetailService {

    /**
     * 根据主键Id查询产品Sku表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param id
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    @Override
    public ProductDetailEntity getProductDetailById(String id) {
        return baseMapper.getProductDetailById(id);
    }

    /**
     * 更新PLM同步过来的数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    public Boolean saveOrUpdateProductDetail(ProductDetailEntity productDetailEntity) {
        ProductDetailEntity entity = getProductDetailById(productDetailEntity.getId());
        //不存在需要新增，同时判断产品名称是否存在了,存在不同步
        if (ObjectUtil.isEmpty(entity)) {
            if (entity.getSkuNo().equals(productDetailEntity.getSkuNo())) {
                return false;
            }
        }
        return this.saveOrUpdate(productDetailEntity);
    }
}
