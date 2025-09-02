package com.erp.server.dmp.inout.handler.input.task.init;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;

/**
 * dmp输入任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
public abstract class DmpInputInitHandler extends DmpInputTaskHandler{
	
	@Override
	public void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputInitRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpInputInitResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpInputInitRequest) dmpRequest, (DmpInputInitResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputInitRequest dmpRequest, DmpInputInitResponse dmpResponse, DmpHandlerChain chain) {
		this.beforeToDoStatus(dmpRequest, dmpResponse);
		
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = this.getInitData(dmpRequest , dmpResponse);
		checkLimitSizeAndCreateNewSplitTask(dmpInputTaskInitDTOList);
		
		dmpResponse.getConvertInputTaskInitDTOListMaps().put(dmpCfgInputConvertEntity, dmpInputTaskInitDTOList);
		this.afterToDoStatus(dmpRequest, dmpResponse);
		
		dmpResponse.setDoUpdateStatus(false);
		DmpOutputTaskRequest dmpOutputInitRequest = new DmpOutputTaskRequest();
		dmpOutputInitRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
		this.doBaseChain(dmpRequest, dmpResponse, chain, dmpOutputInitRequest);
	}
	
	/**
	 * 获取外部初始数据
	 * @param dmpRequest
	 * @param dmpResponse
	 * @return
	 */
	public abstract List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse);
	
	/**
	 * 检查接口返回最大数量并生成拆分任务
	 * @param dmpInputTaskInitDTOList
	 */
	private void checkLimitSizeAndCreateNewSplitTask(List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList){
		String taskType = dmpInputTaskEntity.getTaskType();
		long between = 0L;
		LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
		LocalDateTime endTime = dmpInputTaskEntity.getEndTime();
		if(startTime != null && endTime != null) {
			between = LocalDateTimeUtil.between(startTime, endTime , ChronoUnit.SECONDS);
		}
		
		if(between > 3 && (DmpInputTaskTaskTypeEnum.NORMAL.getCode().equals(taskType) || DmpInputTaskTaskTypeEnum.HISTORY.getCode().equals(taskType)) 
				&& CollUtil.isNotEmpty(dmpInputTaskInitDTOList)) {
			String extendJson = dmpCfgInputEntity.getExtendJson();
			if(StringUtils.isNotBlank(extendJson)) {
				JSONObject parseObject = JSON.parseObject(extendJson);
				Integer limitSize = parseObject.getInteger("dmpLimitSize");
				Integer intervalTime = parseObject.getInteger("dmpIntervalTime");
				if(intervalTime == null) {
					intervalTime = 300;
				}
				int size = dmpInputTaskInitDTOList.size();
				if(limitSize != null && limitSize > 1 && size > limitSize) {
					DmpInputTaskEntity startDmpInputTaskEntity = BeanUtil.copyProperties(dmpInputTaskEntity, DmpInputTaskEntity.class);
					startDmpInputTaskEntity.setId(null);
					startDmpInputTaskEntity.setCreateTime(LocalDateTime.now());
					startDmpInputTaskEntity.setUpdateTime(LocalDateTime.now());
					startDmpInputTaskEntity.setErrorMessage(dmpInputTaskEntity.getId() + "拆分任务" + size);
					startDmpInputTaskEntity.setErrorCount(0);
					startDmpInputTaskEntity.setNextExecTime(LocalDateTimeUtil.offset(LocalDateTime.now(), intervalTime, ChronoUnit.SECONDS));
					startDmpInputTaskEntity.setStartTime(startTime);
					startDmpInputTaskEntity.setEndTime(LocalDateTimeUtil.offset(startTime, between / 2, ChronoUnit.SECONDS));
					
					DmpInputTaskEntity endDmpInputTaskEntity = BeanUtil.copyProperties(startDmpInputTaskEntity, DmpInputTaskEntity.class);
					endDmpInputTaskEntity.setNextExecTime(LocalDateTimeUtil.offset(startDmpInputTaskEntity.getNextExecTime(), intervalTime, ChronoUnit.SECONDS));
					endDmpInputTaskEntity.setStartTime(startDmpInputTaskEntity.getEndTime());
					endDmpInputTaskEntity.setEndTime(endTime);
					
					dmpInputTaskService.saveBatch(Arrays.asList(startDmpInputTaskEntity , endDmpInputTaskEntity));
					dmpInputTaskInitDTOList.clear();
				}
			}
		}
	}
}
