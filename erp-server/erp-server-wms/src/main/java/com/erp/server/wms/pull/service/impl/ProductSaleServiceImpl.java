package com.erp.server.wms.pull.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.wms.pull.mapper.ProductSaleMapper;
import com.erp.server.wms.pull.service.ProductSaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @Description 产品销售信息服务类
 * @Author zhangchunlin
 * @Date 2023-05-11 18:29
 **/
@Service
public class ProductSaleServiceImpl extends ServiceImpl<ProductSaleMapper, ProductSaleEntity> implements ProductSaleService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean saveOrUpdateProductSaleDetail(ProductSaleEntity productSaleEntity) {
        ProductSaleEntity entity = super.getById(productSaleEntity.getId());
        //不存在需要新增，同时判断产品名称是否存在了,存在不同步
        if (ObjectUtil.isNotEmpty(entity)) {
            if (!Objects.equals(entity.getUpdateTime(), productSaleEntity.getUpdateTime())) {
                return this.updateById(productSaleEntity);
            } else {
                // 数据没有发生变更
                return true;
            }
        } else {
            return this.save(productSaleEntity);
        }
    }

}




