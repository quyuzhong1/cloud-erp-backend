package com.erp.server.dmp.inout.handler.factory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.input.all.DmpInputTaskStatusHandler;
import com.erp.server.dmp.inout.handler.input.task.DmpInputBaseTaskHandler;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 输入任务工厂，添加handler给handler链路执行
 * @author Administrator
 *
 */
@Slf4j
@Component
public class DmpInputTaskFactory{
	
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	@Autowired
	private DmpInputBaseTaskHandler dmpInputBaseTaskHandler;
	@Autowired
	private DmpInputTaskStatusHandler dmpInputTaskStatusHandler;
	
	/**
	 * 执行输入任务
	 * @param dmpInputFinishRequest
	 * @return
	 */
	public DmpInputFinishResponse dealInputTask(DmpInputFinishRequest dmpInputFinishRequest) {
		DmpInputTaskStatusEnum[] values = DmpInputTaskStatusEnum.values();
		DmpInputFinishResponse dmpResponse = new DmpInputFinishResponse();
		for(DmpInputTaskStatusEnum value : values) {
			if(value != DmpInputTaskStatusEnum.INIT && value != DmpInputTaskStatusEnum.ERROR) {
				try {
					dmpInputFinishRequest.setDealTaskStatus(value);
					DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
					bean.addDmpHandler(dmpInputBaseTaskHandler);
					bean.addDmpHandler(dmpInputTaskStatusHandler);
					bean.doDmpHandler(dmpInputFinishRequest, dmpResponse);
				} catch (Exception e) {
					log.error("{}任务执行报错" , dmpInputFinishRequest.getInputTaskId(), e);
					Integer maxRetryCount = 3;
					DmpCfgInputDetailEntity dmpCfgInputDetailEntity = dmpResponse.getDmpCfgInputDetailEntity();
					if(dmpCfgInputDetailEntity != null) {
						maxRetryCount = dmpCfgInputDetailEntity.getMaxRetryCount();
					}
					List<DmpInputTaskEntity> beforeDmpInputTaskEntityList = dmpResponse.getBeforeDmpInputTaskEntityList();
					if(CollUtil.isNotEmpty(beforeDmpInputTaskEntityList)) {
						DmpInputTaskEntity dmpInputTaskEntity = beforeDmpInputTaskEntityList.get(0);
						Integer errorCount = dmpInputTaskEntity.getErrorCount() + 1;
						boolean errorFlag = errorCount == maxRetryCount;
						dmpInputTaskService.updateErrorStatus(dmpInputTaskEntity.getId(), errorFlag, errorCount, e);
					}
					throw e;
				}
			}
		}
		return dmpResponse;
	}
	
}
