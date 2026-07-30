package com.erp.server.dmp.inout.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DmpInputCreateJob {
	private static final String INPUT_CREATE_LOCK_PREFIX = "dmp:input:create:system:lock:";

	@Autowired
	private DmpInputCreateFactory dmpInputCreateFactory;
	
	@Autowired
	private DmpCfgInputService dmpCfgInputService;
	
	@Autowired
	private RedissonClient redissonClient;
	
	/**
	 * 创建正常任务
	 * @return
	 */
	@XxlJob("createNormalInputTask")
    public ReturnT createNormalInputTask(){
		String cfgInputId = XxlJobHelper.getJobParam();
		DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
		dmpRequest.setCfgInputId(cfgInputId);
		dmpInputCreateFactory.createNormalInputTask(dmpRequest);
        return ReturnT.SUCCESS;
    }
	
	/**
	 * 创建历史任务
	 * @return
	 */
	@XxlJob("createHistoryInputTask")
    public ReturnT createHistoryInputTask(){
		String cfgInputId = XxlJobHelper.getJobParam();
		DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
		dmpRequest.setCfgInputId(cfgInputId);
		dmpInputCreateFactory.createHistoryInputTask(dmpRequest);
        return ReturnT.SUCCESS;
    }
	
	/**
	 * 根据系统创建正常任务和历史任务
	 * @return
	 */
	@XxlJob("createInputTaskBySystem")
	public ReturnT createInputTaskBySystem(){
		String systemId = XxlJobHelper.getJobParam();
		RLock lock = redissonClient.getLock(INPUT_CREATE_LOCK_PREFIX + systemId);
		if (!lock.tryLock()) {
			log.warn("输入任务生成正在执行中，跳过本次调度，systemId={}", systemId);
			return ReturnT.SUCCESS;
		}

		long startMillis = System.currentTimeMillis();
		int cfgInputCount = 0;
		try {
			List<DmpCfgInputEntity> list = dmpCfgInputService.lambdaQuery()
					.eq(DmpCfgInputEntity::getSystemId, systemId)
					.eq(DmpCfgInputEntity::getDisabled, false)
					.select(DmpCfgInputEntity::getId)
					.list();
			cfgInputCount = list.size();
			if(CollUtil.isNotEmpty(list)) {
				DmpInputCreateRequest dmpRequest = null;
				for(DmpCfgInputEntity l : list) {
					String id = l.getId();

					dmpRequest = new DmpInputCreateRequest();
					dmpRequest.setCfgInputId(id);
					dmpInputCreateFactory.createNormalInputTask(dmpRequest);

					dmpRequest = new DmpInputCreateRequest();
					dmpRequest.setCfgInputId(id);
					dmpInputCreateFactory.createCompensateInputTask(dmpRequest);

					dmpRequest = new DmpInputCreateRequest();
					dmpRequest.setCfgInputId(id);
					dmpInputCreateFactory.createHistoryInputTask(dmpRequest);
				}
			}
		} catch (Exception e) {
			log.error("输入任务生成错误systemId={}" , systemId , e);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
			log.info("输入任务生成结束，systemId={}，配置数量={}，耗时={}ms",
					systemId, cfgInputCount, System.currentTimeMillis() - startMillis);
		}
		
		return ReturnT.SUCCESS;
	}
	
	/**
	 * 创建快速任务并执行
	 * @return
	 */
	@XxlJob("doHotfixInputTask")
	public ReturnT doHotfixInputTask(){
		// {"cfgInputId":""}
		dmpInputCreateFactory.doHotfixInputTask(JSON.parseObject(XxlJobHelper.getJobParam() , DmpInputHotfixCreateRequest.class));
		return ReturnT.SUCCESS;
	}


	/**
	 * 根据系统和任务类型创建任务
	 * systemId=系统ID
	 * taskType=任务类型
	 */
	@XxlJob("createInputTaskByParams")
	public ReturnT<String> createInputTaskByParams(){
		String jobParam = XxlJobHelper.getJobParam();
		String systemId = JSONObject.parseObject(jobParam).getString("systemId");
		String taskTypeStr = JSONObject.parseObject(jobParam).getString("taskTypeList");
		XxlJobHelper.log("【任务开始】任务参数: jobParam:{}",jobParam);
		List<String> taskTypeList = Arrays.stream(taskTypeStr.split(",")).collect(Collectors.toList());
		if (StringUtils.isBlank(systemId) || CollectionUtils.isEmpty(taskTypeList)){
			XxlJobHelper.log("【任务结束】任务参数缺失:systemId或taskType jobParam:{}",jobParam);
			return ReturnT.FAIL;
		}

		RLock lock = redissonClient.getLock(INPUT_CREATE_LOCK_PREFIX + systemId);
		if (!lock.tryLock()) {
			XxlJobHelper.log("【任务结束】任务正在执行中 jobParam:{}",jobParam);
			return ReturnT.FAIL;
		}

		long startMillis = System.currentTimeMillis();
		int cfgInputCount = 0;
		try {
			List<String> taskIdlist = dmpCfgInputService.listBySystemIdAndTaskType(systemId, taskTypeList);
			cfgInputCount = taskIdlist.size();
			if (CollectionUtils.isEmpty(taskIdlist)){
				XxlJobHelper.log("【任务结束】无可执行的任务");
				return ReturnT.SUCCESS;
			}
			// 正常
			if (taskTypeList.contains(DmpInputTaskTaskTypeEnum.NORMAL.getCode())){
				taskIdlist.forEach(id -> {
					DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
					dmpRequest.setCfgInputId(id);
					dmpInputCreateFactory.createNormalInputTask(dmpRequest);
				});
			}
			// 补偿
			if (taskTypeList.contains(DmpInputTaskTaskTypeEnum.COMPENSATE.getCode())){
				taskIdlist.forEach(id -> {
					DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
					dmpRequest.setCfgInputId(id);
					dmpInputCreateFactory.createCompensateInputTask(dmpRequest);
				});
			}
			// 历史
			if (taskTypeList.contains(DmpInputTaskTaskTypeEnum.HISTORY.getCode())){
				taskIdlist.forEach(id -> {
					DmpInputCreateRequest dmpRequest = new DmpInputCreateRequest();
					dmpRequest.setCfgInputId(id);
					dmpInputCreateFactory.createHistoryInputTask(dmpRequest);
				});
			}
		} catch (Exception e) {
			log.error("【任务结束】输入任务生成错误:systemId={}, error={}" , systemId , ExceptionUtil.stacktraceToString(e));
			XxlJobHelper.log("【任务结束】输入任务生成错误:systemId={},jobParam={}, error={}",jobParam, ExceptionUtil.stacktraceToString(e));
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
			long elapsedMillis = System.currentTimeMillis() - startMillis;
			log.info("按参数生成输入任务结束，systemId={}，配置数量={}，耗时={}ms",
					systemId, cfgInputCount, elapsedMillis);
			XxlJobHelper.log("【任务结束】任务执行结束 systemId:{},配置数量:{},耗时:{}ms",
					systemId, cfgInputCount, elapsedMillis);
		}
		return ReturnT.SUCCESS;
	}
}
