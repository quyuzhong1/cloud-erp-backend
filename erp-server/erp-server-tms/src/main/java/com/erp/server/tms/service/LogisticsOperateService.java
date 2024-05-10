package com.erp.server.tms.service;

import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;

/**
 * <p>
 * 物流平台订单操作记录 服务类
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
public interface LogisticsOperateService {

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
     * @param sourceId
     * @param sourceCode
     * @param businessType
     * @param logisticsPlatform
     * @param status
     * @param requestParamJson
     * @param responseParamJson
     * @param isSendMsg
     */
    String pushOperateLog(String sourceId, String sourceCode, String businessType, String logisticsPlatform, String status, String requestParamJson, String responseParamJson, Boolean isSendMsg);

    /**
     * 拉取数据 增加飞书预警
     * @param entity
     */
    void sendPullWarnMsg(DmpPullTaskEntity entity);
    /**
     * 创建订单 增加飞书预警
     * @param entity
     */
    void sendPushWarnMsg(DmpPushTaskEntity entity);
}
