package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputDmpResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.inout.handler.output.task.dmp.DmpOutputDmpHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;

@Service
public abstract class DmpInputDmpHandler extends DmpInputTaskHandler{
	
	protected Set<String> uniqueFieldSet = new HashSet<>();
	protected boolean allFieldFlag;
	protected String storageName;
	protected String entityClassName;
	protected Class<? extends BaseEntity> dmpEntityClass;
	protected Set<String> entityFieldNameSet;
	protected ServiceImpl dmpEntityServiceImpl;
	
	protected static final String INPUT_TASK_ID = "input_task_id";
	protected static final String CONVERT_ID = "convert_id";
	protected static final String NEXT_LEVEL_ID = "next_level_id";
	protected static final String UNIQUE_ENCRYPT = "unique_encrypt";
	protected static final String DATA_ENCRYPT = "data_encrypt";
	
	@Override
	public void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputDmpRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpInputDmpResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpInputDmpRequest) dmpRequest, (DmpInputDmpResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputDmpRequest dmpRequest, DmpInputDmpResponse dmpResponse, DmpHandlerChain chain) {
		allFieldFlag = DmpHandlerUtils.getAllFieldFlag(dmpCfgInputConvertEntity.getUniqueFieldName(), uniqueFieldSet);
		storageName = dmpCfgInputConvertEntity.getStorageName();
		entityClassName = "com.erp.model.dmp.entity." + StrUtils.underlineToCamel(storageName, false) + "Entity";
		try {
			dmpEntityClass = (Class<? extends BaseEntity>) Class.forName(entityClassName);
		} catch (ClassNotFoundException e) {
			throw new ServiceException(entityClassName + "类不存在，异常信息：" + ExceptionUtil.stacktraceToString(e));
		}
		entityFieldNameSet = Stream.of(dmpEntityClass.getDeclaredFields()).filter(f -> f.getAnnotation(TableField.class) != null).map(Field::getName).collect(Collectors.toSet());
		dmpEntityServiceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(storageName, true) + "ServiceImpl" , ServiceImpl.class);
		
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		wrapper.eq(CONVERT_ID, convertId);
		List<BaseEntity> dmpInputDmpBaseEntityList = dmpEntityServiceImpl.list(wrapper);
		
		if(CollUtil.isNotEmpty(dmpInputDmpBaseEntityList)) {
			String status = dmpResponse.getBeforeDmpInputTaskEntityList().get(0).getStatus();
			if(DmpInputTaskStatusEnum.DMP.getCode().equals(status) || DmpInputTaskStatusEnum.FINISH.getCode().equals(status)) {
				dmpResponse.setDoUpdateStatus(false);
				dmpResponse.setDoOutputChain(false);
			}
		}else {
			Map<DmpCfgInputConvertEntity, List<Map>> convertInputMongoEntityListMaps = dmpResponse.getConvertInputMongoEntityListMaps();
			if(convertInputMongoEntityListMaps != null && convertInputMongoEntityListMaps.size() > 0) {
				dmpInputDmpBaseEntityList = convertMongoToDmp(dmpRequest, dmpResponse);
			}else {
				Map<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = dmpResponse.getConvertInputTaskFileEntityListMaps();
				if(convertInputTaskFileEntityListMaps != null && convertInputTaskFileEntityListMaps.size() > 0) {
					dmpInputDmpBaseEntityList = convertFdsToDmp(dmpRequest, dmpResponse);
				}else {
					Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = dmpResponse.getConvertInputTaskInitDTOListMaps();
					if(convertInputTaskInitDTOListMaps != null && convertInputTaskInitDTOListMaps.size() > 0) {
						dmpInputDmpBaseEntityList = convertInitToDmp(dmpRequest, dmpResponse);
					}else {
						dmpInputDmpBaseEntityList = convertNoneToDmp(dmpRequest, dmpResponse);
					}
				}
			}
		}
		
		dmpResponse.getConvertInputDmpBaseEntityListMaps().put(dmpCfgInputConvertEntity, dmpInputDmpBaseEntityList);
		
		if(dmpResponse.isDoOutputChain()) {
			List<String> outputClassList = this.getOutputClassList(dmpRequest, dmpResponse);
			if(CollUtil.isNotEmpty(outputClassList)) {
				for(String outputClass : outputClassList) {
					DmpOutputDmpHandler dmpHandlerBean = this.getDmpHandlerBean(outputClass, DmpOutputDmpHandler.class);
					DmpOutputDmpRequest dmpOutputDmpRequest = new DmpOutputDmpRequest();
					dmpOutputDmpRequest.setDoNextChain(false);
					dmpOutputDmpRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
					dmpOutputDmpRequest.setConvertInputTaskFileEntityListMaps(dmpResponse.getConvertInputTaskFileEntityListMaps());
					dmpOutputDmpRequest.setConvertInputMongoEntityListMaps(dmpResponse.getConvertInputMongoEntityListMaps());
					dmpOutputDmpRequest.setConvertInputDmpBaseEntityListMaps(dmpResponse.getConvertInputDmpBaseEntityListMaps());
					dmpHandlerBean.doDmpHandler(dmpOutputDmpRequest, new DmpOutputDmpResponse(), chain);
				}
			}
		}
		
		if(dmpResponse.isDoUpdateStatus()) {
			this.updateTaskStatus(DmpInputTaskStatusEnum.DMP);
		}
		
		if(dmpResponse.isDoNextChain()) {
			dmpResponse.setDoUpdateStatus(true);
			dmpResponse.setDoOutputChain(true);
			chain.doDmpHandler(dmpRequest, dmpResponse);
		}
	}
	
	public abstract List<BaseEntity> convertMongoToDmp(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse);
	public abstract List<BaseEntity> convertFdsToDmp(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse);
	public abstract List<BaseEntity> convertInitToDmp(DmpInputDmpRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract List<BaseEntity> convertNoneToDmp(DmpInputDmpRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
