package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.SysLogEntity;
import com.erp.model.plm.entity.SysLogFieldEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 18:19
 */
public interface SysLogFieldService extends IService<SysLogFieldEntity> {
    /**
     * @description: 根据类路径集合查询
     * @author Will
     * @date: 2022/12/5 20:35
     * @param classPaths
     * @return List<SysLogFieldEntity>
     */
    List<SysLogFieldEntity> listByClassPaths(List<String> classPaths);
}
