package com.erp.server.workflow.service;

import cn.hutool.json.JSONObject;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.workflow.dto.ThirdProcessManagementDTO;
import com.erp.model.workflow.entity.ThirdProcessManagementEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2025-05-23
 */
public interface ThirdProcessManagementService extends SuperService<ThirdProcessManagementEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdProcessManagementDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    Boolean update(ThirdProcessManagementDTO.UpdateDTO dto);


    /**
     * add or update
     * @param jsonObject
     */
    void addOrUpdate(JSONObject jsonObject, String sourcePlatform);
    /**
     * 业务id和业务key
     * @author will
     * @date 2025/6/27 17:57
     * @param businessId
     * @param businessKey
     * @return ThirdProcessManagementEntity
     */
    ThirdProcessManagementEntity getLastByBusinessIdAndKey(String businessId, String businessKey);
    /**
     * 查询进行中的流程
     * @author will
     * @date 2025/6/30 12:11
     * @param processDefinitionIdList
     * @return List<ThirdProcessManagementEntity>
     */
    List<ThirdProcessManagementEntity> listDoing(List<String> processDefinitionIdList);
}
