package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.CfgOperateLogFieldEntity;
import com.erp.server.plm.mapper.CfgOperateLogFieldMapper;
import com.erp.server.plm.service.CfgOperateLogFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-12-02
 */
@Slf4j
@Service
public class CfgOperateLogFieldServiceImpl extends SuperServiceImpl<CfgOperateLogFieldMapper, CfgOperateLogFieldEntity> implements CfgOperateLogFieldService {


    @Resource
    private CfgOperateLogFieldServiceImpl cfgOperateLogFieldService;

    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        return Boolean.TRUE;
    }
}
