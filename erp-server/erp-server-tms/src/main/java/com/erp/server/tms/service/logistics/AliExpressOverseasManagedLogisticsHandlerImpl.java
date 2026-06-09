package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.order.response.LogisticsServiceDTO;
import com.erp.tms.aliexpress.model.query.response.OverseasManagedShippingServiceResponse;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 速卖通海外托管物流渠道
 */
@Slf4j
@Component
@RefreshScope
@LogisticsPlatformType(LogisticsPlatformEnum.ALI_EXPRESS_OVERSEAS_MANAGED)
public class AliExpressOverseasManagedLogisticsHandlerImpl extends AbstractLogisticsHandler {
    private static final String LOCAL_SERVICE = "LOCAL_SERVICE";
    private static final String ORDER_ID = "orderId";
    private static final String CHILD_ORDER_ID = "childOrderId";
    private static final String TOP_USER_KEY = "topUserKey";
    private static final String SELLER_ID = "sellerId";
    private static final String OVERSEAS_MANAGED_RESPONSE_KEY = "aliexpress_asf_local_supply_shipping_service_get_response";
    private static final String SELLER_RELATION_RESPONSE_KEY = "global_seller_relation_query_response";
    private static final String LOGISTICS_SERVICE_RESPONSE_KEY = "aliexpress_logistics_redefining_listlogisticsservice_response";

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private AliExpressShipperService aliExpressShipperService;
    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Value("${tms.AliExpressOverseasManaged.orderId:}")
    private String overseasManagedOrderId;

    @Value("${tms.AliExpressOverseasManaged.childOrderId:}")
    private String overseasManagedChildOrderId;

    @Value("${tms.AliExpress.orderId:}")
    private String aliExpressOrderId;

    @Value("${tms.AliExpress.childOrderId:}")
    private String aliExpressChildOrderId;

    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        ApiResult<List<ShopAuthEntity>> authShops = shopInfoFeign.getAuthShopByPlatformType(getPlatForm().getCode());
        if (Objects.isNull(authShops) || !authShops.isSuccess() || CollUtil.isEmpty(authShops.getData())) {
            return Collections.emptyList();
        }
        CfgAppClientEntity cfgAppClient = getAliExpressLogisticsClient();
        if (Objects.isNull(cfgAppClient)) return Collections.emptyList();

        List<Map<String, String>> mapList = new ArrayList<>(authShops.getData().size());
        authShops.getData().forEach(shopAuthEntity -> {
            Map<String, String> map = buildAuthMap(cfgAppClient, shopAuthEntity.getShopId(), getShopToken(shopAuthEntity));
            map.put("shopName", shopAuthEntity.getShopName());
            mapList.add(map);
        });
        return mapList;
    }

    /**
     * 速卖通海外托管复用速卖通物流 AppClient，authId 传店铺 shopId。
     */
    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        CfgAppClientEntity cfgAppClient = getAliExpressLogisticsClient();
        if (Objects.isNull(cfgAppClient)) return new HashMap<>();

        Map<String, String> map = buildAuthMap(cfgAppClient, shopId, null);
        if (StringUtils.isNotBlank(shopId)) {
            ShopAuthEntity shopAuth = shopInfoFeign.getShopAuthByShopId(shopId);
            if (Objects.nonNull(shopAuth)) {
                map.put("shopId", shopAuth.getShopId());
                putIfNotBlank(map, "token", getShopToken(shopAuth));
            }
        }
        return map;
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        fillOrderId(chanelQueryVO);
        if (StringUtils.isBlank(chanelQueryVO.getOrderId()) || StringUtils.isBlank(chanelQueryVO.getChildOrderId())) {
            return failure(getPlatForm().getName() + "：获取可用线路时，订单编号不能为空");
        }
        Map<String, String> authMap = chanelQueryVO.getAuthMap();
        String sellerId = resolveSellerId(authMap);
        if (StringUtils.isBlank(sellerId)) {
            return failure(getPlatForm().getName() + "：获取可用线路时，sellerId不能为空");
        }

        IopResponse iopResponse = null;
        try {
            String tradeOrderItemIdList = buildTradeOrderItemIdList(chanelQueryVO.getChildOrderId());
            iopResponse = aliExpressShipperService.getOverseasManagedShippingService(authMap, chanelQueryVO.getOrderId(), tradeOrderItemIdList, sellerId);
            if (Objects.isNull(iopResponse) || StringUtils.isNotBlank(iopResponse.getMessage())) {
                pullGetChannelLog(chanelQueryVO, iopResponse, RequestStatusEnums.FAILED.getCode());
                return failure(getPlatForm().getName() + ":" + (Objects.isNull(iopResponse) ? "接口返回为空" : iopResponse.getMessage()));
            }
            OverseasManagedShippingServiceResponse response = parseShippingServiceResponse(iopResponse.getBody());
            if (Objects.isNull(response) || !isSuccess(response) || Objects.isNull(response.getData())) {
                pullGetChannelLog(chanelQueryVO, response, RequestStatusEnums.FAILED.getCode());
                return failure(getPlatForm().getName() + ":" + getErrorMessage(response));
            }
            List<LogisticsSaleChannelEntity> channels = convertChannels(response.getData().getShipmentProviderDTOList());
            pullGetChannelLog(chanelQueryVO, response, RequestStatusEnums.SUCCESS.getCode());
            return success(channels);
        } catch (Exception e) {
            log.error("速卖通海外托管getChannel接口调用失败：{}", e.getMessage());
            pullGetChannelLog(chanelQueryVO, Objects.nonNull(iopResponse) ? iopResponse : e, RequestStatusEnums.FAILED.getCode());
            return failure(e.getMessage());
        }
    }

    @Override
    public ApiResult<Object> authorization(Map<String, String> authMap) {
        try {
            String sellerId = resolveSellerId(authMap);
            if (StringUtils.isBlank(sellerId)) {
                return failure(getPlatForm().getName() + "：未获取到LOCAL_SERVICE对应的sellerId");
            }
            return success("授权成功");
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        IopResponse iopResponse = null;
        try {
            iopResponse = aliExpressShipperService.listLogisticsService(authMap);
            if (Objects.isNull(iopResponse) || StringUtils.isNotBlank(iopResponse.getMessage())) {
                return failure(getPlatForm().getName() + ":" + (Objects.isNull(iopResponse) ? "接口返回为空" : iopResponse.getMessage()));
            }
            JSONObject payload = parseLogisticsServicePayload(iopResponse.getBody());
            if (Objects.isNull(payload) || !isLogisticsServiceSuccess(payload)) {
                return failure(getPlatForm().getName() + ":" + getLogisticsServiceError(payload));
            }
            JSONArray resultList = payload.getJSONArray("result_list");
            if (CollUtil.isEmpty(resultList)) {
                return success(Collections.emptyList());
            }
            List<LogisticsServiceDTO> list = resultList.toJavaList(LogisticsServiceDTO.class);
            List<LogisticsServiceResponseVO> result = new ArrayList<>(list.size());
            for (LogisticsServiceDTO item : list) {
                if (Objects.isNull(item) || StringUtils.isBlank(item.getServiceName())) {
                    continue;
                }
                LogisticsServiceResponseVO vo = new LogisticsServiceResponseVO();
                vo.setServiceName(firstNotBlank(item.getDisplayName(), item.getServiceName()));
                vo.setLogisticsType(item.getServiceName());
                result.add(vo);
            }
            return success(result);
        } catch (Exception e) {
            log.error("速卖通海外托管listLogisticsService接口调用失败：{}", e.getMessage());
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.ALI_EXPRESS_OVERSEAS_MANAGED;
    }

    private CfgAppClientEntity getAliExpressLogisticsClient() {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_LOGISTICS;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        try {
            return dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务dmpTaskFeign.getCfgAppClient接口异常：{}", e.getMessage());
            return null;
        }
    }

    private Map<String, String> buildAuthMap(CfgAppClientEntity cfgAppClient, String shopId, String token) {
        Map<String, String> map = new HashMap<>();
        map.put("id", cfgAppClient.getId());
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("clientSecret", cfgAppClient.getClientSecret());
        map.put("clientId", cfgAppClient.getClientId());
        map.put("url", cfgAppClient.getUrl());
        if (StringUtils.isNotBlank(shopId)) {
            map.put("shopId", shopId);
        }
        if (StringUtils.isNotBlank(token)) {
            map.put("token", token);
        }
        putIfNotBlank(map, ORDER_ID, firstNotBlank(overseasManagedOrderId, aliExpressOrderId));
        putIfNotBlank(map, CHILD_ORDER_ID, firstNotBlank(overseasManagedChildOrderId, aliExpressChildOrderId));
        return map;
    }

    private void fillOrderId(ChanelQueryVO chanelQueryVO) {
        Map<String, String> authMap = chanelQueryVO.getAuthMap();
        if (Objects.isNull(authMap)) {
            authMap = new HashMap<>();
            chanelQueryVO.setAuthMap(authMap);
        }
        if (StringUtils.isBlank(chanelQueryVO.getOrderId())) {
            chanelQueryVO.setOrderId(authMap.get(ORDER_ID));
        }
        if (StringUtils.isBlank(chanelQueryVO.getChildOrderId())) {
            chanelQueryVO.setChildOrderId(authMap.get(CHILD_ORDER_ID));
        }
    }

    private String resolveSellerId(Map<String, String> authMap) {
        if (Objects.isNull(authMap)) {
            return "";
        }
        String sellerId = firstNotBlank(authMap.get(SELLER_ID), authMap.get("channelSellerId"), authMap.get("channel_seller_id"));
        if (StringUtils.isNotBlank(sellerId)) {
            authMap.put(SELLER_ID, sellerId);
            return sellerId;
        }
        try {
            IopResponse response = aliExpressShipperService.getGlobalSellerRelation(authMap, LOCAL_SERVICE);
            sellerId = parseChannelSellerId(response);
            if (StringUtils.isNotBlank(sellerId)) {
                authMap.put(SELLER_ID, sellerId);
                authMap.put(TOP_USER_KEY, sellerId);
            }
            return sellerId;
        } catch (Exception e) {
            log.error("速卖通海外托管获取sellerId失败：{}", e.getMessage());
            return "";
        }
    }

    private String getShopToken(ShopAuthEntity shopAuth) {
        if (Objects.isNull(shopAuth)) {
            return "";
        }
        return firstNotBlank(shopAuth.getAccessToken(), shopAuth.getToken());
    }

    private String parseChannelSellerId(IopResponse response) {
        if (Objects.isNull(response) || StringUtils.isBlank(response.getBody())) {
            return "";
        }
        JSONObject root = JSON.parseObject(response.getBody());
        JSONObject payload = root.getJSONObject(SELLER_RELATION_RESPONSE_KEY);
        if (Objects.isNull(payload)) {
            payload = root;
        }
        return findString(payload, "channel_seller_id", "channelSellerId");
    }

    private OverseasManagedShippingServiceResponse parseShippingServiceResponse(String body) {
        if (StringUtils.isBlank(body)) {
            return null;
        }
        JSONObject root = JSON.parseObject(body);
        JSONObject payload = root.getJSONObject(OVERSEAS_MANAGED_RESPONSE_KEY);
        if (Objects.isNull(payload)) {
            payload = root;
        } else {
            putIfAbsent(payload, "code", root.getString("code"));
            putIfAbsent(payload, "request_id", root.getString("request_id"));
        }
        return payload.toJavaObject(OverseasManagedShippingServiceResponse.class);
    }

    private JSONObject parseLogisticsServicePayload(String body) {
        if (StringUtils.isBlank(body)) {
            return null;
        }
        JSONObject root = JSON.parseObject(body);
        JSONObject payload = root.getJSONObject(LOGISTICS_SERVICE_RESPONSE_KEY);
        if (Objects.isNull(payload)) {
            payload = root;
        } else {
            putIfAbsent(payload, "code", root.getString("code"));
            putIfAbsent(payload, "request_id", root.getString("request_id"));
        }
        return payload;
    }

    private boolean isSuccess(OverseasManagedShippingServiceResponse response) {
        if (Objects.isNull(response)) {
            return false;
        }
        Object success = response.getSuccess();
        boolean successFlag = !Boolean.FALSE.equals(success) && !"false".equalsIgnoreCase(String.valueOf(success));
        return "0".equals(response.getCode()) && successFlag
                && StringUtils.isBlank(response.getErrorCode());
    }

    private String getErrorMessage(OverseasManagedShippingServiceResponse response) {
        if (Objects.isNull(response)) {
            return "接口返回为空";
        }
        return firstNotBlank(response.getErrorMessage(), response.getErrorCode(), "获取可用线路失败");
    }

    private boolean isLogisticsServiceSuccess(JSONObject payload) {
        if (Objects.isNull(payload)) {
            return false;
        }
        Object success = payload.get("result_success");
        boolean successFlag = !Boolean.FALSE.equals(success) && !"false".equalsIgnoreCase(String.valueOf(success));
        return successFlag && StringUtils.isBlank(payload.getString("error_code"));
    }

    private String getLogisticsServiceError(JSONObject payload) {
        if (Objects.isNull(payload)) {
            return "接口返回为空";
        }
        return firstNotBlank(payload.getString("error_desc"), payload.getString("error_code"), "获取标发物流渠道失败");
    }

    private String buildTradeOrderItemIdList(String childOrderId) {
        String value = StringUtils.trimToEmpty(childOrderId);
        if (value.startsWith("[") && value.endsWith("]")) {
            return value;
        }
        if (value.contains(",")) {
            JSONArray array = new JSONArray();
            for (String item : value.split(",")) {
                if (StringUtils.isNotBlank(item)) {
                    array.add(StringUtils.trim(item));
                }
            }
            return array.toJSONString();
        }
        return "[" + value + "]";
    }

    private List<LogisticsSaleChannelEntity> convertChannels(List<OverseasManagedShippingServiceResponse.ShipmentProviderDTO> providerList) {
        if (CollUtil.isEmpty(providerList)) {
            return Collections.emptyList();
        }
        List<LogisticsSaleChannelEntity> result = new ArrayList<>(providerList.size());
        for (OverseasManagedShippingServiceResponse.ShipmentProviderDTO provider : providerList) {
            if (Objects.isNull(provider) || StringUtils.isBlank(provider.getShipmentProviderCode())) {
                continue;
            }
            LogisticsSaleChannelEntity entity = new LogisticsSaleChannelEntity();
            entity.setPlatformChannelId(provider.getShipmentProviderCode());
            entity.setCode(provider.getShipmentProviderCode());
            entity.setCnName(provider.getShipmentProviderName());
            entity.setEnName(provider.getShipmentProviderName());
            entity.setSupplierName(provider.getShipmentProviderName());
            entity.setSupplierCode(provider.getShipmentProviderCode());
            entity.setShipmentMethod(joinShippingTypes(provider.getShippingTypes()));
            entity.setLogisticsPlatform(getPlatForm().getCode());
            entity.setSourceData(JSONUtil.toJsonStr(provider));
            entity.setIsTrack(Boolean.TRUE);
            entity.setChannelStatus(0);
            entity.setCarrierType(joinCarrierInfo(provider.getCarrierInfoDTOList()));
            entity.setChannelType(provider.getDeliveryMode());
            entity.setServicePlatform("tms");
            result.add(entity);
        }
        return result;
    }

    private String joinShippingTypes(List<OverseasManagedShippingServiceResponse.ShippingTypeDTO> shippingTypes) {
        if (CollUtil.isEmpty(shippingTypes)) {
            return "";
        }
        return shippingTypes.stream()
                .filter(Objects::nonNull)
                .map(item -> firstNotBlank(item.getCode(), item.getName()))
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.joining(","));
    }

    private String joinCarrierInfo(List<OverseasManagedShippingServiceResponse.CarrierInfoDTO> carrierList) {
        if (CollUtil.isEmpty(carrierList)) {
            return "";
        }
        return carrierList.stream()
                .filter(Objects::nonNull)
                .map(item -> firstNotBlank(item.getCarrierCode(), item.getCarrierName()))
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.joining(","));
    }

    private void pullGetChannelLog(ChanelQueryVO chanelQueryVO, Object response, String status) {
        logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), getPlatForm().getCode(),
                status, JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(response));
    }

    private String findString(Object obj, String... keys) {
        if (Objects.isNull(obj)) {
            return "";
        }
        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            for (String key : keys) {
                Object value = jsonObject.get(key);
                if (Objects.nonNull(value) && StringUtils.isNotBlank(String.valueOf(value))) {
                    return String.valueOf(value);
                }
            }
            for (String key : jsonObject.keySet()) {
                String value = findString(jsonObject.get(key), keys);
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) obj;
            for (Object item : jsonArray) {
                String value = findString(item, keys);
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            }
        }
        return "";
    }

    private String firstNotBlank(String... values) {
        if (Objects.isNull(values)) {
            return "";
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                seen.add(value);
            }
        }
        return seen.stream().findFirst().orElse("");
    }

    private void putIfNotBlank(Map<String, String> map, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            map.put(key, value);
        }
    }

    private void putIfAbsent(JSONObject jsonObject, String key, String value) {
        if (Objects.nonNull(jsonObject) && !jsonObject.containsKey(key) && StringUtils.isNotBlank(value)) {
            jsonObject.put(key, value);
        }
    }
}
