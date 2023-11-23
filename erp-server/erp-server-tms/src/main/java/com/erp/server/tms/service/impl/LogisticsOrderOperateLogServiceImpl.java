package com.erp.server.tms.service.impl;


import com.common.business.enums.SyncStatusEnum;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * <p>
 * 物流平台订单操作记录 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
@Slf4j
@Service
public class LogisticsOrderOperateLogServiceImpl implements LogisticsOrderOperateLogService {
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public String pullOperateLog(String authId, String sourceId, String businessType, String logisticsPlatform, String status, String requestParamJson, String responseParamJson) {
        DmpPullTaskEntity dmpPullTaskEntity = new DmpPullTaskEntity();
        dmpPullTaskEntity.setSourcePlatformName(PlatformEnum.ERP_TMS.getDesc());
        dmpPullTaskEntity.setSourceType(businessType);
        dmpPullTaskEntity.setSourceId("");
        dmpPullTaskEntity.setSourceCode(logisticsPlatform);
        dmpPullTaskEntity.setTargetPlatformName(PlatformEnum.ERP_TMS.getDesc());
        //请求状态（0请求中 1请求成功 2请求失败）
        if (RequestStatusEnums.SUCCESS.getCode().equals(status)) {
            dmpPullTaskEntity.setStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
        } else {
            dmpPullTaskEntity.setStatus(SyncStatusEnum.FAILED_SYNC.getCode());
        }
        dmpPullTaskEntity.setMqTopic("");
        dmpPullTaskEntity.setMqTag("");
        dmpPullTaskEntity.setMqData(requestParamJson);
        dmpPullTaskEntity.setReturnMsg(responseParamJson);
        String s = null;
        try {
            s = dmpTaskFeign.saveOrUpdateDmpPullTask(dmpPullTaskEntity);
        } catch (Exception e) {
            log.error("saveOrUpdateDmpPullTask:记录操作日志失败");
        }
        return s;
    }

    @Override
    public String pushOperateLog(String authId, String sourceId, String businessType, String logisticsPlatform, String status, String requestParamJson, String responseParamJson) {
        DmpPushTaskEntity dmpPushTaskEntity = new DmpPushTaskEntity();
        dmpPushTaskEntity.setSourcePlatformName(PlatformEnum.ERP_TMS.getDesc());
        dmpPushTaskEntity.setSourceType(businessType);
        dmpPushTaskEntity.setSourceId("");
        dmpPushTaskEntity.setSourceCode(logisticsPlatform);
        dmpPushTaskEntity.setTargetPlatformName(PlatformEnum.ERP_TMS.getDesc());
        //请求状态（0请求中 1请求成功 2请求失败）
        if (RequestStatusEnums.SUCCESS.getCode().equals(status)) {
            dmpPushTaskEntity.setStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
        } else {
            dmpPushTaskEntity.setStatus(SyncStatusEnum.FAILED_SYNC.getCode());
        }
        dmpPushTaskEntity.setMqTopic("");
        dmpPushTaskEntity.setMqTag("");
        dmpPushTaskEntity.setMqData(requestParamJson);
        dmpPushTaskEntity.setReturnMsg(responseParamJson);
        String s = null;
        try {
            s = dmpTaskFeign.saveOrUpdateDmpPushTask(dmpPushTaskEntity);
        } catch (Exception e) {
            log.error("saveOrUpdateDmpPushTask:记录操作日志失败");
        }
        return s;
    }
}
