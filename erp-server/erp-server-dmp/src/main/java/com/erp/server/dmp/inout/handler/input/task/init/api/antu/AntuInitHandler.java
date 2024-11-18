package com.erp.server.dmp.inout.handler.input.task.init.api.antu;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.wms.antu.dto.request.AntuGetProductReq;
import com.sdk.wms.antu.dto.response.AntuResponse;
import com.sdk.wms.antu.utils.AntuUtils;

import cn.hutool.core.collection.CollUtil;


/**
 * dmp输入init任务基础处理器下的安兔api获取数据方式
 */
@Service
@Scope("prototype")
public class AntuInitHandler extends DmpInputInitHandler {

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<DmpInputTaskInitDTO> resultList = new ArrayList<>();
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
		String apiType = dmpCfgApiEntity.getApiType();

		AntuGetProductReq antuGetProductReq = new AntuGetProductReq();
		Integer page = 1;
		antuGetProductReq.setPageSize(100);
		int currTotal = 0;
		List<Object> allResult = new ArrayList<>();
		List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.ANTU.getCode()));
		if(CollUtil.isEmpty(overseasProviderEntityList)) {
			return Collections.emptyList();
		}
		for (OverseasProviderEntity overseasProviderEntity : overseasProviderEntityList) {

			if (overseasProviderEntity.getEnableDate().compareTo(LocalDate.now()) > 0) {
				continue;
			}
			ThirdWarehouseContext.setAuthMap(overseasProviderEntity.getAuthJson());
			while(true) {
				antuGetProductReq.setPage(page);
				String response = AntuUtils.callService(apiType, antuGetProductReq);
				AntuResponse<List<?>> result = JSONObject.parseObject(response,new TypeReference<AntuResponse<List<Object>>>() {}.getType());
				List<?> data = result.getData();
				int size = data.size();
				if(size == 0) {
					break;
				}
				allResult.addAll(data);
				currTotal = currTotal + size;
				Integer count = result.getCount();
				if(count == null) {
					count  = 0;
				}
				if(currTotal >= count) {
					break;
				}
				page = page + 1;
			}
			String id = overseasProviderEntity.getId();
			DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
			JSONArray parseArray = JSON.parseArray(JSONObject.toJSONString(allResult));
			parseArray.forEach(p -> {
				JSONObject j = (JSONObject)p;
				j.put("authId", id);
			});
			dmpInputTaskInitDTO.setMsg(parseArray.toJSONString());

			resultList.add(dmpInputTaskInitDTO);
		}

		return resultList;
	}




}
