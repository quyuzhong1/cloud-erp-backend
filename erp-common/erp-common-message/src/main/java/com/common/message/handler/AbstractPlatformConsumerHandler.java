package com.common.message.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * 销售订单处理器抽象类
 * @author Cloud
 */
@Slf4j
@Service
public abstract class AbstractPlatformConsumerHandler<T extends DmpSyncTaskIdDTO> implements RocketMQListener<Object> {

    @Override
    public void onMessage(Object obj) {
        String dmpSyncTaskId = "";
        String platform = "";
        String uniqueId = "";
        try {
            dmpSyncTaskId = new JSONObject(obj).getStr("dmpSyncTaskId");
            platform = new JSONObject(obj).getStr("platform");
            uniqueId = new JSONObject(obj).getStr("uniqueId");
            if (StringUtils.isBlank(dmpSyncTaskId)){
                log.error("平台数据消费异常:找不到dmpSyncTaskId, object={}", JSONUtil.toJsonStr(obj));
                return;
            }
            ApiResult<?> handle = handle(obj);
            if (!handle.isSuccess()) {
                log.error("平台数据消费异常 {}", JSONUtil.toJsonStr(handle));
                updateSyncTaskStatus(dmpSyncTaskId, SyncStatusEnum.FAILED_SYNC, handle.getMsg());
                //异常预警
                sendWarnMsg(dmpSyncTaskId, handle.getMsg());
                updateMongodbData(platform, uniqueId, 0);
                return;
            }
            updateSyncTaskStatus(dmpSyncTaskId, SyncStatusEnum.SUCCESS_SYNC, SyncStatusEnum.SUCCESS_SYNC.getName());
            updateMongodbData(platform, uniqueId, 2);
        }catch (Exception e) {
            updateSyncTaskStatus(dmpSyncTaskId, SyncStatusEnum.FAILED_SYNC, StrUtil.isBlank(e.getMessage()) ? e.getMessage() : ExceptionUtil.stacktraceToString(e));
            log.error("平台数据消费异常", e);
            //异常预警
            sendWarnMsg(dmpSyncTaskId, e.getMessage());
            updateMongodbData(platform, uniqueId, 0);
        }
    }

    /**
     * 更新同步任务状态
     * @param syncTaskId
     * @param code
     */
    public abstract void updateSyncTaskStatus(String syncTaskId, SyncStatusEnum code, String msg);

    /**
     * 更新mongodb状态
     * @param platform
     * @param uniqueId
     * @param isClean
     */
    public void updateMongodbData(String platform,String uniqueId, Integer isClean){// TODO
    };

    /**
     * 预警
     */
    public abstract void sendWarnMsg(String syncTaskId, String msg);

    /**
     * 处理平台数据
     * @param ext
     */
    public abstract ApiResult<?> handle(Object ext);

}