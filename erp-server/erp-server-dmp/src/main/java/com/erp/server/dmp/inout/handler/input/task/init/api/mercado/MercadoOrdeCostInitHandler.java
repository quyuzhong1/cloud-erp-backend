package com.erp.server.dmp.inout.handler.input.task.init.api.mercado;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.cost.CostDTO;
import com.sdk.oms.mercado.dto.mercado.shipment.ShipmentViewDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
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
public class MercadoOrdeCostInitHandler extends DmpInputInitHandler {
	@Resource
    private MercadoSdkClientService mercadoSdkClientService;

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getId, dmpInputTaskEntity.getParentTaskId()).list();

		if (CollectionUtil.isEmpty(list)) {
			return new ArrayList<>();
		}
		List<DmpInputTaskEntity> parentTaskEntityList = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getId, list.get(0).getParentTaskId()).list();
		if (CollectionUtil.isEmpty(parentTaskEntityList)) {
			return new ArrayList<>();
		}
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

		MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(parentTaskEntityList.get(0).getNextLevelId());
		if (ObjectUtil.isEmpty(shopInfoDTO)) {
			throw new ServiceException("美客多店铺id：" + this.nextLevelId + "未找到对应的店铺信息");
		}

		//平台接口地址
		String url = MercadoConstant.URL;
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);

		for (Map<String, Object> findMongoDatum : findMongoData) {
			Map<String, Object> shipping = (Map<String, Object>)findMongoDatum.get("shipping");

			String path = dmpCfgApiEntity.getApiType().replace("{shippingId}", shipping.get("fid").toString());

			//入参
			HashMap<String, Object> orderParams = new HashMap<>(1);

			//设置请求头
			Map<String, String> orderHeaderMap = new HashMap<>(1);
			orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
			orderHeaderMap.put("x-format-new", "true");

			//拉取数据
			ApiResult shipmentResult = new ApiResult();
			Object data = null;
			long sleepTime = 1000;
			int count = 0;
			while(ObjectUtil.isEmpty(data)) {
				shipmentResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
				if(shipmentResult.getMsg().equalsIgnoreCase("Read timed out")) {
					if(count == 10) {
						throw new ServiceException("调用速卖通" + url + path + "接口重试" + count + "失败");
					}
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {}
					sleepTime = sleepTime + 1000;
					count = count + 1;
				}
				data = shipmentResult.getData();
			}

			if (!Objects.equals(shipmentResult.getCode(), 200) && !Objects.equals(shipmentResult.getCode(), 201)) {
				log.error("调用url={},入参params={}, 美客多费用明细数据失败，返回值 responseMap={}", url + path, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult));
				throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 费用明细请求失败，返回值 responseMap={}",
						url + path, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
			}

			//解析数据
			ObjectMapper objectMapper = new ObjectMapper();
			CostDTO costDTO = null;
			try {
				costDTO = objectMapper.readValue(JSONUtil.toJsonStr(shipmentResult.getData()), CostDTO.class);
			} catch (JsonProcessingException e) {
				log.error("美客多费用明细接口数据解析错误，数据={}", shipmentResult.getData());
				throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 费用明细数据解析失败，返回值 responseMap={}",
						url + path, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
			}
			if (ObjectUtil.isEmpty(costDTO)) {
				return Collections.emptyList();
			}

			DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
			dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(Arrays.asList(costDTO)));
			dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
		}

		return dmpInputTaskInitDTOList;

	}
}
