package com.erp.server.dmp.inout.handler.input.task.init.api.tiktok;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.constant.DmpInputConstant;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.OrderDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrdersBean;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@Scope("prototype")
public class TikTokOrderApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private TikTokSdkClientService tikTokSdkClientService;


    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
    	List<DmpInputTaskInitDTO> dealOrderIdQuery = dealOrderIdQuery(dmpInputApiInitRequest);
    	if(dealOrderIdQuery != null) {
    		return dealOrderIdQuery;
    	}
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();


        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();


        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("TikTok店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }

        List<OrdersBean> resultsBeanList = new ArrayList<>();

        //每次最多获取200条
        Integer pageSize = 100;
        //分页token
        String pageToken = "";
        //平台接口地址
        String url = TikTokConstant.URL;
        //服务密钥
        String toktikInfo = "8ff628de24faf70c24855de4d967fb6a17a47e3f";

        String platformOrderCreateTime = "";
        String taskExtendJson = dmpInputApiInitRequest.getTaskExtendJson();
        if(StringUtils.isNotBlank(taskExtendJson)) {
        	JSONObject parseObject = JSON.parseObject(taskExtendJson);
            if(parseObject != null) {
            	platformOrderCreateTime = parseObject.getString(DmpInputConstant.PLATFORM_ORDER_CREATE_TIME);
            }
        }
        while (true) {
            StringBuffer sb = new StringBuffer();
            //组装授权url
            String path = dmpInputApiInitRequest.getApiType().replace("{version}", TikTokConstant.VERSION);

            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", "6buinkjt3hmld");
            params.put("page_size", pageSize);
            params.put("page_token", pageToken);
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            params.put("sign", "");
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, Object> bodyMap = new HashMap<>();
            if(StringUtils.isNotBlank(platformOrderCreateTime)) {
            	bodyMap.put("create_time_ge", Long.valueOf(platformOrderCreateTime) - 5);
                bodyMap.put("create_time_lt", Long.valueOf(platformOrderCreateTime) + 5);
            }else {
            	bodyMap.put("update_time_ge", dmpInputApiInitRequest.getStartTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli() / 1000);
                bodyMap.put("update_time_lt", dmpInputApiInitRequest.getEndTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli() / 1000);
            }

            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
            //加入sign签名入参
            params.put("sign", sign);

            //组装url
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
            sb.append("&app_key=" + "6buinkjt3hmld" + "");
            sb.append("&page_size=" + pageSize + "");
            sb.append("&page_token=" + pageToken + "");
            sb.append("&shop_cipher=" + shopInfoDTO.getShopCipher() + "");
            sb.append("&shop_id=");
            sb.append("&sign=" + sign + "");
            sb.append("&timestamp=" + timestamp + "");
            sb.append("&version=" + TikTokConstant.VERSION + "");

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单数据失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            OrderDTO orderDTO = null;
            try {
                orderDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            } catch (JsonProcessingException e) {
                log.error("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询订单数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            if (CollectionUtil.isEmpty(orderDTO.getData().getOrders())) {
                break;
            }

            //获取到所有客户的产品id
//            List<com.sdk.oms.tiktok.dto.tiktok.order.OrdersBean> ordersBeans = orderDTO.getData().getOrders().stream()
//                    .filter(req -> !"UNPAID".equalsIgnoreCase(req.getStatus())
//                            && !"ON_HOLD".equalsIgnoreCase(req.getStatus())
//                    ).distinct().collect(Collectors.toList());
            List<com.sdk.oms.tiktok.dto.tiktok.order.OrdersBean> ordersBeans = orderDTO.getData().getOrders();


            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(ordersBeans));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

            pageToken = orderDTO.getData().getNextPageToken();

            if (StringUtil.isBlank(pageToken)) {
                break;
            }

        }
        return dmpInputTaskInitDTOList;
    }
    
    private List<DmpInputTaskInitDTO> dealOrderIdQuery(DmpInputApiInitRequest dmpInputApiInitRequest) {
    	String extendJson = dmpInputApiInitRequest.getTaskExtendJson();
    	if(StringUtils.isBlank(extendJson)) {
            return null;
        }
        JSONObject parseObject = JSON.parseObject(extendJson);
        if(null == parseObject) {
            return null;
        }
        JSONArray jsonArray = parseObject.getJSONArray(DmpInputConstant.ORDER_ID_LIST);
        if (CollectionUtils.isEmpty(jsonArray)){
            return null;
        }
     	List<String> orderIdList = jsonArray.stream()
            .map(Object::toString)
            .collect(Collectors.toList());
     	parseObject.remove(DmpInputConstant.ORDER_ID_LIST);
     	
     	String nextLevelId = dmpInputApiInitRequest.getNextLevelId();

     	TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("TikTok店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }
     	
        String secret = shopInfoDTO.getClientSecret();
		String url = TikTokConstant.URL;
		//组装授权url
		String path = "/order/"+ TikTokConstant.VERSION +"/orders";
    	Map<String, Object> params = new HashMap<>();
		params.put("access_token", shopInfoDTO.getAccessToken());
		params.put("app_key", shopInfoDTO.getClientId());
		String ids = StringUtil.join(orderIdList, ",");
		params.put("ids", ids);
		params.put("shop_cipher", shopInfoDTO.getShopCipher());
		params.put("shop_id", "");
		String timestamp = System.currentTimeMillis() / 1000 + "";
		params.put("timestamp", timestamp);
		params.put("version", TikTokConstant.VERSION);

		//设置请求头
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("content-type", "multipart/form-data");
		headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

		String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, "");
		// 追加请求路径获取签名
		String sign = EncryptionUtils.generateSHA256(input, secret);
		//加入sign签名入参
		params.put("sign", sign);

		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
		//拉取数据
		ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url+path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
		String jsonStr = JSONUtil.toJsonStr(apiResult.getData());
        JSONObject resultJson = JSON.parseObject(jsonStr);
        JSONObject resultData = resultJson.getJSONObject("data");
        JSONArray orders = resultData.getJSONArray("orders");
        if(CollUtil.isNotEmpty(orders)) {
        	for(Object order : orders) {
        		JSONObject orderJSONObject = (JSONObject) order;
        		Long create_time = orderJSONObject.getLong("create_time");
        		parseObject.put(DmpInputConstant.PLATFORM_ORDER_CREATE_TIME, create_time);
            	dmpInputApiInitRequest.setTaskExtendJson(parseObject.toJSONString());
            	dmpInputTaskInitDTOList.addAll(this.getApiData(dmpInputApiInitRequest));
        	}
        }else {
        	log.warn("{}未查询到数据，返回报文：{}" , ids , jsonStr);
        }
        
     	return dmpInputTaskInitDTOList;
    }
    
    public static void main(String[] args) {
		TikTokShopInfoDTO shopInfoDTO = JSON.parseObject("{\r\n" + 
				"  \"@type\": \"com.sdk.oms.tiktok.dto.TikTokShopInfoDTO\",\r\n" + 
				"  \"accessToken\": \"ROW_agxMJAAAAACj-JAAAriAWjVtF2MrUIFdPkU8v2PwmslxLKqRdTTD6LC69bn3X7wtL1fvhLtlULbb9xoOJGkUBexq78KD43AkOaiNHMBhmGQF1fEt3fHZNP9_hnY8ogPjtHZZHW55s-Py3n61yNmzVBG4iau5YpUr6vJI1Q8ojleOIOPsQyg3Bj0jVRZIzztDUliJpXwVfb8\",\r\n" + 
				"  \"baseUrl\": \"https://auth.tiktok-shops.com\",\r\n" + 
				"  \"clientId\": \"6buinkjt3hmld\",\r\n" + 
				"  \"clientSecret\": \"8ff628de24faf70c24855de4d967fb6a17a47e3f\",\r\n" + 
				"  \"id\": \"1820403424249143297\",\r\n" + 
				"  \"sellerType\": \"CROSS_BORDER\",\r\n" + 
				"  \"shopCipher\": \"TTP_pEhpJwAAAADvOkDJ2jIoaS9Uak191t0d\",\r\n" + 
				"  \"site\": \"US\"\r\n" + 
				"}", TikTokShopInfoDTO.class);
		
		String orderId = "577271495219450812,577295695216152974";
		
		String secret = shopInfoDTO.getClientSecret();
		String url = TikTokConstant.URL;
		//组装授权url
		String path = "/order/202309/orders";
    	Map<String, Object> params = new HashMap<>();
		params.put("access_token", shopInfoDTO.getAccessToken());
		params.put("app_key", shopInfoDTO.getClientId());
		params.put("ids", orderId);
		params.put("shop_cipher", shopInfoDTO.getShopCipher());
		params.put("shop_id", "");
		String timestamp = System.currentTimeMillis() / 1000 + "";
		params.put("timestamp", timestamp);
		params.put("version", TikTokConstant.VERSION);

		//设置请求头
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("content-type", "multipart/form-data");
		headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

		String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, "");
		// 追加请求路径获取签名
		String sign = EncryptionUtils.generateSHA256(input, secret);
		//加入sign签名入参
		params.put("sign", sign);

		//拉取数据
		ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url+path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
		String jsonStr = JSONUtil.toJsonStr(apiResult.getData());
        JSONObject resultJson = JSON.parseObject(jsonStr);
        JSONObject resultData = resultJson.getJSONObject("data");
        JSONArray orders = resultData.getJSONArray("orders");
        if(CollUtil.isNotEmpty(orders)) {
        	for(Object order : orders) {
        		JSONObject orderJSONObject = (JSONObject) order;
        		Long create_time = orderJSONObject.getLong("create_time");
        		System.out.println(create_time);
        	}
        }else {
        	log.warn("{}未查询到数据，返回报文：{}" , orderId , jsonStr);
        }
	}
}
