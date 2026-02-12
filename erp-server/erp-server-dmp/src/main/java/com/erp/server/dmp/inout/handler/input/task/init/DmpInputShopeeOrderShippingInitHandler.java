package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.CfgAppClientService;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.service.ShopeeLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
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
public class DmpInputShopeeOrderShippingInitHandler extends DmpInputInitHandler{
	@Resource
	private CfgAppClientService cfgAppClientService;
	@Resource
    private ShopeeLogisticsService shopeeLogisticsService;

	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<Map<String, Object>> findMongoData = null;
		String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
		if(StringUtils.isNotBlank(parentStorageName)) {
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
			findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
		}
		if(CollUtil.isEmpty(findMongoData)) {
			return new ArrayList<>();
		}

		List<String> orderSnList = findMongoData.stream().map(f -> f.get("order_sn").toString()).collect(Collectors.toList());
		List<ParamData> detailParamDataList = new ArrayList<>();
		detailParamDataList.add(new ParamData("order_sn", "order_sn", PannoEnum.IN, orderSnList));
		List<Map<String, Object>> detailMongoData = mongoService.findMongoData(detailParamDataList, "Shopee_orderDetail_data");
		if(CollUtil.isEmpty(detailMongoData)) {
			log.warn("没有订单详情数据，订单SN列表：{}", orderSnList);
			return new ArrayList<>();
		}
		//过滤出订单状态是READY_TO_SHIP的订单
		List<String> readyToShipOrderSnList = detailMongoData.stream().filter(f -> "READY_TO_SHIP".equals(f.get("order_status"))).map(f -> f.get("order_sn").toString()).collect(Collectors.toList());
		if(CollUtil.isEmpty(readyToShipOrderSnList)) {
			log.warn("没有READY_TO_SHIP状态的订单，订单SN列表：{}", orderSnList);
			return new ArrayList<>();
		}
		AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
		List<CfgAppClientEntity> cfgAppClientEntityList = cfgAppClientService.lambdaQuery()
			.eq(CfgAppClientEntity::getBusinessType, appClientEnum.getBusinessType())
			.eq(CfgAppClientEntity::getDictPlatform, appClientEnum.getPlatform())
			.eq(CfgAppClientEntity::getPlatformType, appClientEnum.getPlatformType())
			.list();
		if(CollUtil.isEmpty(cfgAppClientEntityList)) {
			throw new ServiceException("shopee应用未配置");
		}
		
		CfgAppClientEntity cfgAppClientEntity = cfgAppClientEntityList.get(0);
		List<ShopAuthEntity> shopAuthEntityList = FeignQuery.create(ShopAuthEntity.class).eq(ShopAuthEntity::getShopId, findMongoData.get(0).get("nextLevelId").toString()).list();
		if(CollUtil.isEmpty(shopAuthEntityList)) {
			throw new ServiceException("shopee授权未配置");
		}
		ShopAuthEntity shopAuthEntity = shopAuthEntityList.get(0);
		BaseRequest baseRequest = BaseRequest.builder()
				.partnerKey(cfgAppClientEntity.getClientSecret())
				.partnerId(Long.parseLong(cfgAppClientEntity.getClientId()))
				.shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
				.accessToken(shopAuthEntity.getAccessToken())
				.host(cfgAppClientEntity.getUrl())
				.build();
		ValidatorUtil.validateEntity(baseRequest);
		
		// 结果resultList
		List<JSONObject> resultList = new LinkedList<>();

		for (String orderSn : readyToShipOrderSnList) {
			JSONObject data = null;
			long sleepTime = 1000;
			int count = 0;
			while(data == null) {
				data = this.execute(baseRequest, orderSn);
				if(data == null) {
					if(count == 10) {
						throw new ServiceException("调用shopee订单配送信息接口重试" + count + "失败");
					}
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					sleepTime = sleepTime + 1000;
					count = count + 1;
				}
			}
			JSONObject result = setOrderSnAndToJsonObject(data, orderSn, shopAuthEntity.getShopId());
			resultList.add(result);
		}
		return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(resultList)));
	}

	/**
	 * 执行请求
	 */
	private JSONObject execute(BaseRequest orderRequest, String orderSn){
		try {
			return shopeeLogisticsService.requestShippingParameter(orderRequest, orderSn, "");
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if(cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
				return null;
			}
			throw new ServiceException("调用shopee订单配送信息报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
		}

	}


	/**
	 * 设置订单SN和转换JSON
	 */
	private JSONObject setOrderSnAndToJsonObject(JSONObject json, String orderSn, String currentShopId) {
		json.put("order_sn", orderSn);
		json.put("shopId", currentShopId);
		return json;
	}
}
