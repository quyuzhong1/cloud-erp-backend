package com.erp.server.dmp.inout.handler.input.task.init.api.mercado;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.cost.CostDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import com.sdk.oms.mercado.dto.mercado.shipment.ShipmentViewDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
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
public class MercadoOrderDetailInitHandler extends DmpInputInitHandler {
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
		if (CollectionUtil.isEmpty(findMongoData)) {
			return new ArrayList<>();
		}

		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

		List<String> orderIds = new ArrayList<>();
		for (Map<String, Object> findMongoDatum : findMongoData) {
			Object orders = findMongoDatum.get("orders");
			if (ObjectUtil.isNotEmpty(orders)) {
				List<Object> objectsList = (List<Object>) orders;
				for (Object o : objectsList) {
					Map<String, Object> map = (Map<String, Object>) o;
					orderIds.add(String.valueOf(map.get("fid")));
				}
			}
		}

		MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(findMongoData.get(0).get("nextLevelId").toString());
		if (ObjectUtil.isEmpty(shopInfoDTO)) {
			throw new ServiceException("美客多店铺id：" + this.nextLevelId + "未找到对应的店铺信息");
		}

		//平台接口地址
		String url = MercadoConstant.URL;
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);


		for (String id : orderIds) {
			String path = dmpCfgApiEntity.getApiType().replace("{id}", id);

			//入参
			HashMap<String, Object> orderParams = new HashMap<>(1);

			//设置请求头
			Map<String, String> orderHeaderMap = new HashMap<>(1);
			orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
//			https://api.mercadolibre.com/marketplace/orders/2000007633674134
			//拉取数据
			ApiResult apiResult = new ApiResult();
			Object data = null;
			long sleepTime = 1000;
			int count = 0;
			while(ObjectUtil.isEmpty(data)) {
				apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
				if(apiResult.getMsg().equalsIgnoreCase("Read timed out")) {
					if(count == 10) {
						throw new ServiceException("调用美客多" + url + path + "接口重试" + count + "失败");
					}
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {}
					sleepTime = sleepTime + 1000;
					count = count + 1;
				}
				data = apiResult.getData();
			}

			if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
				continue;
			}

			//解析数据
			com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO orderViewDTO = null;
			ObjectMapper objectMapperBase = new ObjectMapper();
			try {
				orderViewDTO = objectMapperBase.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderViewDTO.class);
			} catch (JsonProcessingException e) {
				log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult.getData()));
				throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
						url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult.getData())));
			}

			if (ObjectUtil.isEmpty(orderViewDTO)) {
				return Collections.emptyList();
			}

			DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
			dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(Arrays.asList(orderViewDTO)));
			dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
		}

		return dmpInputTaskInitDTOList;

	}
}
