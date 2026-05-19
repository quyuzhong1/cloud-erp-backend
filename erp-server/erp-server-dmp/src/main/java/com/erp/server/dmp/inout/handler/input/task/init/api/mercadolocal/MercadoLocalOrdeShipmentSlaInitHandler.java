package com.erp.server.dmp.inout.handler.input.task.init.api.mercadolocal;

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
import com.erp.model.dmp.entity.DmpInputTaskEntity;
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
import com.sdk.oms.mercadolocal.dto.mercadolocal.shipment.ShipmentSlaViewDTO;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
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
public class MercadoLocalOrdeShipmentSlaInitHandler extends DmpInputInitHandler {
	@Resource
	private MercadoLocalSdkClientService mercadoLocalSdkClientService;
	@Resource
	private MercadoLocalRateLimitHelper rateLimitHelper;

	private static final String BIZ_TYPE = MercadoLocalRateLimitHelper.BIZ_SHIPMENT_SLA;

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

		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, dmpInputTaskEntity.getParentTaskId()).list();
		DmpInputTaskEntity dmpInputTaskEntity = list.stream().filter(req -> "1899659842348408323".equals(req.getCfgInputId())).findFirst().orElse(null);
		//查询shipment数据
		List<Map<String, Object>> shipmentMongoList = null;
		if(Objects.nonNull(dmpInputTaskEntity)){
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getId()));
			shipmentMongoList = mongoService.findMongoData(paramDataList, "mercadolibreLocal_shipment_data");
		}
		if (CollectionUtil.isEmpty(shipmentMongoList)) {
			return new ArrayList<>();
		}

		Map<Object, Map<String, Object>> shipmentMongoListMap = shipmentMongoList.stream().collect(Collectors.toMap(e -> e.get("fid"), e -> e));

		MercadoShopInfoDTO shopInfoDTO = mercadoLocalSdkClientService.getShopInfoByShopId(findMongoData.get(0).get("nextLevelId").toString());
		if (ObjectUtil.isEmpty(shopInfoDTO)) {
			throw new ServiceException("美客多店铺id：" + this.nextLevelId + "未找到对应的店铺信息");
		}
		String userId = String.valueOf(shopInfoDTO.getUserId());

		// 入口检查限流退避标记，命中则软退
		if (rateLimitHelper.isLimited(userId, BIZ_TYPE)) {
			log.warn("【美客多本土站-shipments SLA】userId={} 处于限流退避中，跳过本轮 inputTaskId={}",
					userId, this.dmpInputTaskEntity == null ? null : this.dmpInputTaskEntity.getId());
			((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
			return Collections.emptyList();
		}

		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

		//平台接口地址
		String url = MercadoConstant.URL;
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);

		for (Map<String, Object> findMongoDatum : findMongoData) {
			Object statusObj = findMongoDatum.get("status");
			if(Objects.isNull(statusObj)){
				continue;
			}

			if(String.valueOf(statusObj).equalsIgnoreCase("cancelled")){
				continue;
			}

			Map<String, Object> shipping = (Map<String, Object>)findMongoDatum.get("shipping");
			if(Objects.isNull(shipping)){
				continue;
			}
			Object fid = shipping.get("id");
			if(Objects.isNull(fid) || fid.toString().equals("0")){
				continue;
			}
			String shippingId = fid.toString();

			Map<String, Object>  shipmentMap = shipmentMongoListMap.getOrDefault(fid, null);
			if(Objects.isNull(shipmentMap)){
				continue;
			}

			Map<String, Object> logistic = (Map<String, Object>) shipmentMap.get("logistic");
			if(logistic.get("type").equals("fulfillment")){
				continue;
			}

			// 优先复用结果缓存（按 inputTaskId 隔离：仅在当前任务软退后下次重试命中，跨任务不复用）
			// 注意上面的 dmpInputTaskEntity 是局部变量遮蔽，这里要用 this. 引用当前任务实体
			String cached = rateLimitHelper.getResultCache(this.dmpInputTaskEntity.getId(), userId, BIZ_TYPE, shippingId);
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
				log.warn("【美客多本土站-shipments SLA】触发限流/超时，写入退避标记。userId={}, code={}, msg={}, url={}",
						userId, apiResult.getCode(), apiResult.getMsg(), url + path);
				((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
				return Collections.emptyList();
			}

			if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
				log.error("调用url={},入参params={}, 美客多shipments/'shippingId'/sla数据失败，返回值 responseMap={}", url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult));
				throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
						url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult)));
			}
			if (ObjectUtil.isEmpty(apiResult.getData())) {
				continue;
			}
			//解析数据
			ObjectMapper objectMapper = new ObjectMapper();
			ShipmentSlaViewDTO shipmentViewDTO;
			try {
				shipmentViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ShipmentSlaViewDTO.class);
			} catch (JsonProcessingException e) {
				log.error("美客多shipments/'shippingId'/sla接口数据解析错误，数据={}", apiResult.getData(), e);
				throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}" ,e.getMessage() +
						url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult)));
			}
			if (ObjectUtil.isEmpty(shipmentViewDTO)) {
				continue;
			}
			//设置shipmentId 后续用于关联查询
			shipmentViewDTO.setFid(Long.valueOf(shippingId));

			String resultJson = JSONArray.toJSONString(Collections.singletonList(shipmentViewDTO));
			DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
			dmpInputTaskInitDTO.setMsg(resultJson);
			dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

			rateLimitHelper.setResultCache(this.dmpInputTaskEntity.getId(), userId, BIZ_TYPE, shippingId, resultJson,
					MercadoLocalRateLimitHelper.CACHE_SECONDS_STABLE);
		}

		return dmpInputTaskInitDTOList;

	}
}
