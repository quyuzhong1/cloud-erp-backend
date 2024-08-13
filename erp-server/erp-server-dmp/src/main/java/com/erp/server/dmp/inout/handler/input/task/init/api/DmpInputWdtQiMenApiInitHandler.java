package com.erp.server.dmp.inout.handler.input.task.init.api;

import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.sdk.third.qimen.QiMenClientService;
import com.sdk.third.qimen.config.QiMenUtils;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoRequest;
import com.taobao.api.TaobaoResponse;

import cn.hutool.core.exceptions.ExceptionUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputWdtQiMenApiInitHandler implements DmpInputApiInitHandler{

	@Resource
    private QiMenClientService qimenService;
	
	@Override
	public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputWdtApiInitRequest) {
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        
		SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
		
		String apiType = dmpInputWdtApiInitRequest.getApiType();
		String className = "com.qimencloud.api.scene3ldsmu02o9.request.Wdt" + StrUtils.underlineToCamel(apiType.replace(".", "_"), false) + "Request";
		
		try {
			Class<?> requestClass = Class.forName(className);
			TaobaoRequest request = (TaobaoRequest)requestClass.newInstance();
			Method requestMethod = requestClass.getMethod("setTargetAppKey", String.class);
			requestMethod.invoke(request, qimenService.getTargetAppKey());
			requestMethod = requestClass.getMethod("setWdtAppkey", String.class);
			requestMethod.invoke(request, qimenService.getWdtAppKey());
			requestMethod = requestClass.getMethod("setWdtSalt", String.class);
			requestMethod.invoke(request, qimenService.getWdtSalt());
			requestMethod = requestClass.getMethod("putOtherTextParam", String.class , String.class);
			requestMethod.invoke(request, qimenService.getCustomerIdKey(), qimenService.getCustomerIdValue());
			
			String requestParam = dmpInputWdtApiInitRequest.getRequestParam();
			JSONObject parseObject = JSON.parseObject(requestParam);
			if("wms.stockin.PreStockin.search".equals(apiType)) {
				parseObject.put("mtFrom", dmpInputWdtApiInitRequest.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				parseObject.put("mtTo", dmpInputWdtApiInitRequest.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			}else if("setting.Shop.queryShop".equals(apiType) || "setting.Warehouse.queryWarehouse".equals(apiType)){
				
			}else {
				parseObject.put("startTime", dmpInputWdtApiInitRequest.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				parseObject.put("endTime", dmpInputWdtApiInitRequest.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));	
			}
			Class<?> paramsClass = Class.forName(className + "$Params");
			Object params = paramsClass.newInstance();
			for(Map.Entry<String , Object> parse: parseObject.entrySet()) {
				Object value = parse.getValue();
				Method paramsMethod = null;
				try {
					paramsMethod = paramsClass.getMethod("set" + StringUtils.capitalize(parse.getKey()), value.getClass());
				} catch (NoSuchMethodException e) {
					if(value instanceof Integer) {
						paramsMethod = paramsClass.getMethod("set" + StringUtils.capitalize(parse.getKey()), Long.class);
						value = Long.valueOf((Integer) value);
					}else {
						throw e;
					}
				}
				if(paramsMethod != null) {
					paramsMethod.invoke(params, value);
				}
			}
			requestMethod = requestClass.getMethod("setParams", paramsClass);
			requestMethod.invoke(request, params);
			
			Long pageSize = parseObject.getLong("pageSize");
			if(pageSize == null || pageSize <= 0) {
				pageSize = 200L;
			}
			Class<?> pagerClass = Class.forName(className + "$Pager");
			Object pager = pagerClass.newInstance();
			Method pagerMethod = pagerClass.getMethod("setPageSize", Long.class);
			pagerMethod.invoke(pager, pageSize);
			pagerMethod = pagerClass.getMethod("setPageNo", Long.class);
			long pageNo = 1;
			
			int currTotal = 0;
			while(true) {
				requestMethod = requestClass.getMethod("setDatetime", String.class);
				requestMethod.invoke(request, qimenService.format(new Date()));
				
				pagerMethod.invoke(pager, pageNo);
				requestMethod = requestClass.getMethod("setPager", pagerClass);
				requestMethod.invoke(request, pager);
				
				requestMethod = requestClass.getMethod("setWdtSign", String.class);
				requestMethod.invoke(request, QiMenUtils.getNewQimenCustomWdtSign(request, qimenService.getWdtSecret()));
				
				TaobaoResponse execute = null;
				try {
					execute = qimenService.execute(request);
				} catch (ApiException e) {
					throw new ServiceException("调用旺店通奇门" + apiType + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
				}
				JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(execute , config, SerializerFeature.PrettyFormat));
				Integer status = jsonObject.getInteger("status");
				if(status == null || status != 0) {
					String message = jsonObject.getString("sub_message");
					if(StringUtils.isBlank(message)) {
						message = jsonObject.getString("msg");
					}
					throw new ServiceException("调用旺店通奇门" + apiType + "接口报错，错误原因：" + message);
				}
				
				JSONObject data = jsonObject.getJSONObject("data");
				
				Integer total = data.getInteger("total_count");
				JSONArray order = data.getJSONArray("order");
				if(order == null) {
					order = data.getJSONArray("details");
				}
				
				currTotal = currTotal + order.size();
				DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
				dmpInputTaskInitDTO.setMsg(order.toJSONString());
				dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
				if(currTotal >= total) {
					break;
				}
				pageNo = pageNo + 1;
			}
		}catch (ServiceException e) {
			throw e;
		}catch (Exception e) {
			throw new ServiceException("调用旺店通奇门" + apiType + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
		}
		
		return dmpInputTaskInitDTOList;
	}

}
