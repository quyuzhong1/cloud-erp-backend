package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.netflix.client.ClientException;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputErpPushSkuMappingApiInitHandler extends DmpInputInitHandler{

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		int i = 0;
		List<OmsPushMsgEntity> list = null;
		while(list == null) {
			try {
				list = FeignQuery.invokeList(OmsPushMsgEntity.class , "com.erp.server.oms.service.impl.SkuMappingServiceImpl", "syncDataToSdy", 
						Arrays.asList(dmpInputTaskEntity.getStartTime() , dmpInputTaskEntity.getEndTime()));
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if(cause instanceof ClientException && i < 9) {
					try {Thread.sleep(10000);} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}else {
					throw e;
				}
			}
			i = i + 1;
		}
		dmpInputTaskInitDTO.setMsg(JSON.toJSONString(list));
		
		return Collections.singletonList(dmpInputTaskInitDTO);
	}
	
}
