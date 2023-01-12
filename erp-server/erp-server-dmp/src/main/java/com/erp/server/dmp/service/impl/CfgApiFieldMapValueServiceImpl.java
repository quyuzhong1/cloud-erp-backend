package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.server.dmp.mapper.CfgApiFieldMapValueMapper;
import com.erp.server.dmp.service.CfgApiFieldMapValueService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
@Service
public class CfgApiFieldMapValueServiceImpl extends ServiceImpl<CfgApiFieldMapValueMapper, CfgApiFieldMapValueEntity> implements CfgApiFieldMapValueService {


    @Override
    public void removeByFieldMapIds(List<String> ids) {
        LambdaUpdateWrapper<CfgApiFieldMapValueEntity>  updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CfgApiFieldMapValueEntity::getFieldMapId,ids);
        this.remove(updateWrapper);
    }

    @Override
    public List<CfgApiFieldMapValueEntity> listByFieldMapId(String fieldMapId) {
        LambdaQueryWrapper<CfgApiFieldMapValueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CfgApiFieldMapValueEntity::getFieldMapId,fieldMapId);
        queryWrapper.orderByAsc(CfgApiFieldMapValueEntity::getId);
        return this.list(queryWrapper);
    }

    @Override
    public List<CfgApiFieldMapValueEntity> listByFieldMapIds(List<String> fieldMapIds) {
        LambdaQueryWrapper<CfgApiFieldMapValueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgApiFieldMapValueEntity::getFieldMapId,fieldMapIds);
        queryWrapper.orderByAsc(CfgApiFieldMapValueEntity::getId);
        return this.list(queryWrapper);
    }
}
