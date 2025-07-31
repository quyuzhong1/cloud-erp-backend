package com.common.message.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
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
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * restcloud处理器抽象类
 * @author Cloud
 */
@Slf4j
@Service
public abstract class AbstractRestCloudPlatformConsumerHandler implements RocketMQListener<Object> {

	@Value("${restcloud.url:172.16.100.96}")
    private String restcloudUrl;
	
	@Value("${restcloud.port:8080}")
	private String restcloudPort;
	
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
        		Map<String, Object> map = new HashMap<>();
        		map.put("data", Arrays.asList(updateDTO));
				HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/push/updatePushStatus")
		                .header("Content-Type", "application/json")
		                .body(JSON.toJSONString(map))
		                .timeout(60000)
		                .execute();
				if (200 != response.getStatus()) {
					log.error("输出回调restCloud错误");
				}else {
					String body = response.body();
					JSONObject responseJson = JSON.parseObject(body);
					Integer resultCode = responseJson.getInteger("resultCode");
		            // 判断结果异常:ETLProcessRunResultCode
		            if (null != resultCode && 1 == resultCode) {
		            	result = ApiResult.success(true);
		            }else {
		            	log.error("输出回调restCloud失败：" + body);
		            }
				}
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
                    log.error( "线程睡眠阻塞: Interrupted!:{}", e.getMessage());
                    Thread.currentThread().interrupt();
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