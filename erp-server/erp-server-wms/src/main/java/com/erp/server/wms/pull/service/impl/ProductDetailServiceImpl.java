package com.erp.server.wms.pull.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.wms.pull.mapper.ProductDetailMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProductDetailServiceImpl extends ServiceImpl<ProductDetailMapper, ProductDetailEntity> implements ProductDetailService {

    /**
     * 更新PLM同步过来的数据
     * @Author zhangchunlin
     * @Date 2023-05-11 18:10
     **/
    public Boolean saveOrUpdateProductDetail(ProductDetailEntity productDetailEntity) {
        ProductDetailEntity entity = super.getById(productDetailEntity.getId());
        //不存在需要新增，同时判断产品是否更新，用最后更新时间
        if (ObjectUtil.isNotEmpty(entity)) {
            if (!Objects.equals(entity.getUpdateTime(), productDetailEntity.getUpdateTime())) {
                return this.updateById(entity);
            } else {
                // 数据没有发生变更
                return true;
            }
        } else {
            return this.save(entity);
        }
    }
}
