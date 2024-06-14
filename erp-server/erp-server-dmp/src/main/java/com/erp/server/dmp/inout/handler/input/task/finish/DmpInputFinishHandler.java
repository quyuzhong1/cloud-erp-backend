package com.erp.server.dmp.inout.handler.input.task.finish;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputDmpBaseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;

import cn.hutool.core.collection.CollUtil;

@Service
public abstract class DmpInputFinishHandler extends DmpInputHandler{
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
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
		List<DmpInputDmpBaseEntity> dmpInputDmpBaseEntityList = dmpResponse.getDmpInputDmpBaseEntityList();
		if(CollUtil.isNotEmpty(dmpInputDmpBaseEntityList)) {
			dealDmpToFinish(dmpRequest, dmpResponse);
		}else {
			List<Map> dmpInputMongoEntityList = dmpResponse.getDmpInputMongoEntityList();
			if(CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
				dealMongoToFinish(dmpRequest, dmpResponse);
			}else {
				List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = dmpResponse.getDmpInputTaskFileEntityList();
				if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
					dealFdsToFinish(dmpRequest, dmpResponse);
				}else {
					List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = dmpResponse.getDmpInputTaskInitDTOList();
					if(CollUtil.isNotEmpty(dmpInputTaskInitDTOList)) {
						dealInitToFinish(dmpRequest, dmpResponse);
					}else {
						dealNoneToFinish(dmpRequest, dmpResponse);
					}
				}
			}
		}
		
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	public abstract void dealDmpToFinish(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse);
	public abstract void dealMongoToFinish(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse);
	public abstract void dealFdsToFinish(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse);
	public abstract void dealInitToFinish(DmpInputDmpRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract void dealNoneToFinish(DmpInputDmpRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
