package com.erp.server.dmp.inout.handler.input.task.init.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;

import cn.hutool.core.exceptions.ExceptionUtil;

/**
 * 速卖通海外托管商品接口返回结构兼容精简和非精简两种包装。
 */
public final class AliExpressOverseasManagedProductHelper {

	public static final String LOCAL_SERVICE = "LOCAL_SERVICE";
	public static final String SELLER_RELATION_API = "global.seller.relation.query";
	public static final String PRODUCT_LIST_API = "aliexpress.local.service.products.list";
	public static final String PRODUCT_DETAIL_API = "aliexpress.local.service.product.query";
	public static final String PRODUCT_LIST_RESPONSE_KEY = "aliexpress_local_service_products_list_response";
	public static final String PRODUCT_DETAIL_RESPONSE_KEY = "aliexpress_local_service_product_query_response";
	public static final String SELLER_RELATION_RESPONSE_KEY = "global_seller_relation_query_response";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private AliExpressOverseasManagedProductHelper() {
	}

	public static SellerRelation resolveSellerRelation(IopClient client, String token) {
		IopRequest request = new IopRequest();
		request.setApiName(SELLER_RELATION_API);
		request.addApiParameter("business_type", LOCAL_SERVICE);
		request.addApiParameter("simplify", "true");
		IopResponse response = execute(client, request, token, SELLER_RELATION_API);
		JSONObject payload = unwrap(response.getBody(), SELLER_RELATION_RESPONSE_KEY);
		String channelSellerId = findString(payload, "channel_seller_id", "channelSellerId");
		String channel = findString(payload, "channel");
		if (StringUtils.isBlank(channelSellerId) || StringUtils.isBlank(channel)) {
			throw new ServiceException("调用速卖通" + SELLER_RELATION_API + "接口未返回channel或channel_seller_id，响应：" + response.getBody());
		}
		return new SellerRelation(channel, channelSellerId);
	}

	public static JSONObject unwrap(String body, String responseKey) {
		if (StringUtils.isBlank(body)) {
			return new JSONObject();
		}
		JSONObject root = JSONObject.parseObject(body);
		JSONObject payload = root.getJSONObject(responseKey);
		if (payload == null) {
			payload = root;
		}
		putIfAbsent(payload, "code", root.getString("code"));
		putIfAbsent(payload, "request_id", root.getString("request_id"));
		return payload;
	}

	public static JSONObject unwrapResult(String body, String responseKey) {
		JSONObject payload = unwrap(body, responseKey);
		JSONObject result = payload.getJSONObject("result");
		if (result != null) {
			return result;
		}
		return payload;
	}

	public static JSONObject unwrapData(String body, String responseKey) {
		JSONObject result = unwrapResult(body, responseKey);
		JSONObject data = result.getJSONObject("data");
		if (data != null) {
			return data;
		}
		return result;
	}

	public static IopResponse execute(IopClient client, IopRequest request, String token, String apiType) {
		try {
			return client.execute(request, token, Protocol.TOP);
		} catch (ApiException e) {
			throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
		}
	}

	public static void assertSuccess(JSONObject payload, String apiType) {
		if (payload == null) {
			throw new ServiceException("调用速卖通" + apiType + "接口报错，接口返回为空");
		}
		String code = payload.getString("code");
		String errorCode = firstNotBlank(payload.getString("errorCode"), payload.getString("error_code"));
		String errorMessage = firstNotBlank(payload.getString("errorMessage"), payload.getString("error_message"), payload.getString("error_desc"));
		Object success = firstNotNull(payload.get("success"), payload.get("result_success"));
		boolean successFlag = success == null || (!Boolean.FALSE.equals(success) && !"false".equalsIgnoreCase(String.valueOf(success)));
		if (StringUtils.isNotBlank(code) && !"0".equals(code)) {
			throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + payload.toJSONString());
		}
		if (StringUtils.isNotBlank(errorCode) || !successFlag) {
			throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + firstNotBlank(errorMessage, payload.toJSONString()));
		}
	}

	public static String formatTime(LocalDateTime time) {
		if (time == null) {
			return "";
		}
		return time.format(DATE_TIME_FORMATTER);
	}

	public static String findString(JSONObject payload, String... keys) {
		Object value = findValue(payload, keys);
		return value == null ? "" : String.valueOf(value);
	}

	public static Object findValue(Object data, String... keys) {
		if (data == null || keys == null || keys.length == 0) {
			return null;
		}
		if (data instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) data;
			for (String key : keys) {
				Object value = jsonObject.get(key);
				if (value != null) {
					return value;
				}
			}
			for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
				Object value = findValue(entry.getValue(), keys);
				if (value != null) {
					return value;
				}
			}
		} else if (data instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) data;
			for (Object item : jsonArray) {
				Object value = findValue(item, keys);
				if (value != null) {
					return value;
				}
			}
		}
		return null;
	}

	public static JSONArray findArray(Object data, String... keys) {
		Object value = findValue(data, keys);
		if (value instanceof JSONArray) {
			return (JSONArray) value;
		}
		if (value instanceof JSONObject) {
			JSONArray array = new JSONArray();
			array.add(value);
			return array;
		}
		return new JSONArray();
	}

	public static JSONObject findObject(Object data, String... keys) {
		Object value = findValue(data, keys);
		if (value instanceof JSONObject) {
			return (JSONObject) value;
		}
		return null;
	}

	public static String joinImages(JSONObject detail, JSONObject sku) {
		JSONArray mainImages = new JSONArray();
		JSONObject skuMultimedia = sku.getJSONObject("multimedia");
		if (skuMultimedia != null) {
			mainImages = skuMultimedia.getJSONArray("main_image_list");
		}
		if (mainImages.isEmpty()) {
			JSONObject productMultimedia = findObject(detail, "multimedia");
			if (productMultimedia != null) {
				mainImages = productMultimedia.getJSONArray("main_image_list");
			}
		}
		if (mainImages.isEmpty()) {
			mainImages = findArray(detail, "main_image_list", "mainImageList");
		}
		List<String> images = new ArrayList<>();
		for (Object image : mainImages) {
			if (image == null) {
				continue;
			}
			if (image instanceof JSONObject) {
				String url = findString((JSONObject) image, "url", "image_url", "imageUrl");
				if (StringUtils.isNotBlank(url)) {
					images.add(url);
				}
			} else {
				images.add(String.valueOf(image));
			}
		}
		return images.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(";"));
	}

	public static String joinSkuProperties(JSONObject sku) {
		JSONArray propertyList = sku.getJSONArray("sku_property_list");
		if (propertyList == null) {
			propertyList = sku.getJSONArray("skuPropertyList");
		}
		if (propertyList == null) {
			return "";
		}
		List<String> specs = new ArrayList<>();
		for (Object property : propertyList) {
			if (!(property instanceof JSONObject)) {
				continue;
			}
			JSONObject jsonObject = (JSONObject) property;
			String name = findString(jsonObject, "sku_property_name", "skuPropertyName", "property_name", "propertyName");
			String value = findString(jsonObject, "sku_property_value", "skuPropertyValue", "property_value", "propertyValue");
			if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
				specs.add(name + ":" + value);
			} else if (StringUtils.isNotBlank(name)) {
				specs.add(name);
			} else if (StringUtils.isNotBlank(value)) {
				specs.add(value);
			}
		}
		return String.join(";", specs);
	}

	public static String mapStatus(String status) {
		if ("active".equalsIgnoreCase(status)) {
			return "在售";
		}
		if ("inactive".equalsIgnoreCase(status)) {
			return "停售";
		}
		return status;
	}

	private static void putIfAbsent(JSONObject payload, String key, Object value) {
		if (payload != null && !payload.containsKey(key) && value != null) {
			payload.put(key, value);
		}
	}

	private static String firstNotBlank(String... values) {
		if (values == null) {
			return "";
		}
		for (String value : values) {
			if (StringUtils.isNotBlank(value)) {
				return value;
			}
		}
		return "";
	}

	private static Object firstNotNull(Object... values) {
		if (values == null) {
			return null;
		}
		for (Object value : values) {
			if (Objects.nonNull(value)) {
				return value;
			}
		}
		return null;
	}

	public static class SellerRelation {
		private final String channel;
		private final String channelSellerId;

		public SellerRelation(String channel, String channelSellerId) {
			this.channel = channel;
			this.channelSellerId = channelSellerId;
		}

		public String getChannel() {
			return channel;
		}

		public String getChannelSellerId() {
			return channelSellerId;
		}
	}
}
