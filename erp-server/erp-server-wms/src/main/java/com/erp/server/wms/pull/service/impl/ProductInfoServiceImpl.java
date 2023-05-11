package com.erp.server.wms.pull.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.wms.pull.mapper.ProductInfoMapper;
import com.erp.server.wms.pull.service.ProductInfoService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @CreateTime: 2023-05-11  19:19
 * @Author: zhangchunlin
 */
@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfoEntity> implements ProductInfoService {

    @Override
    public Boolean saveOrUpdateProductInfo(ProductInfoEntity productInfoEntity) {
        ProductInfoEntity entity = super.getById(productInfoEntity.getId());
        //不存在需要新增，同时判断产品是否更新，用最后更新时间
        if (ObjectUtil.isNotEmpty(entity)) {
            if (!Objects.equals(entity.getUpdateTime(), productInfoEntity.getUpdateTime())) {
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