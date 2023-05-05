package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.CfgOperateLogFieldEntity;

import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
 */
public interface CfgOperateLogFieldService extends SuperService<CfgOperateLogFieldEntity> {
    /**
     * @description: 根据类路径查询配置的字段
     * @author Will
     * @date: 2023/3/20 15:56
     * @param classPaths
     * @return List<CfgModuleOperateLogFieldEntity>
     */
    List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths);

    /**
     * @description: 新增配置信息
     * @author Will
     * @date: 2023/3/22 10:50
     * @return Boolean
     */
     Boolean saveBatchSysLogField();
}
