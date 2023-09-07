package com.common.message.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import lombok.extern.slf4j.Slf4j;
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
        T  ext = (T) obj;
        try {
            ApiResult handle = handle(ext);
            if (!handle.isSuccess()) {
                log.error("平台数据消费异常 {}", JSONUtil.toJsonStr(handle));
                updateSyncTaskStatus(ext.getDmpSyncTaskId(), SyncStatusEnum.FAILED_SYNC, handle.getMsg());
                return;
            }
            updateSyncTaskStatus(ext.getDmpSyncTaskId(), SyncStatusEnum.SUCCESS_SYNC, SyncStatusEnum.SUCCESS_SYNC.getName());
        }catch (Exception e) {
            updateSyncTaskStatus(ext.getDmpSyncTaskId(), SyncStatusEnum.FAILED_SYNC, StrUtil.isBlank(e.getMessage()) ? e.getMessage() : ExceptionUtil.stacktraceToString(e));
            log.error("平台数据消费异常", e);
        }
    }

    /**
     * 更新同步任务状态
     * @param syncTaskId
     * @param code
     */
    public abstract void updateSyncTaskStatus(String syncTaskId, SyncStatusEnum code, String msg);

    /**
     * 处理平台数据
     * @param ext
     */
    public abstract ApiResult handle(T ext);

}