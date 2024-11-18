package com.erp.server.dmp.inout.handler.input.task.init.api.mercado;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.listing.BodyBean;
import com.sdk.oms.mercado.dto.mercado.listing.ListingViewDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class MercadoProductDetailInitHandler extends DmpInputInitHandler {
	@Resource
	private MercadoSdkClientService mercadoSdkClientService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {

		List<Map<String, Object>> findMongoData = null;
		String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
		if (StringUtils.isNotBlank(parentStorageName)) {
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
			findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
		}
		if (findMongoData == null) {
			return new ArrayList<>();
		}

		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

		List<String> productIds = new ArrayList<>();
		for (Map<String, Object> findMongoDatum : findMongoData) {
			List<String> results = (List<String>) findMongoDatum.get("results");
			productIds.addAll(results);
		}


		MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(findMongoData.get(0).get("nextLevelId").toString());
		if (ObjectUtil.isEmpty(shopInfoDTO)) {
			throw new ServiceException("美客多店铺id：" + this.nextLevelId + "未找到对应的店铺信息");
		}

		//平台接口地址
		String url = MercadoConstant.URL;
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);

		String path = dmpCfgApiEntity.getApiType();
		List<List<String>> partition = Lists.partition(productIds, 20);

		for (List<String> list : partition) {

			//入参
			HashMap<String, Object> params = new HashMap<>(1);
			params.put("ids", org.thymeleaf.util.StringUtils.join(list,","));

			//设置请求头
			Map<String, String> headerMap = new HashMap<>(1);
			headerMap.put("Authorization", "Bearer "+ shopInfoDTO.getAccessToken());

			//拉取数据
			ApiResult apiResult = new ApiResult();
			Object data = null;
			long sleepTime = 1000;
			int count = 0;
			while(ObjectUtil.isEmpty(data)) {
				apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
				if(apiResult.getMsg().equalsIgnoreCase("Read timed out")) {
					if(count == 10) {
						throw new ServiceException("调用美客多" + url + path + "接口重试" + count + "失败");
					}
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					sleepTime = sleepTime + 1000;
					count = count + 1;
				}
				data = apiResult.getData();
			}

			if (!Objects.equals(apiResult.getCode(), 200)) {
				log.error("调用url={},入参params={}, 美客多Listing数据失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
				throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多Listing数据失败，返回值 responseMap={}",
						url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
			}

			ObjectMapper objectMapper = new ObjectMapper();
			List<ListingViewDTO> dataList = null;
			try {
				dataList = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), new TypeReference<List<ListingViewDTO>>() {});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
				throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
						url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
			}
			if(CollectionUtil.isEmpty(dataList)){
				break;
			}

			List<BodyBean> collect = dataList.stream().map(req -> req.getBody()).collect(Collectors.toList());
			DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
			dmpInputTaskInitDTO.setMsg(JSON.toJSONString(collect));
			dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
		}

		return dmpInputTaskInitDTOList;

	}
}
