package com.erp.server.dmp.inout.handler.input.task.finish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputFinishRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDmpHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;

import cn.hutool.core.collection.CollUtil;

@Service
public abstract class DmpInputFinishHandler extends DmpInputTaskHandler{
	@Override
	public void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputFinishRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpInputFinishResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpInputFinishRequest) dmpRequest, (DmpInputFinishResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputFinishRequest dmpRequest, DmpInputFinishResponse dmpResponse, DmpHandlerChain chain) {
		updateTaskStatus = DmpInputTaskStatusEnum.FINISH;
		this.beforeToDoStatus(dmpRequest, dmpResponse);
		if(!this.isNextStatus(dmpResponse)) {
			this.dealConvertInputDmpBaseEntityListMaps(dmpResponse);
			Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpResponse.getConvertInputDmpBaseEntityListMaps();
			if(convertInputDmpBaseEntityListMaps != null && convertInputDmpBaseEntityListMaps.size() > 0) {
				dealDmpToFinish(dmpRequest, dmpResponse);
			}else {
				this.dealDmpInputMongoBaseEntityList(dmpResponse);
				Map<DmpCfgInputConvertEntity, List<Map<String , Object>>> convertInputMongoEntityListMaps = dmpResponse.getConvertInputMongoEntityListMaps();
				if(convertInputMongoEntityListMaps != null && convertInputMongoEntityListMaps.size() > 0) {
					dealMongoToFinish(dmpRequest, dmpResponse);
				}else {
					this.dealConvertInputTaskFileEntityListMaps(dmpResponse);
					Map<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = dmpResponse.getConvertInputTaskFileEntityListMaps();
					if(convertInputTaskFileEntityListMaps != null && convertInputTaskFileEntityListMaps.size() > 0) {
						dealFdsToFinish(dmpRequest, dmpResponse);
					}else {
						Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = dmpResponse.getConvertInputTaskInitDTOListMaps();
						if(convertInputTaskInitDTOListMaps != null && convertInputTaskInitDTOListMaps.size() > 0) {
							dealInitToFinish(dmpRequest, dmpResponse);
						}else {
							dealNoneToFinish(dmpRequest, dmpResponse);
						}
					}
				}
			}
		}
		this.afterToDoStatus(dmpRequest, dmpResponse);
		
		DmpOutputFinishRequest dmpOutputFinishRequest = new DmpOutputFinishRequest();
		dmpOutputFinishRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
		dmpOutputFinishRequest.setConvertInputTaskFileEntityListMaps(dmpResponse.getConvertInputTaskFileEntityListMaps());
		dmpOutputFinishRequest.setConvertInputMongoEntityListMaps(dmpResponse.getConvertInputMongoEntityListMaps());
		dmpOutputFinishRequest.setConvertInputDmpBaseEntityListMaps(dmpResponse.getConvertInputDmpBaseEntityListMaps());
		this.doBaseChain(dmpRequest, dmpResponse, chain, dmpOutputFinishRequest);
	}
	
	public abstract void dealDmpToFinish(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse);
	public abstract void dealMongoToFinish(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse);
	public abstract void dealFdsToFinish(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse);
	public abstract void dealInitToFinish(DmpInputDmpRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract void dealNoneToFinish(DmpInputDmpRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
