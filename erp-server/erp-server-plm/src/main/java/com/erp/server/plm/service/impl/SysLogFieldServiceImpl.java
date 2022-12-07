package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.SysLogEntity;
import com.erp.model.plm.entity.SysLogFieldEntity;
import com.erp.server.plm.mapper.SysLogFieldMapper;
import com.erp.server.plm.mapper.SysLogMapper;
import com.erp.server.plm.service.SysLogFieldService;
import com.erp.server.plm.service.SysLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 18:18
 */
@Service
public class SysLogFieldServiceImpl extends ServiceImpl<SysLogFieldMapper, SysLogFieldEntity> implements SysLogFieldService {

    @Override
    public List<SysLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<SysLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }
}
