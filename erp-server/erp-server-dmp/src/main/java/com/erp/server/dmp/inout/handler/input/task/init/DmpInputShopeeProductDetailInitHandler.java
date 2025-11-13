package com.erp.server.dmp.inout.handler.input.task.init;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.CfgAppClientService;
import com.google.common.collect.Lists;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.product.request.ProductRequest;
import com.sdk.oms.shopee.service.ShopeeProductService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputShopeeProductDetailInitHandler extends DmpInputInitHandler{
	@Resource
	private CfgAppClientService cfgAppClientService;
	@Resource
    private ShopeeProductService shopeeProductService;
	
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
		
		List<Object> itemIds = findMongoData.stream().map(f -> f.get("item_id")).collect(Collectors.toList());
		if(CollUtil.isEmpty(itemIds)) {
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
		
		JSONArray jsonArray = new JSONArray();
		ProductRequest productRequest = ProductRequest.builder()
                .host(cfgAppClientEntity.getUrl())
                .offset(0)
                .token(shopAuthEntity.getAccessToken())
                .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                .partnerId(Long.parseLong(cfgAppClientEntity.getClientId()))
                .tmpPartnerKey(cfgAppClientEntity.getClientSecret())
                .timeFrom(null)
                .timeTo(null)
                .build();
		
		List<List<Object>> partition = Lists.partition(itemIds, 50);

		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		ShopeeResponse data;
    	long sleepTime;
    	int count;
		for(List<Object> p : partition) {
			productRequest.setItemIdList(StringUtils.join(p, ","));
			
	    	data = null;
	    	sleepTime = 1000;
	    	count = 0;
	    	while(data == null) {
	    		data = this.execute(productRequest);
	    		if(data == null) {
	    			if(count == 10) {
	    				throw new ServiceException("调用shopee产品明细接口重试" + count + "失败");
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
	        
	    	JSONObject result = data.getResponse();
	        
			JSONArray item_list = result.getJSONArray("item_list");
			if(CollUtil.isNotEmpty(item_list)) {
				jsonArray.addAll(item_list);
			}
		}
		
		List<JSONObject> resultList = new ArrayList<>();
		if(CollUtil.isNotEmpty(jsonArray)) {
			for(Object object : jsonArray) {
				JSONObject json = (JSONObject)object;
				Long itemId = json.getLong("item_id");
				productRequest.setItemId(itemId);
				data = null;
				sleepTime = 1000;
		    	count = 0;
		    	while(data == null) {
		    		data = this.executeModel(productRequest);
		    		if(data == null) {
		    			if(count == 10) {
		    				throw new ServiceException("调用shopee产品明细接口重试" + count + "失败");
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
		    	JSONObject modelResult = data.getResponse();
		    	JSONArray modelList = modelResult.getJSONArray("model");
		    	json.set("dmp_model_list", modelList);
		    	resultList.add(json);
			}
		}
		dmpInputTaskInitDTO.setMsg(JSONUtil.toJsonStr(resultList));
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
    
		return dmpInputTaskInitDTOList;
	}
	
	private ShopeeResponse execute(ProductRequest productRequest){
		ShopeeResponse response = null;
		try {
			response = shopeeProductService.getProductItemBaseInfo(productRequest);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if(cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
				return null;
			}
			throw new ServiceException("调用shopee产品明细接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
		}
		
		if(response != null) {
			String error = response.getError();
			if(StringUtils.isNotBlank(error)) {
				throw new ServiceException("调用shopee产品明细接口报错，错误原因：" + response.getMessage());
			}
		}
		
		return response;
	}
	
	private ShopeeResponse executeModel(ProductRequest productRequest){
		ShopeeResponse response = null;
		try {
			response = shopeeProductService.getModelListInfo(productRequest);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if(cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
				return null;
			}
			throw new ServiceException("调用shopee产品模型接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
		}
		
		if(response != null) {
			String error = response.getError();
			if(StringUtils.isNotBlank(error)) {
				throw new ServiceException("调用shopee产品模型接口报错，错误原因：" + response.getMessage());
			}
		}
		
		return response;
	}
}
