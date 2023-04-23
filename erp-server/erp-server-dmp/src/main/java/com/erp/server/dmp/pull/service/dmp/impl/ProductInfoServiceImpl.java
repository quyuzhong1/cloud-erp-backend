package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.dmp.pull.mapper.ProductInfoMapper;
import com.erp.server.dmp.pull.service.dmp.ProductInfoService;
import org.springframework.stereotype.Service;

@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfoEntity> implements ProductInfoService {

    /**
     * 根据主键Id查询产品表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param id
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    public ProductInfoEntity getProductInfoById(String id) {
        return baseMapper.getProductInfoById(id);
    }

    /**
     * 更新PLM同步过来的数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    public Boolean saveOrUpdateProductInfo(ProductInfoEntity productInfoEntity) {
        ProductInfoEntity entity = getProductInfoById(productInfoEntity.getId());
        //不存在需要新增，同时判断产品名称是否存在了,存在不同步
        if (ObjectUtil.isEmpty(entity)) {
            if (entity.getName().equals(productInfoEntity.getName())) {
                return false;
            }
        }
        return this.saveOrUpdate(productInfoEntity);
    }
}
