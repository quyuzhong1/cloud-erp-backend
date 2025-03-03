package com.erp.server.dmp.inout.handler.input.task.init.api.spt;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * dmp输入init任务基础处理器下的速派通api获取数据方式
 */
@Slf4j
@Service
@Scope("prototype")
public class SptInitHandler extends DmpInputInitHandler {

	public static final String SOURCE_PLATFORM = "sourcePlatform";
	public static final String AUTH_ID = "authId";

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
		List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.SPT.getCode()));
		if(CollUtil.isEmpty(overseasProviderEntityList)) {
			return Collections.emptyList();
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if(null == overseasProviderEntity) {
			throw new ServiceException("速派通对应授权ID信息不存在");
		}

			if (overseasProviderEntity.getEnableDate().isAfter(LocalDate.now())) {
				return Collections.emptyList();
			}
			ThirdWarehouseContext.setAuthMap(overseasProviderEntity.getAuthJson());
			while(true) {
				antuGetProductReq.setPage(page);
				String response = AntuUtils.callService(getPlatForm(),apiType, antuGetProductReq);
				log.info(getPlatForm().getName()+"（"+apiType+"）api接口返回数据：{}", response);
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
				j.put(AUTH_ID, id);
				j.put(SOURCE_PLATFORM, DmpBasicSystemCodeEnum.SPT.getCode());
			});
			dmpInputTaskInitDTO.setMsg(parseArray.toJSONString());

			resultList.add(dmpInputTaskInitDTO);

		return resultList;
	}

	private static OmsPlatformEnum getPlatForm() {
		return OmsPlatformEnum.OMS_SPT;
	}



}
