package com.erp.server.dmp.inout.handler.input.task.init;

import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.order.request.OrderRequest;
import com.sdk.oms.shopee.service.ShopeeReturnService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputShopeeRetrunInitHandler extends DmpInputInitHandler{
	@Resource
	private CfgAppClientService cfgAppClientService;
	@Resource
    private ShopeeReturnService shopeeReturnService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
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
		List<ShopAuthEntity> shopAuthEntityList = FeignQuery.create(ShopAuthEntity.class).eq(ShopAuthEntity::getShopId, nextLevelId).list();
		if(CollUtil.isEmpty(shopAuthEntityList)) {
			throw new ServiceException("shopee授权未配置");
		}
		ShopAuthEntity shopAuthEntity = shopAuthEntityList.get(0);
		long timeFrom = Timestamp.valueOf(dmpInputTaskEntity.getStartTime()).getTime() / 1000;
		long timeTo = Timestamp.valueOf(dmpInputTaskEntity.getEndTime()).getTime() / 1000;
		OrderRequest orderRequest = OrderRequest.builder()
                .host(cfgAppClientEntity.getUrl())
                .offset(0)
                .token(shopAuthEntity.getAccessToken())
                .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                .partnerId(Long.parseLong(cfgAppClientEntity.getClientId()))
                .tmpPartnerKey(cfgAppClientEntity.getClientSecret())
                .timeFrom(timeFrom)
                .timeTo(timeTo)
                .cursor("")
                .build();
		
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		JSONArray item = new JSONArray();
		boolean hasNextPage = true;
		while(hasNextPage) {
			ShopeeResponse data = null;
	    	long sleepTime = 1000;
	    	int count = 0;
	    	while(data == null) {
	    		data = this.execute(orderRequest);
	    		if(data == null) {
	    			if(count == 10) {
	    				throw new ServiceException("调用shopee退货信息接口重试" + count + "失败");
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
	    	hasNextPage = result.getBool("more");
	    	if(hasNextPage) {
	    		orderRequest.setCursor(result.getStr("next_cursor"));
	    	}
	    	item.addAll(result.getJSONArray("return"));
		}
		dmpInputTaskInitDTO.setMsg(item.toJSONString());
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
    
		return dmpInputTaskInitDTOList;
	}
	
	private ShopeeResponse execute(OrderRequest orderRequest){
		ShopeeResponse response = null;
		try {
			response = shopeeReturnService.getReturnList(orderRequest);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if(cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
				return null;
			}
			throw new ServiceException("调用shopee退货信息接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
		}
		
		if(response != null) {
			String error = response.getError();
			if(StringUtils.isNotBlank(error)) {
				throw new ServiceException("调用shopee退货信息接口报错，错误原因：" + response.getMessage());
			}
		}
		
		return response;
	}
}
