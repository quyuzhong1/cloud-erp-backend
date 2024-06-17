package com.erp.server.dmp.inout.handler.input.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.common.business.utils.ApplicationContextUtils;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpCfgInputTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.DmpCfgOutputDetailService;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpInputTaskHandler extends DmpInputHandler{
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
	@Autowired
	private DmpCfgOutputDetailService dmpCfgOutputDetailService;
	@Autowired
	private DmpCfgApiService dmpCfgApiService;
	
	protected String inputTaskId;
	protected String convertId;
	protected DmpCfgInputConvertEntity dmpCfgInputConvertEntity;
	protected boolean currStatusLastHandlerFlag = false;
	
	public void setInputTaskId(String inputTaskId) {
		this.inputTaskId = inputTaskId;
	}

	public void setConvertId(String convertId) {
		this.convertId = convertId;
	}

	public void setDmpCfgInputConvertEntity(DmpCfgInputConvertEntity dmpCfgInputConvertEntity) {
		this.dmpCfgInputConvertEntity = dmpCfgInputConvertEntity;
	}
	
	public void setCurrStatusLastHandlerFlag(boolean currStatusLastHandlerFlag) {
		this.currStatusLastHandlerFlag = currStatusLastHandlerFlag;
	}

	@Override
	public void doDmpHandler(DmpRequest dmpRequest, DmpResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputTaskRequest)) {
            throw new ServiceException(dmpRequest + " not DmpInputRequest");
        }
        if (!(dmpResponse instanceof DmpInputTaskResponse)) {
            throw new ServiceException(dmpResponse + " not DmpInputResponse");
        }
        doDmpHandler((DmpInputTaskRequest) dmpRequest, (DmpInputTaskResponse) dmpResponse, chain);
	}
	
	protected void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	protected <T extends DmpHandler> T getDmpHandlerBean(String beanClass , Class<T> clazz) {
		if(StringUtils.isBlank(beanClass)) {
			return null;
		}
		return ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(beanClass) , clazz);
	}
	
	protected List<String> getOutputClassList(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse) {
		List<String> apiClassList = new ArrayList<>();
		List<DmpCfgOutputEntity> dmpCfgOutputEntityList = dmpCfgOutputService.lambdaQuery()
				.eq(DmpCfgOutputEntity::getInputConvertId, convertId)
				.eq(DmpCfgOutputEntity::getDisabled, Boolean.FALSE)
				.list();
		if(CollUtil.isNotEmpty(dmpCfgOutputEntityList)) {
			DmpCfgInputDetailEntity dmpCfgInputDetailEntity = dmpResponse.getDmpCfgInputDetailEntity();
			for(DmpCfgOutputEntity dmpCfgOutputEntity : dmpCfgOutputEntityList) {
				List<DmpCfgOutputDetailEntity> dmpCfgOutputDetailEntityList = dmpCfgOutputDetailService.lambdaQuery()
						.eq(DmpCfgOutputDetailEntity::getMainId, dmpCfgOutputEntity.getId())
						.eq(StringUtils.isNotBlank(dmpCfgInputDetailEntity.getNextLevelId()) , DmpCfgOutputDetailEntity::getNextLevelId, dmpCfgInputDetailEntity.getNextLevelId())
						.list();
				for(DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity : dmpCfgOutputDetailEntityList) {
					String type = dmpCfgOutputEntity.getType();
					String apiClass = "";
					if(DmpCfgInputTypeEnum.API.getCode().equals(type)) {
						String typeId = dmpCfgOutputEntity.getTypeId();
						if(StringUtils.isNotBlank(typeId)) {
							DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
							apiClass = dmpCfgApiEntity.getApiClass();
						}
					}else if(DmpCfgInputTypeEnum.MQ.getCode().equals(type)) {
						apiClass = "mqPushHandler";
					}else if(DmpCfgInputTypeEnum.DB.getCode().equals(type)) {
						apiClass = "dbPushHandler";
					}
					apiClassList.add(apiClass);
				}
			}
		}
		return apiClassList;
	}
	
	protected boolean updateTaskStatus(DmpInputTaskStatusEnum taskStatus) {
		if(currStatusLastHandlerFlag) {
			return dmpInputTaskService.lambdaUpdate()
					.eq(DmpInputTaskEntity::getId, inputTaskId)
					.set(DmpInputTaskEntity::getStatus, taskStatus.getCode())
					.set(DmpInputTaskEntity::getUpdateTime, LocalDateTime.now())
					.update();
		}
		return true;
	}
}
