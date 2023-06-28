package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.entity.ApiPlmSyncLogEntity;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/11 11:38
 */
public interface ApiPlmSyncLogService extends IService<ApiPlmSyncLogEntity> {

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/1/11 16:15
     * @param dto
     * @return Boolean
     */
    Boolean insert(ApiPlmSyncLogDTO dto);

    /**
     * 查询
     * @param apiPlatFormName
     * @param moduleType
     * @param businessId
     * @return
     */
    ApiPlmSyncLogEntity find(String apiPlatFormName, Integer moduleType, String businessId);

    /**
     * 更新错误日志
     * @param id
     * @param requestParam
     * @param msg
     */
    void updateLog(String id, String requestParam, String msg);

}
