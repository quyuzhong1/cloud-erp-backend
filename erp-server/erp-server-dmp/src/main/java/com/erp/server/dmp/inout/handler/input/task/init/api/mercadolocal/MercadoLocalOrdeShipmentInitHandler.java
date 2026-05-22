package com.erp.server.dmp.inout.handler.input.task.init.api.mercadolocal;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.mercadolocal.constant.MercadoConstant;
import com.sdk.oms.mercadolocal.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.shipment.ShipmentViewDTO;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class MercadoLocalOrdeShipmentInitHandler extends DmpInputInitHandler {
	@Resource
	private MercadoLocalSdkClientService mercadoLocalSdkClientService;
	@Resource
	private MercadoLocalRateLimitHelper rateLimitHelper;

	private static final String BIZ_TYPE = MercadoLocalRateLimitHelper.BIZ_SHIPMENT;

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

		MercadoShopInfoDTO shopInfoDTO = mercadoLocalSdkClientService.getShopInfoByShopId(findMongoData.get(0).get("nextLevelId").toString());
		if (ObjectUtil.isEmpty(shopInfoDTO)) {
			throw new ServiceException("美客多店铺id：" + this.nextLevelId + "未找到对应的店铺信息");
		}
		String userId = String.valueOf(shopInfoDTO.getUserId());

		// 入口检查限流退避标记，命中则软退
		if (rateLimitHelper.isLimited(userId, BIZ_TYPE)) {
			log.warn("【美客多本土站-shipments】userId={} 处于限流退避中，跳过本轮 inputTaskId={}",
					userId, dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getId());
			((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
			return Collections.emptyList();
		}

		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

		//平台接口地址
		String url = MercadoConstant.URL;
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);

		for (Map<String, Object> findMongoDatum : findMongoData) {
			Map<String, Object> shipping = (Map<String, Object>)findMongoDatum.get("shipping");

			Object id = shipping.get("id");
			if(Objects.isNull(id)|| id.toString().equals("0")){
				continue;
			}
			String shippingId = id.toString();

			// 优先复用结果缓存（按 inputTaskId 隔离 + 600s 任务生命周期档位）：
			// shipment 状态会变（ready_to_ship → shipped → delivered），但 key 带 inputTaskId 后
			// 跨任务天然 miss，下个调度周期的新 inputTask 会重新拉真实状态；
			// 命中窗口最多就是当前任务从首次软退到最终完成，最坏滞后 ≈ 任务生命周期 + 1 个调度周期。
			String cached = rateLimitHelper.getResultCache(dmpInputTaskEntity.getId(), userId, BIZ_TYPE, shippingId);
			if (StringUtils.isNotBlank(cached)) {
				DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
				dto.setMsg(cached);
				dmpInputTaskInitDTOList.add(dto);
				continue;
			}

			String path = dmpCfgApiEntity.getApiType().replace("{shippingId}", shippingId);

			//入参
			HashMap<String, Object> orderParams = new HashMap<>(1);

			//设置请求头
			Map<String, String> orderHeaderMap = new HashMap<>(1);
			orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
			orderHeaderMap.put("x-format-new", "true");

			ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path,
					JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);

			if (rateLimitHelper.isRateLimitedCode(apiResult.getCode())
					|| (apiResult.getMsg() != null && apiResult.getMsg().equalsIgnoreCase("Read timed out"))) {
				rateLimitHelper.markLimited(userId, BIZ_TYPE, MercadoLocalRateLimitHelper.DEFAULT_BACKOFF_SECONDS);
				log.warn("【美客多本土站-shipments】触发限流/超时，写入退避标记。userId={}, code={}, msg={}, url={}",
						userId, apiResult.getCode(), apiResult.getMsg(), url + path);
				((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
				return Collections.emptyList();
			}

			if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
				log.error("调用url={},入参params={}, 美客多marketplace/shipments数据失败，返回值 responseMap={}", url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult));
				throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
						url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult)));
			}
			if (ObjectUtil.isEmpty(apiResult.getData())) {
				continue;
			}

			//解析数据
			ObjectMapper objectMapper = new ObjectMapper();
			ShipmentViewDTO shipmentViewDTO;
			try {
				shipmentViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ShipmentViewDTO.class);
			} catch (JsonProcessingException e) {
				log.error("美客多shipments/'shippingId'/接口数据解析错误，数据={}", apiResult.getData(), e);
				throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}" ,e.getMessage() +
						url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult)));
			}
			if (ObjectUtil.isEmpty(shipmentViewDTO)) {
				continue;
			}

			String resultJson = JSONArray.toJSONString(Collections.singletonList(shipmentViewDTO));
			DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
			dmpInputTaskInitDTO.setMsg(resultJson);
			dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

			rateLimitHelper.setResultCache(dmpInputTaskEntity.getId(), userId, BIZ_TYPE, shippingId, resultJson,
					MercadoLocalRateLimitHelper.CACHE_SECONDS_TASK_LIFE);
		}

		return dmpInputTaskInitDTOList;

	}


	public static void main(String[] args) {

		String dataJsonString = "{\"gross_amount\":3.99,\"currency_id\":\"USD\",\"receiver\":{\"user_id\":24384856,\"cost\":0,\"compensation\":0,\"save\":0,\"discounts\":[{\"rate\":1,\"type\":\"ratio\",\"promoted_amount\":4.99}],\"compensations\":[]},\"senders\":[{\"user_id\":2198665353,\"cost\":3.99,\"compensation\":0,\"save\":0,\"discounts\":[],\"compensations\":[]}]}";
		ObjectMapper objectMapper = new ObjectMapper();
		ShipmentViewDTO shipmentViewDTO = null;
		try {
			shipmentViewDTO = objectMapper.readValue(dataJsonString, ShipmentViewDTO.class);
		} catch (JsonProcessingException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		System.out.println(shipmentViewDTO);
	}
}
