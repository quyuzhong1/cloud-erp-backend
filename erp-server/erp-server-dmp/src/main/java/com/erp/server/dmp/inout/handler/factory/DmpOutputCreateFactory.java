package com.erp.server.dmp.inout.handler.factory;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.wrapper.QueryParam;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputHistoryCreateHandler;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputHotfixCreateHandler;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputInputCreateHandler;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputNormalCreateHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 输出任务创建工厂，添加handler给handler链路执行
 * @author Administrator
 *
 */
@Component
@Slf4j
public class DmpOutputCreateFactory{
	@Autowired
	private DmpOutputInputCreateHandler dmpOutputInputCreateHandler;
	@Autowired
	private DmpOutputHotfixCreateHandler dmpOutputHotfixCreateHandler;
	@Autowired
	private DmpOutputTaskFactory dmpOutputTaskFactory;
	@Autowired
	private DmpHandlerCache dmpHandlerCache;
	@Resource
	private DmpOutputNormalCreateHandler dmpOutputNormalCreateHandler;
	@Resource
	private DmpOutputHistoryCreateHandler dmpOutputHistoryCreateHandler;

	/**
	 * 创建输入任务类型输出任务
	 * @param dmpOutputCreateRequest
	 */
	public DmpOutputCreateResponse createInputOutputTask(DmpOutputCreateRequest dmpRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputInputCreateHandler);
		DmpOutputCreateResponse dmpResponse = new DmpOutputCreateResponse();
		bean.doDmpHandler(dmpRequest, dmpResponse);
		return dmpResponse;
	}
	
	/**
	 * 创建输入任务类型立马执行
	 * @param dmpRequest
	 */
	@Transactional(rollbackFor = Exception.class)
	public DmpOutputTaskResponse doInputOutputTask(DmpOutputInputCreateRequest dmpRequest) {
		DmpOutputCreateResponse dmpOutputCreateResponse = this.createInputOutputTask(dmpRequest);
		DmpOutputTaskRequest dmpOutputTaskRequest = dmpRequest.getDmpOutputTaskRequest();
		DmpOutputTaskEntity dmpOutputTaskEntity = dmpOutputCreateResponse.getAfterDmpOutputTaskEntityList().get(0);
		dmpOutputTaskRequest.setOutputTaskId(dmpOutputTaskEntity.getId());
		dmpOutputTaskRequest.setExecTimeout(dmpOutputTaskEntity.getExecTimeout());
		DmpOutputTaskResponse dmpOutputTaskResponse = dmpOutputTaskFactory.dealOutputTask(dmpOutputTaskRequest);
		return dmpOutputTaskResponse;
	}
	
	/**
	 * 创建输入任务类型输出任务
	 * @param dmpOutputCreateRequest
	 */
	public DmpOutputCreateResponse createHotfixOutputTask(DmpOutputCreateRequest dmpRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputHotfixCreateHandler);
		DmpOutputCreateResponse dmpResponse = new DmpOutputCreateResponse();
		bean.doDmpHandler(dmpRequest, dmpResponse);
		return dmpResponse;
	}
	
	/**
	 * 创建快速任务类型立马执行
	 * @param dmpRequest
	 */
	@Transactional(rollbackFor = Exception.class)
	public DmpOutputTaskResponse doHotfixOutputTask(DmpOutputHotfixCreateRequest dmpRequest) {
		dmpRequest.setThrowException(true);
		String cfgOutputId = dmpRequest.getCfgOutputId();
		DmpOutputCreateResponse dmpOutputCreateResponse = this.createHotfixOutputTask(dmpRequest);
		DmpOutputTaskRequest dmpOutputTaskRequest = new DmpOutputTaskRequest();
		DmpOutputTaskEntity dmpOutputTaskEntity = dmpOutputCreateResponse.getAfterDmpOutputTaskEntityList().get(0);
		dmpOutputTaskRequest.setNotValidate(true);
		dmpOutputTaskRequest.setOutputTaskId(dmpOutputTaskEntity.getId());
		dmpOutputTaskRequest.setExecTimeout(dmpOutputTaskEntity.getExecTimeout());
		
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpOutputCreateResponse.getDmpCfgOutputEntity();
		String inputConvertId = dmpCfgOutputEntity.getInputConvertId();
		DmpCfgInputConvertEntity dmpCfgInputConvertEntity = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getId().equals(inputConvertId)).get(0);
		String inputStatus = dmpCfgInputConvertEntity.getInputStatus();
		if(DmpInputTaskStatusEnum.FDS.getCode().equals(inputStatus)) {
			throw new ServiceException("推送fds数据未实现");
		}else if(DmpInputTaskStatusEnum.MONGO.getCode().equals(inputStatus)) {
			throw new ServiceException("推送mongo数据未实现");
		}else if(DmpInputTaskStatusEnum.DMP.getCode().equals(inputStatus)) {
			List<QueryParam> queryParams = dmpRequest.getQueryParams();
			QueryWrapper<?> queryWrapper = QueryParam.getQueryWrapper(queryParams);
			List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache
					.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(dmpCfgInputConvertEntity.getMainId()) && d.getInputStatus().equals(inputStatus));
			dmpCfgInputConvertEntityList.sort((d1 , d2) -> d1.getOrder().compareTo(d2.getOrder()));
			
			DmpCfgInputConvertEntity mainDmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(0);
			ServiceImpl serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(mainDmpCfgInputConvertEntity.getStorageName(), true) + "ServiceImpl" , ServiceImpl.class);
			List<BaseEntity> list = serviceImpl.list(queryWrapper);
			dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().put(mainDmpCfgInputConvertEntity, list);
			dmpOutputTaskRequest.getChangeConvertInputDmpBaseEntityListMaps().put(mainDmpCfgInputConvertEntity, list);
			if(CollUtil.isNotEmpty(list)) {
				if("1801575677567136779".equals(cfgOutputId)) {
					List<String> soOutStockIds = null;
					for(int i = 1; i < dmpCfgInputConvertEntityList.size(); i++) {
						DmpCfgInputConvertEntity childDmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(i);
						String storageName = childDmpCfgInputConvertEntity.getStorageName();
						serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(storageName, true) + "ServiceImpl" , ServiceImpl.class);
						QueryWrapper<?> wrapper = new QueryWrapper<>();
						if("dmp_so_outStock".equals(storageName)) {
							wrapper.in("source_id", list.stream().map(BaseEntity::getId).collect(Collectors.toList()));
						}else if("dmp_so_outStock_detail".equals(storageName)){
							if(CollUtil.isEmpty(soOutStockIds)) {
								continue;
							}
							wrapper.in("main_id", soOutStockIds);
						}else {
							wrapper.in("main_id", list.stream().map(BaseEntity::getId).collect(Collectors.toList()));
						}
						List<BaseEntity> childEntityList = serviceImpl.list(wrapper);
						if("dmp_so_outStock".equals(storageName)) {
							soOutStockIds = childEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
						}
						dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
						dmpOutputTaskRequest.getChangeConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
					}
				}else {
					for(int i = 1; i < dmpCfgInputConvertEntityList.size(); i++) {
						DmpCfgInputConvertEntity childDmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(i);
						serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(childDmpCfgInputConvertEntity.getStorageName(), true) + "ServiceImpl" , ServiceImpl.class);
						QueryWrapper<?> wrapper = new QueryWrapper<>();
						wrapper.in("main_id", list.stream().map(BaseEntity::getId).collect(Collectors.toList()));
						List<BaseEntity> childEntityList = serviceImpl.list(wrapper);
						dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
						dmpOutputTaskRequest.getChangeConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
					}
				}
			}
		}
		
		DmpOutputTaskResponse dmpOutputTaskResponse = dmpOutputTaskFactory.dealOutputTask(dmpOutputTaskRequest);
		return dmpOutputTaskResponse;
	}


	/**
	 * 创建正常任务
	 */
	public void createNormalOutputTask(DmpOutputCreateRequest dmpOutputCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputNormalCreateHandler);
		bean.doDmpHandler(dmpOutputCreateRequest, new DmpOutputCreateResponse());
	}

	/**
	 * 创建历史任务
	 */
	public void createHistoryOutputTask(DmpOutputCreateRequest dmpOutputCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputHistoryCreateHandler);
		bean.doDmpHandler(dmpOutputCreateRequest, new DmpOutputCreateResponse());
	}
}
