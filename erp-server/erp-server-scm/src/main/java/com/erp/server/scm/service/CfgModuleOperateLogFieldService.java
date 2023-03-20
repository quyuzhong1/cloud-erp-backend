package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.entity.CfgModuleOperateLogFieldEntity;

import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
 */
public interface CfgModuleOperateLogFieldService extends SuperService<CfgModuleOperateLogFieldEntity> {
    /**
     * @description: 根据类路径查询配置的字段
     * @author Will
     * @date: 2023/3/20 15:56
     * @param classPaths
     * @return List<CfgModuleOperateLogFieldEntity>
     */
    List<CfgModuleOperateLogFieldEntity> listByClassPaths(List<String> classPaths);
}
