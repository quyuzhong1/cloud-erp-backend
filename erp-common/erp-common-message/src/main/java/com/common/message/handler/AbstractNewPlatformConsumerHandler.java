package com.common.message.handler;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 新中台订单处理器抽象类
 * @author Cloud
 */
@Slf4j
@Service
public abstract class AbstractNewPlatformConsumerHandler implements RocketMQListener<Object> {

    private static final long LOCK_WAIT_SECONDS = 120L;

	@Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void onMessage(Object ext) {
    	//json数据
    	String data = ext.toString();
		JSONObject jsonObject = JSON.parseObject(data);
        String dmpOutputTaskRecordId = jsonObject.getString("dmpOutputTaskRecordId");
        String bizName = this.getBizName();
        log.warn("{}接收到输出id={} ，数据：{}" , bizName , dmpOutputTaskRecordId , ext);
        String dmpOutputTaskRecordDataId = jsonObject.getString("dmpOutputTaskRecordDataId");
        DmpOutputTaskRecordDTO.UpdateDTO updateDTO = new DmpOutputTaskRecordDTO.UpdateDTO();
        updateDTO.setId(dmpOutputTaskRecordId);
        updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.FINISH.getCode());

        if(StringUtils.isBlank(dmpOutputTaskRecordId)) {
        	log.error("{}接收到异常数据 ，数据：{}" , bizName , ext);
        	return;
        }

        String lockKey = buildLockKey(dmpOutputTaskRecordDataId);
        RLock lock = StringUtils.isBlank(lockKey) ? null : redissonClient.getLock(lockKey);
        boolean locked = false;
        try {

            try {
                locked = tryLock(lock, lockKey, dmpOutputTaskRecordDataId);
                this.handle(data);
            } catch (Throwable e) {
                log.error("{}同步输出任务失败，msg = {}", bizName, e.getMessage(), e);
                updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode());
                updateDTO.setResponseData(bizName + "消费数据失败：" + ExceptionUtil.stacktraceToOneLineString(e));
                updateDTO.setMessage(bizName + "【" + e.getMessage() + "】");
            }

            int callbackRetry = 1;
            while (callbackRetry <= 3) {
                ApiResult<Boolean> result = null;
                try {
                    result = dmpInoutTaskFeign.updateOutputTaskRecord(updateDTO);
                } catch (Exception e) {
                    log.error("输出回调错误，id={}，回调信息={}", dmpOutputTaskRecordId, JSON.toJSONString(updateDTO), e);
                }
                if (result != null && result.getData() != null && Boolean.TRUE.equals(result.getData())) {
                    break;
                }
                callbackRetry = callbackRetry + 1;
            }
        } finally {
            releaseLock(lock, lockKey, locked);
        }
    }

    private String buildLockKey(String dmpOutputTaskRecordDataId) {
        if (StringUtils.isBlank(dmpOutputTaskRecordDataId)) {
            return null;
        }
        return "dmp:output:record:" + dmpOutputTaskRecordDataId;
    }

    private boolean tryLock(RLock lock, String lockKey, String dmpOutputTaskRecordDataId) {
        if (lock == null) {
            return false;
        }
        try {
            boolean locked = lock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                log.error("同步输出任务等待锁超时，放弃消费：{}，已等待{}秒", lockKey, LOCK_WAIT_SECONDS);
                throw new ServiceException("同步输出任务正在执行中，等待锁超时：" + dmpOutputTaskRecordDataId);
            }
            return true;
        } catch (InterruptedException e) {
            log.error("等待同步输出任务锁被中断：{}", lockKey, e);
            Thread.currentThread().interrupt();
            throw new ServiceException("等待同步输出任务锁被中断：" + dmpOutputTaskRecordDataId);
        }
    }

    private void releaseLock(RLock lock, String lockKey, boolean locked) {
        if (!locked || lock == null) {
            return;
        }
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            } else {
                log.warn("当前线程未持有同步输出任务锁，跳过释放：{}", lockKey);
            }
        } catch (Exception e) {
            log.warn("释放同步输出任务锁失败：{}", lockKey, e);
        }
    }

    /**
     * 获取业务类型
     * @return
     */
    public abstract String getBizName();

    /**
     * 处理平台数据
     */
    public abstract void handle(String data);
}