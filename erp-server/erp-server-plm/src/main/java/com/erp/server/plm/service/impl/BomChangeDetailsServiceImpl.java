package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.BomChangeDetailsEntity;
import com.erp.server.plm.mapper.BomChangeDetailsMapper;
import com.erp.server.plm.service.BomChangeDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 变更管理变更实体的信息表(ProductChangeDetails)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Service
public class BomChangeDetailsServiceImpl extends ServiceImpl<BomChangeDetailsMapper, BomChangeDetailsEntity> implements BomChangeDetailsService {


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
        BomChangeDetailsEntity changeDetailsEntity = new BomChangeDetailsEntity();
        changeDetailsEntity.setChangeInfoId(changeInfoId);
        changeDetailsEntity.setDetailsJson(detailsJson);
        this.save(changeDetailsEntity);
    }

    public void deleteByChangeInfoId(String changeInfoId) {
        LambdaQueryWrapper<BomChangeDetailsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BomChangeDetailsEntity::getChangeInfoId, changeInfoId);
        this.remove(queryWrapper);

    }

    @Override
    public String getDetailsJson(String changeId) {
        LambdaQueryWrapper<BomChangeDetailsEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(BomChangeDetailsEntity::getChangeInfoId, changeId);
        queryWrapper.last("LIMIT 1");
        BomChangeDetailsEntity entity = this.getOne(queryWrapper);
        if (entity != null) {
            return entity.getDetailsJson();
        }
        return "";
    }
}
