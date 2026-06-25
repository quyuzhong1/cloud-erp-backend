package com.erp.server.dmp.inout.handler.input.task.init.api.mercadolocal;

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
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercadolocal.dto.MercadoShopInfoDTO;
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
public class MercadoLocalOrderShipmentHistoryInitHandler extends DmpInputInitHandler {
	@Resource
	private MercadoLocalSdkClientService mercadoLocalSdkClientService;
	@Resource
	private MercadoLocalRateLimitHelper rateLimitHelper;

	private static final String BIZ_TYPE = MercadoLocalRateLimitHelper.BIZ_SHIPMENT_HISTORY;

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
            log.warn("【美客多本土站-shipments history】userId={} 处于限流退避中，跳过本轮 inputTaskId={}",
                    userId, dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getId());
            ((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
            return Collections.emptyList();
        }

        List<JSONObject> resultData = new ArrayList<>();

		//平台接口地址
		String url = MercadoConstant.URL;
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);

		for (Map<String, Object> findMongoDatum : findMongoData) {
            Map<String, Object> shipping = (Map<String, Object>)findMongoDatum.get("shipping");

            String id = shipping.getOrDefault("id", "").toString();
            if(StringUtils.isBlank(id) || "0".equals(id)){
                continue;
            }

            // 优先复用结果缓存（按 inputTaskId 隔离：仅在当前任务软退后下次重试命中，跨任务不复用）
            String cached = rateLimitHelper.getResultCache(dmpInputTaskEntity.getId(), userId, BIZ_TYPE, id);
            if (StringUtils.isNotBlank(cached)) {
                List<JSONObject> cachedList = JSONArray.parseArray(cached, JSONObject.class);
                if (CollectionUtil.isNotEmpty(cachedList)) {
                    resultData.addAll(cachedList);
                }
                continue;
            }

            String path = dmpCfgApiEntity.getApiType().replace("{shippingId}", id);

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
				log.warn("【美客多本土站-shipments history】触发限流/超时，写入退避标记。userId={}, code={}, msg={}, url={}",
						userId, apiResult.getCode(), apiResult.getMsg(), url + path);
				((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
				return Collections.emptyList();
			}

			if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
				log.error("调用url={},入参params={}, 美客多marketplace/shipments数据失败，返回值 responseMap={}", url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult));
				throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
						url + path, orderParams.toString(), JSONUtil.toJsonStr(apiResult)));
			}
            if (null == apiResult.getData()){
                continue;
            }

			List<JSONObject> curJsonList = JSONArray.parseArray(apiResult.getData().toString())
                    .stream()
                    .map(e -> fillDataJsonObject(e, id)).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(curJsonList)) {
                continue;
            }
            resultData.addAll(curJsonList);

            rateLimitHelper.setResultCache(dmpInputTaskEntity.getId(), userId, BIZ_TYPE, id, JSON.toJSONString(curJsonList),
                    MercadoLocalRateLimitHelper.CACHE_SECONDS_STABLE);
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(resultData)));
	}

	/**
	 * 设置fid
	 */
	private JSONObject fillDataJsonObject(Object dataObj, String shipmentFid) {
		JSONObject json = (JSONObject) JSON.toJSON(dataObj);
		json.put("shipmentFid", shipmentFid);
		return json;
	}
}
