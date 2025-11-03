package com.erp.server.workflow.service;
import cn.hutool.json.JSONObject;
import com.erp.model.workflow.entity.CfgSystemFieldMappingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgSystemFieldMappingDTO;

import java.util.List;

/**
 * <p>
 * 远程查询配置 服务类
 * </p>
 *
 * @author will
 * @since 2025-10-17
 */
public interface CfgSystemFieldMappingService extends SuperService<CfgSystemFieldMappingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-10-17
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgSystemFieldMappingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-10-17
    * @param dto
    * @return
    */
    Boolean update(CfgSystemFieldMappingDTO.UpdateDTO dto);

    /**
     * 根据字段映射查询
     * @author will
     * @date 2025/10/20 09:34
     * @param paramList
     * @return List<CfgSystemFieldMappingEntity>
     */
    List<CfgSystemFieldMappingEntity> listSystemFieldMapping(List<CfgSystemFieldMappingDTO.FieldMappingParamDTO> paramList);
    /**
     * 根据系统字段配置查询
     * @author will
     * @date 2025/10/20 10:40
     * @param cfgSystemFieldMappingEntity
     * @return List<JSONObject>
     */
    List<JSONObject> listFeignQueryData(CfgSystemFieldMappingEntity cfgSystemFieldMappingEntity);
}
