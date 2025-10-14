package com.erp.server.fms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.fms.entity.CfgOperateLogFieldEntity;
import com.erp.server.fms.mapper.CfgOperateLogFieldMapper;
import com.erp.server.fms.service.CfgOperateLogFieldService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 操作日志字段配置表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2023-03-17
 */
@Service
public class CfgOperateLogFieldServiceImpl extends SuperServiceImpl<CfgOperateLogFieldMapper, CfgOperateLogFieldEntity> implements CfgOperateLogFieldService {

    @Lazy
    @Resource
    private CfgOperateLogFieldService service;
    
    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        // 用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        // 这里可以添加财务系统相关的字段配置
        // 示例：为财务实体类添加字段配置
        return Boolean.TRUE;
    }
}
