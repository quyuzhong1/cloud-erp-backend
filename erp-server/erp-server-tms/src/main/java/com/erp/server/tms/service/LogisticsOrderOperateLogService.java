package com.erp.server.tms.service;

import com.erp.model.tms.entity.LogisticsOrderOperateLogEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsOrderOperateLogDTO;

/**
 * <p>
 * 物流平台订单操作记录 服务类
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
public interface LogisticsOrderOperateLogService extends SuperService<LogisticsOrderOperateLogEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author zdy
     * @date: 2023-11-08
     */
    BaseResultDTO.AddDTO add(LogisticsOrderOperateLogDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author zdy
     * @date: 2023-11-08
     */
    Boolean update(LogisticsOrderOperateLogDTO.UpdateDTO dto);

    /**
     * 增加接口调用记录
     *
     * @param authId
     * @param sourceId
     * @param businessType
     * @param logisticsPlatform
     * @param status
     * @param requestParamJson
     * @param responseParamJson
     */
    LogisticsOrderOperateLogEntity addOperateLog(String authId, String sourceId, String businessType, String logisticsPlatform, String status, String requestParamJson, String responseParamJson);
    /**
     * 增加接口调用记录
     *
     * @param authId
     * @param sourceId
     * @param businessType
     * @param logisticsPlatform
     * @param status
     * @param requestParamJson
     * @param responseParamJson
     */
    String pullOperateLog(String authId, String sourceId, String businessType, String logisticsPlatform, String status, String requestParamJson, String responseParamJson);
    /**
     * 增加接口调用记录
     *
     * @param authId
     * @param sourceId
     * @param businessType
     * @param logisticsPlatform
     * @param status
     * @param requestParamJson
     * @param responseParamJson
     */
    String pushOperateLog(String authId, String sourceId, String businessType, String logisticsPlatform, String status, String requestParamJson, String responseParamJson);

}
