package com.common.message.handler;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 新中台订单处理器抽象类
 * @author Cloud
 */
@Slf4j
@Service
public abstract class AbstractNewPlatformConsumerHandler implements RocketMQListener<Object> {

	@Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;
    
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
	
    @Override
    public void onMessage(Object ext) {
    	//json数据
    	String data = ext.toString();
		JSONObject jsonObject = JSON.parseObject(data);
        String dmpOutputTaskRecordId = jsonObject.getString("dmpOutputTaskRecordId");
        MDC.put("traceId", dmpOutputTaskRecordId);
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
        
        int count = 1;
        // 检查和等待
        checkAndWait(dmpOutputTaskRecordDataId, count);

        try {
        	 this.handle(data);
        } catch (Throwable e) {
            log.error("{}同步输出任务失败，msg = {}",bizName ,e.getMessage(),e);
            updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode());
            updateDTO.setResponseData(bizName + "消费数据失败：" + ExceptionUtil.stacktraceToOneLineString(e));
            updateDTO.setMessage(bizName + "【" + e.getMessage() + "】");
        }
        
        count = 1;
        while(count <= 3) {
        	ApiResult<Boolean> result = null;
        	try {
				result = dmpInoutTaskFeign.updateOutputTaskRecord(updateDTO);
			} catch (Exception e) {
				log.error("输出回调错误，id={}，回调信息={}" , dmpOutputTaskRecordId , JSON.toJSONString(updateDTO) , e);
			}
        	if(result != null && result.getData() != null && Boolean.TRUE.equals(result.getData())) {
        		break;
        	}else {
        		count = count + 1;
        		for(int i=0;i < 1000;i++);//相当于休眠，执行时间约3700纳秒，1毫秒等于10^6纳秒
        	}
        }
    }

    private void checkAndWait(String dmpOutputTaskRecordDataId, int count) {
        if(StringUtils.isNotBlank(dmpOutputTaskRecordDataId)) {
        	String redisKey = "dmp:output:record:" + dmpOutputTaskRecordDataId;
            while(!redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 30, TimeUnit.SECONDS)) {
            	log.warn("同步输出任务正在执行中：{}，重试获取锁次数：{}" , redisKey , count);
            	count = count + 1;
            	try {
    				Thread.sleep(1000);
    			} catch (InterruptedException e) {
    			}
            }
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