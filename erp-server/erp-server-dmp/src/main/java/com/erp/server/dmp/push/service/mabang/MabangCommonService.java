package com.erp.server.dmp.push.service.mabang;

import com.erp.model.dmp.entity.ApiPlmSyncLogEntity;
import com.erp.model.dmp.entity.PlatformEntity;

import java.util.Map;

/**
 * @Classname: MabangCommonService
 * @Description: 马帮公关服务接口
 * @CreateTime: 2023-06-27  17:31
 * @Author: zhangchunlin
 */
public interface MabangCommonService {

    /**
     * @description: 查询平台
     */
    PlatformEntity getPlatformEntity(String bizId, Integer type);

    /**
     * 记录日志
     * @param platformEntity
     * @param businessId
     * @param jsonData
     * @param msg
     * @param type
     * @param status
     */
    void insertLogWriteBackSyncMabangStatus(PlatformEntity platformEntity, String businessId,
                                                    String jsonData, String msg, Integer type, Integer status);

    /**
     * 操作成功添加日志
     * @param platformEntity
     * @param businessId
     * @param jsonData
     * @param msg
     */
    void insertSyncLog(PlatformEntity platformEntity,String businessId,String jsonData,String msg,Integer type,Integer status);

    /**
     * 查询日志
     * @param platformEntity
     * @param businessId
     * @param type
     */
    ApiPlmSyncLogEntity findLog(PlatformEntity platformEntity, String businessId, Integer type);

    /**
     * 更新日志
     * @param id
     * @param requestParam
     * @param msg
     */
    void updateLog(String id, String requestParam, String msg);

}
