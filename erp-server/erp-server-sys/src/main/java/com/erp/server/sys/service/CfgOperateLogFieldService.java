package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.entity.CfgOperateLogFieldEntity;

import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务类
 * </p>
 *
 * @author will
 * @since 2025-05-12
 */
public interface CfgOperateLogFieldService extends SuperService<CfgOperateLogFieldEntity> {
    /**
     * @description: 根据类路径查询配置的字段
     * @author Will
     * @date: 2025/5/12 15:56
     * @param classPaths
     * @return List<CfgModuleOperateLogFieldEntity>
     */
    List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths);

    /**
     * @description: 新增配置信息
     * @author Will
     * @date: 2025/5/12 10:50
     * @return Boolean
     */
    Boolean saveBatchSysLogField();
}
