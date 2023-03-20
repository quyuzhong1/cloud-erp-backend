package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.scm.entity.CfgModuleOperateLogFieldEntity;
import com.erp.server.scm.mapper.CfgModuleOperateLogFieldMapper;
import com.erp.server.scm.service.CfgModuleOperateLogFieldService;
import com.common.core.serveice.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
 */
@Service
public class CfgModuleOperateLogFieldServiceImpl extends SuperServiceImpl<CfgModuleOperateLogFieldMapper, CfgModuleOperateLogFieldEntity> implements CfgModuleOperateLogFieldService {

    @Override
    public List<CfgModuleOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgModuleOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgModuleOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }
}
