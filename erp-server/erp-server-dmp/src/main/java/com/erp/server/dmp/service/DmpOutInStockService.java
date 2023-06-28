package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpOutInStockEntity;
import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.PlatformEntity;


/**
 * <p>
 * 手工出入库待同步数据表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
 */
public interface DmpOutInStockService extends SuperService<DmpOutInStockEntity> {

    /**
     * 更新出入库同步信息（成功）
     * @param id
     * @param syncStatus
     * @param targetOrderNo
     * @param requestParam
     * @param platformEntity
     * @param type
     * @param approveType
     */
    void updateSyncInfoSuccess(String id, String syncStatus, String targetOrderNo, String requestParam,
                        PlatformEntity platformEntity, Integer type, String approveType);

    /**
     * 更新出入库同步信息（失败）
     * @param id
     * @param syncStatus
     * @param sourceCode
     * @param requestParam
     * @param platformEntity
     * @param type
     * @param errMsg
     * @param approveType
     */
    void updateSyncInfoError(String id, String syncStatus, String sourceCode, String requestParam,
                        PlatformEntity platformEntity, Integer type, String errMsg, String approveType);

}
