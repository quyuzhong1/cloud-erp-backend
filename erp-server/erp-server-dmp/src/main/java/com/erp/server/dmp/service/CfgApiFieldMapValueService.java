package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
public interface CfgApiFieldMapValueService extends IService<CfgApiFieldMapValueEntity> {
    /**
     * @description: 根据字段映射表ids删除
     * @author Will
     * @date: 2023/1/11 15:13
     * @param ids
     */
    void removeByFieldMapIds(List<String> ids);
    /**
     * @description: 根据字段映射id查询
     * @author Will
     * @date: 2023/1/11 15:16
     * @param fieldMapId
     * @return List<CfgApiFieldMapValueEntity>
     */
    List<CfgApiFieldMapValueEntity> listByFieldMapId(String fieldMapId);

    /**
     * @description: 根据字段映射ids查询
     * @author Will
     * @date: 2023/1/11 15:16
     * @param fieldMapIds
     * @return List<CfgApiFieldMapValueEntity>
     */
    List<CfgApiFieldMapValueEntity> listByFieldMapIds(List<String> fieldMapIds);
}
