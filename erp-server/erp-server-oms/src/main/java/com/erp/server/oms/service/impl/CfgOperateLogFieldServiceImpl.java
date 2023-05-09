package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.CfgOperateLogFieldEntity;
import com.erp.server.oms.mapper.CfgOperateLogFieldMapper;
import com.erp.server.oms.service.CfgOperateLogFieldService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Service
public class CfgOperateLogFieldServiceImpl extends SuperServiceImpl<CfgOperateLogFieldMapper, CfgOperateLogFieldEntity> implements CfgOperateLogFieldService {


    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        return null;
    }
}
