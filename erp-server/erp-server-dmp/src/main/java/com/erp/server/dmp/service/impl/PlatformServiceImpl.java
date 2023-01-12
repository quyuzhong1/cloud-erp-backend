package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.server.dmp.mapper.PlatformMapper;
import com.erp.server.dmp.service.PlatformService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 14:41
 */
@Service
public class PlatformServiceImpl extends ServiceImpl<PlatformMapper, PlatformEntity> implements PlatformService {

    @Override
    public PlatformEntity getByName(String name) {
        LambdaQueryWrapper<PlatformEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlatformEntity::getName,name);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }
}
