package com.common.message.handler;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 销售订单处理器抽象类
 * @author Cloud
 */
@Slf4j
@Service
public abstract class AbstractPlatformConsumerHandler<T extends DmpSyncTaskIdDTO> implements RocketMQListener<Object> {

	@Resource
    private RedisTemplate<String,Object> redisTemplate;
	
    @Override
    public void onMessage(Object obj) {
        log.info("监听到消息：{}", JSONUtil.toJsonStr(obj));
        String dmpSyncTaskId = "";
        String platform = "";
        String uniqueId = "";
        dmpSyncTaskId = new JSONObject(obj).getStr("dmpSyncTaskId");
        if (StringUtils.isBlank(dmpSyncTaskId)){
            log.error("平台数据消费异常:找不到dmpSyncTaskId, object={}", JSONUtil.toJsonStr(obj));
            return;
        }
        String redisKey = "dmp:sync:task:" + dmpSyncTaskId;
        
        int count = 1;
        while(!redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 30, TimeUnit.SECONDS)) {
        	log.warn("同步任务正在执行中：{}，重试获取锁次数：{}" , dmpSyncTaskId , count);
        	count = count + 1;
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
        }
    	try {
            platform = new JSONObject(obj).getStr("platform");
            uniqueId = new JSONObject(obj).getStr("uniqueId");
            
            ApiResult<?> handle = handle(obj);
            if (!handle.isSuccess()) {
                log.error("平台数据消费异常 {}", JSONUtil.toJsonStr(handle));
                updateSyncTaskStatus(dmpSyncTaskId, SyncStatusEnum.FAILED_SYNC, handle.getMsg());
                //异常预警
                sendWarnMsg(dmpSyncTaskId, handle.getMsg());
                updateMongodbData(platform, uniqueId, 0);
                return;
            }
            String msg = SyncStatusEnum.SUCCESS_SYNC.getName();
            if (Objects.nonNull(handle.getData())){
                //记录正确响应数据返回-留痕
                msg = JSONUtil.toJsonStr(handle.getData());
            }
            updateSyncTaskStatus(dmpSyncTaskId, SyncStatusEnum.SUCCESS_SYNC, msg);
            updateMongodbData(platform, uniqueId, 2);
        }catch (Throwable e) {
            try {
                updateSyncTaskStatus(dmpSyncTaskId, SyncStatusEnum.FAILED_SYNC,  ExceptionUtil.stacktraceToString(e, 2000));
                log.error("平台数据消费异常", e);
                //异常预警
                sendWarnMsg(dmpSyncTaskId, e.getMessage());
                updateMongodbData(platform, uniqueId, 0);
            } catch (Exception ex) {
                // 终止异常停止当前MQ重试，由重试任务处理重试
                log.error("处理平台数据消费异常:{}", ExceptionUtil.stacktraceToString(ex));
            }
        }finally {
        	redisTemplate.delete(redisKey);
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
    public abstract void updateMongodbData(String platform,String uniqueId, Integer isClean);

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