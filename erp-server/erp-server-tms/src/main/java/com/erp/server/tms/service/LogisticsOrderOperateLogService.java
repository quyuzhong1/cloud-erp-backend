package com.erp.server.tms.service;

/**
 * <p>
 * 物流平台订单操作记录 服务类
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
public interface LogisticsOrderOperateLogService {

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
