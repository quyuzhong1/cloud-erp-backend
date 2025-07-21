package com.erp.server.dmp.inout.job;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.erp.server.dmp.inout.dto.request.DmpEtlCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpEtlCreateFactory;
import com.erp.server.dmp.service.DmpCfgEtlService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmpEtlCreateJob {
	@Autowired
	private DmpEtlCreateFactory dmpEtlCreateFactory;
	
	@Autowired
	private DmpCfgEtlService dmpCfgEtlService;
	
	@Resource
    private RedisTemplate<String,Object> redisTemplate;
	
	/**
	 * 创建Etl任务
	 * @return
	 */
	@XxlJob("createEtlTaskByAppId")
	public ReturnT createEtlTaskByAppId(){
		String appId = XxlJobHelper.getJobParam();
		List<DmpCfgEtlEntity> list = dmpCfgEtlService.lambdaQuery()
				.eq(DmpCfgEtlEntity::getDisabled, false)
				.eq(DmpCfgEtlEntity::getAppId, appId)
				.list();
		if(CollUtil.isNotEmpty(list)) {
			for(DmpCfgEtlEntity l : list) {
				String flowName = l.getFlowName();
				String id = l.getId();
				String redisKey = "dmp:etl:create:id:" + id;
				if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 300, TimeUnit.SECONDS)) {
					try {
						DmpEtlCreateRequest dmpRequest = new DmpEtlCreateRequest();
						dmpRequest.setCfgEtlId(id);
						dmpEtlCreateFactory.createEtlTask(dmpRequest);
					} catch (Exception e) {
						log.error("Etl任务生成错误flowName={}" , flowName , e);
					} finally {
						redisTemplate.delete(redisKey);
					}
				}else {
					log.error("Etl任务生成正在执行中flowName={}" , flowName);
				}
			}
		}
		
		return ReturnT.SUCCESS;
	}
	
}
