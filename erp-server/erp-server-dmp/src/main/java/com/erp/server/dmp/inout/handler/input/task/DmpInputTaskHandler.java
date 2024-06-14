package com.erp.server.dmp.inout.handler.input.task;

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
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpCfgInputTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.DmpCfgOutputDetailService;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入创建任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpInputTaskHandler extends DmpInputHandler{
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	@Autowired
	private DmpCfgInputService dmpCfgInputService;
	@Autowired
	private DmpCfgInputDetailService dmpCfgInputDetailService;
	@Autowired
	private DmpCfgInputConvertService dmpCfgInputConvertService;
	@Autowired
	private DmpCfgApiService dmpCfgApiService;
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
	@Autowired
	private DmpCfgOutputDetailService dmpCfgOutputDetailService;
	
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputTaskRequest)) {
            throw new ServiceException(dmpRequest + " not DmpInputCreateRequest");
        }
        if (!(dmpResponse instanceof DmpInputTaskResponse)) {
            throw new ServiceException(dmpResponse + " not DmpInputResponse");
        }
        doDmpHandler((DmpInputTaskRequest) dmpRequest, (DmpInputTaskResponse) dmpResponse, chain);
	}
	
	protected void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		String inputTaskId = dmpRequest.getInputTaskId();
		
		if(StringUtils.isBlank(inputTaskId)) {
			log.warn("输入任务id为空");
			return;
		}
		DmpInputTaskEntity dmpInputTaskEntity = dmpInputTaskService.getById(inputTaskId);
		if(dmpInputTaskEntity == null) {
			log.warn("输入任务不存在id={}" , inputTaskId);
			return;
		}
		
		dmpResponse.getBeforeDmpInputTaskEntityList().add(dmpInputTaskEntity);
		chain.addFirstDmpHandlerList(this.getDmpCfgInputConvertEntityList(dmpRequest, dmpResponse));
		
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	private List<DmpHandler> getDmpCfgInputConvertEntityList(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse){
		DmpInputTaskEntity dmpInputTaskEntity = dmpResponse.getBeforeDmpInputTaskEntityList().get(0);
		String inputStatus = dmpInputTaskEntity.getStatus();
		
		List<DmpHandler> dmpHandlerList = new ArrayList<>();
		
		DmpCfgInputDetailEntity dmpCfgInputDetailEntity = dmpCfgInputDetailService.getById(dmpInputTaskEntity.getInputDetailId());
		String mainId = dmpCfgInputDetailEntity.getMainId();
		if(DmpInputTaskStatusEnum.INIT.getCode().equals(inputStatus)) {
			DmpCfgInputEntity dmpCfgInputEntity = dmpCfgInputService.getById(mainId);
			String type = dmpCfgInputEntity.getType();
			if(DmpCfgInputTypeEnum.API.getCode().equals(type)) {
				String typeId = dmpCfgInputEntity.getTypeId();
				if(StringUtils.isNotBlank(typeId)) {
					DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
					dmpHandlerList.add(this.getDmpHandlerBean(dmpCfgApiEntity.getApiClass()));
				}
			}
		}else {
			List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpCfgInputConvertService.lambdaQuery()
					.eq(DmpCfgInputConvertEntity::getMainId, mainId)
					.eq(DmpCfgInputConvertEntity::getInputStatus, inputStatus)
					.eq(DmpCfgInputConvertEntity::getDisabled, Boolean.FALSE)
					.list();
			dmpCfgInputConvertEntityList.sort((d1 , d2) -> {
				if(d1 == null) {
					return -1;
				}
				if(d2 == null) {
					return 1;
				}
				return d1.getOrder().compareTo(d2.getOrder());
			});
			for(DmpCfgInputConvertEntity dmpCfgInputConvertEntity : dmpCfgInputConvertEntityList) {
				dmpHandlerList.add(this.getDmpHandlerBean(dmpCfgInputConvertEntity.getConvertClass()));
				String covertId = dmpCfgInputConvertEntity.getId();
				List<DmpCfgOutputEntity> dmpCfgOutputEntityList = dmpCfgOutputService.lambdaQuery()
						.eq(DmpCfgOutputEntity::getInputConvertId, covertId)
						.eq(DmpCfgOutputEntity::getDisabled, Boolean.FALSE)
						.list();
				if(CollUtil.isNotEmpty(dmpCfgOutputEntityList)) {
					for(DmpCfgOutputEntity dmpCfgOutputEntity : dmpCfgOutputEntityList) {
						List<DmpCfgOutputDetailEntity> dmpCfgOutputDetailEntityList = dmpCfgOutputDetailService.lambdaQuery()
								.eq(DmpCfgOutputDetailEntity::getMainId, dmpCfgOutputEntity.getId())
								.eq(DmpCfgOutputDetailEntity::getNextLevelId, dmpCfgInputDetailEntity.getNextLevelId())
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
							dmpHandlerList.add(this.getDmpHandlerBean(apiClass));
						}
					}
				}
			}
		}
		
		return dmpHandlerList;
	}
	
	private DmpHandler getDmpHandlerBean(String beanClass) {
		if(StringUtils.isBlank(beanClass)) {
			return null;
		}
		if(!beanClass.contains("\\.")) {
			beanClass = StringUtils.uncapitalize(beanClass);
		}
		return ApplicationContextUtils.getBean(beanClass, DmpHandler.class);
	}
}
