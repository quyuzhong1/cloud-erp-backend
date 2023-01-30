package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductChangeDetailsEntity;
import com.erp.server.plm.mapper.ProductChangeDetailsMapper;
import com.erp.server.plm.service.ProductChangeDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 变更管理变更实体的信息表(ProductChangeDetails)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Service
public class ProductChangeDetailsServiceImpl extends ServiceImpl<ProductChangeDetailsMapper, ProductChangeDetailsEntity> implements ProductChangeDetailsService {


    /**
     * 保存变更信息 表
     *
     * @param changeInfoId
     * @param detailsJson
     */
    @Override
    @Transactional
    public void saveChangeDetails(String changeInfoId, String detailsJson) {
        //先删除 有保存
        deleteByChangeInfoId(changeInfoId);
        ProductChangeDetailsEntity changeDetailsEntity = new ProductChangeDetailsEntity();
        changeDetailsEntity.setChangeInfoId(changeInfoId);
        changeDetailsEntity.setDetailsJson(detailsJson);
        this.save(changeDetailsEntity);
    }

    public void deleteByChangeInfoId(String changeInfoId) {
        LambdaQueryWrapper<ProductChangeDetailsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductChangeDetailsEntity::getChangeInfoId, changeInfoId);
        this.remove(queryWrapper);

    }

    @Override
    public String getDetailsJson(String changeId) {
        LambdaQueryWrapper<ProductChangeDetailsEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductChangeDetailsEntity::getChangeInfoId, changeId);
        queryWrapper.last("LIMIT 1");
        ProductChangeDetailsEntity entity = this.getOne(queryWrapper);
        if (entity != null) {
            return entity.getDetailsJson();
        }
        return "";
    }
}
