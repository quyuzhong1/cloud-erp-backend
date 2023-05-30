package com.erp.server.wms.pull.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.wms.pull.mapper.ProductSaleMapper;
import com.erp.server.wms.pull.service.ProductSaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @Description 产品销售信息服务类
 * @Author zhangchunlin
 * @Date 2023-05-11 18:29
 **/
@Slf4j
@Service
public class ProductSaleServiceImpl extends ServiceImpl<ProductSaleMapper, ProductSaleEntity> implements ProductSaleService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateProductSaleDetail(List<ProductSaleEntity> productSaleEntities) {
        for (ProductSaleEntity productSaleEntity : productSaleEntities) {
            ProductSaleEntity entity = this.baseMapper.selectById(productSaleEntity.getId());
            //不存在需要新增，同时判断产品名称是否存在了,存在不同步
            if (ObjectUtil.isNotEmpty(entity)) {
                if (!Objects.equals(entity.getUpdateTime(), productSaleEntity.getUpdateTime())) {
                    try {
                        this.baseMapper.updateAllById(productSaleEntity);
                    }  catch (Exception e) {
                        log.error("产品销售信息SKU ID【{}】更新异常", productSaleEntity.getSkuId(), e);
                    }
                } else {
                    // 数据没有发生变更
                }
            } else {
                try {
                    this.save(productSaleEntity);
                }  catch (Exception e) {
                    log.error("产品销售信息SKU ID【{}】新增异常", productSaleEntity.getSkuId(), e);
                }
            }
        }
    }

}




