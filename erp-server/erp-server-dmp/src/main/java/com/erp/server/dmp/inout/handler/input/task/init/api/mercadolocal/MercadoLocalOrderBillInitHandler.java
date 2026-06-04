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
import com.sdk.oms.mercadolocal.dto.mercadolocal.shipment.BillViewDTO;
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
public class MercadoLocalOrderBillInitHandler extends DmpInputInitHandler {
	@Resource
	private MercadoLocalSdkClientService mercadoLocalSdkClientService;
	@Resource
	private MercadoLocalRateLimitHelper rateLimitHelper;

	private static final String BIZ_TYPE = MercadoLocalRateLimitHelper.BIZ_ORDER_BILLING;

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

		// 入口检查限流退避标记，命中则软退，让任务等下次调度再来，不去打 API 加剧限流
		if (rateLimitHelper.isLimited(userId, BIZ_TYPE)) {
			log.warn("【美客多本土站-账单查询】userId={} 处于限流退避中，跳过本轮 inputTaskId={}",
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
			String fid = findMongoDatum.get("fid").toString();

			// 优先复用结果缓存（按 inputTaskId 隔离 + 600s 任务生命周期档位）：
			// 账单 status pending → paid 会变，但 key 带 inputTaskId 后跨任务天然 miss，
			// 下个调度周期的新 inputTask 会重新拉真实状态；命中窗口 = 当前任务生命周期。
			String cached = rateLimitHelper.getResultCache(dmpInputTaskEntity.getId(), userId, BIZ_TYPE, fid);
			if (StringUtils.isNotBlank(cached)) {
				DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
				dto.setMsg(cached);
				dmpInputTaskInitDTOList.add(dto);
				continue;
			}

			String path = dmpCfgApiEntity.getApiType().replace("{order_id}", fid);

			//入参
			HashMap<String, Object> orderParams = new HashMap<>(1);

			//设置请求头
			Map<String, String> orderHeaderMap = new HashMap<>(2);
			orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
			orderHeaderMap.put("x-version", "2");

			//拉取数据（fail-fast，去掉内嵌 sleep 重试，让限流码统一走退避）
			ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path,
					JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);

			// 限流 / 网关临时不可用 / Read timed out → 写退避标记 + 软退，任务下次调度再来
			if (rateLimitHelper.isRateLimitedCode(apiResult.getCode())
					|| (apiResult.getMsg() != null && apiResult.getMsg().equalsIgnoreCase("Read timed out"))) {
				rateLimitHelper.markLimited(userId, BIZ_TYPE, MercadoLocalRateLimitHelper.DEFAULT_BACKOFF_SECONDS);
				log.warn("【美客多本土站-账单查询】触发限流/超时，写入退避标记。userId={}, code={}, msg={}, url={}",
						userId, apiResult.getCode(), apiResult.getMsg(), url + path);
				((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
				return Collections.emptyList();
			}

			if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
				log.error("调用url={},入参params={}, 美客多本地站查询账单信息数据失败，返回值 responseMap={}", url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult));
				throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
						url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult)));
			}
			if (ObjectUtil.isEmpty(apiResult.getData())) {
				continue;
			}

			//解析数据
			ObjectMapper objectMapper = new ObjectMapper();
			BillViewDTO shipmentViewDTO;
			try {
				shipmentViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), BillViewDTO.class);
			} catch (JsonProcessingException e) {
				log.error("美客多本地站查询账单信息接口数据解析错误，数据={}", apiResult.getData());
				throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}" ,e.getMessage() +
						url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult)));
			}
			if (ObjectUtil.isEmpty(shipmentViewDTO)) {
				continue;
			}
			shipmentViewDTO.setFid(fid);

			String resultJson = JSONArray.toJSONString(Collections.singletonList(shipmentViewDTO));
			DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
			dmpInputTaskInitDTO.setMsg(resultJson);
			dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

			rateLimitHelper.setResultCache(dmpInputTaskEntity.getId(), userId, BIZ_TYPE, fid, resultJson,
					MercadoLocalRateLimitHelper.CACHE_SECONDS_TASK_LIFE);
		}

		return dmpInputTaskInitDTOList;

	}
}
