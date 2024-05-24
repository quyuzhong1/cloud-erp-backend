package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.CfgOperateLogFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.CfgOperateLogFieldDTO;

import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-05-22
 */
public interface CfgOperateLogFieldService extends SuperService<CfgOperateLogFieldEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-05-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgOperateLogFieldDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-05-22
    * @param dto
    * @return
    */
    Boolean update(CfgOperateLogFieldDTO.UpdateDTO dto);
    /**
     * @description: 根据类路径查询配置的字段
     * @author Will
     * @date: 2023/3/20 15:56
     * @param classPaths
     * @return List<CfgOperateLogFieldEntity>
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
