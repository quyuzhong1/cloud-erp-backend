package com.erp.server.bi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.ModuleSysConfigurationDTO;
import com.erp.model.bi.dto.ModuleSysDTO;
import com.erp.model.bi.entity.BiSysModuleEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统模块表(BiSysModule)表服务接口
 *
 * @author yl
 * @since 2022-12-12 16:54:14
 */
public interface BiSysModuleService  extends IService<BiSysModuleEntity> {


    Boolean insert(ModuleSysDTO dto);

    Boolean updateSysModule(ModuleSysDTO dto);

    List<Map<String, Object>> getSysModuleList(Integer isAdd);

    List<Map<String, Object>> getPid(String pid);

    void updateAddState(String id,Integer isAddFlag);
    /**
     * 模块配置
     */
    Boolean moduleConfiguration(ModuleSysConfigurationDTO dto);
    /**
     * @description: 根据名称查询
     * @author Will
     * @date: 2022/12/28 10:05
     * @param name
     * @return BiSysModuleEntity
     */
    BiSysModuleEntity getByName(String name);
    /**
     * @description: 根据模块id查询系统模块信息
     * @author Will
     * @date: 2022/12/28 16:06
     * @param moduleId
     * @return ModuleSysConfigurationDTO
     */
    ModuleSysConfigurationDTO getByModuleId(String moduleId);
}
