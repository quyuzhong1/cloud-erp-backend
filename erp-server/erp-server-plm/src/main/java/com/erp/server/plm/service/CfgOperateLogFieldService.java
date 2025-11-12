package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.CfgOperateLogFieldEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/5 18:19
 */
public interface CfgOperateLogFieldService extends IService<CfgOperateLogFieldEntity> {
    /**
     * @description: 根据类路径集合查询
     * @author Will
     * @date: 2022/12/5 20:35
     * @param classPaths
     * @return List<CfgOperateLogFieldEntity>
     */
    List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths);
    /**
     * @description: 操作日志字段新增
     * @author Will
     * @date: 2022/12/7 15:11
     * @return Boolean
     */
    Boolean saveBatchSysLogField();
}
