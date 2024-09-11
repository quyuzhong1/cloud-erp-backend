package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.netflix.client.ClientException;

import cn.hutool.core.date.LocalDateTimeUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputErpPushApiInitHandler extends DmpInputInitHandler{

	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();
        String system = "";
        String extendJson = dmpCfgInputEntity.getExtendJson();
        if(StringUtils.isNotBlank(extendJson)) {
        	JSONObject parseObject = JSON.parseObject(extendJson);
            system = parseObject.getString("system");
        }
        if(StringUtils.isBlank(system)) {
        	throw new ServiceException("dmp_cfg_input的extend_json扩展字段中系统未配置");
        }
        if(StringUtils.isBlank(apiType)) {
        	throw new ServiceException("dmp_cfg_api的api_type接口类型未配置");
        }
        String className = "com.erp.model."+ system +".entity."+ StringUtils.capitalize(system) +"PushMsgEntity";
        Class<BaseEntity> clazz = null;
		try {
			clazz = (Class<BaseEntity>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new ServiceException(className + "实体不存在");
		}
		
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		int i = 0;
		List<BaseEntity> list = null;
		while(list == null) {
			try {
				list = FeignQuery.create(clazz)
						.in("source_type", apiType.split(","))
						.last(" and update_time >= CAST('" + LocalDateTimeUtil.formatNormal(dmpInputTaskEntity.getStartTime()) + 
								"' AS timestamp) AND update_time <= CAST('" + LocalDateTimeUtil.formatNormal(dmpInputTaskEntity.getEndTime()) + "' AS timestamp) ")
						.list();
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if(cause instanceof ClientException && i < 6) {
					try {Thread.sleep(10000);} catch (InterruptedException e1) {}
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
