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
import com.erp.model.file.dto.FileDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
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
    private static final String RTS_PACK_RESPONSE_KEY = "aliexpress_asf_local_supply_split_quantity_rts_pack_response";
    private static final String DOCUMENT_QUERY_RESPONSE_KEY = "aliexpress_asf_local_supply_platform_logistics_document_query_response";
    private static final String REPACK_RESPONSE_KEY = "aliexpress_asf_local_supply_platform_logistics_repack_response";

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private AliExpressShipperService aliExpressShipperService;
    @Resource
    private LogisticsOperateService logisticsOperateService;
    @Resource
    private FileFeign fileFeign;

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
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
        IopResponse iopResponse = null;
        try {
            Map<String, String> authMap = logisticsOrderVO.getAuthMap();
            String sellerId = resolveSellerId(authMap);
            String tradeOrderId = logisticsOrderVO.getPlatformCode();
            String addressId = Objects.nonNull(logisticsOrderVO.getSenderInfo()) ? logisticsOrderVO.getSenderInfo().getId() : "";
            String refundAddressId = firstNotBlank(Objects.nonNull(logisticsOrderVO.getReturnInfo()) ? logisticsOrderVO.getReturnInfo().getId() : "", addressId);
            String sendOption = firstNotBlank(logisticsOrderVO.getDeliveryType(),
                    Objects.nonNull(logisticsOrderVO.getLogisticsChannelEntity()) ? logisticsOrderVO.getLogisticsChannelEntity().getDeliveryType() : "",
                    getFirstShipmentMethod(logisticsOrderVO));
            validateCreateOrderParam(sellerId, tradeOrderId, addressId, refundAddressId, sendOption);
            String itemJson = buildTradeOrderItemSupportItemDTOS(logisticsOrderVO.getLogisticsProductVOList());
            iopResponse = aliExpressShipperService.createOverseasManagedLogisticsOrder(authMap, sellerId,
                    tradeOrderId, itemJson, addressId, refundAddressId, sendOption);
            JSONObject payload = parsePayload(iopResponse, RTS_PACK_RESPONSE_KEY);
            if (!isCommonSuccess(payload)) {
                responseVO.failure(getPlatForm().getName(), logisticsOrderVO.getDeliveryNo(), getCommonError(payload, iopResponse));
                pullPushLog(logisticsOrderVO, BusinessTypeEnum.CREATE_ORDER.getCode(), iopResponse, RequestStatusEnums.FAILED.getCode(), false);
                return failure(responseVO);
            }
            JSONObject data = findObject(payload, "data");
            responseVO.setDeliveryNo(logisticsOrderVO.getDeliveryNo());
            responseVO.setTransportNo(findString(data, "fulfillmentPackageId", "fulfillment_package_id", "packageId", "package_id"));
            responseVO.setTrackNo(findString(data, "trackingNumber", "tracking_number"));
            responseVO.success();
            pullPushLog(logisticsOrderVO, BusinessTypeEnum.CREATE_ORDER.getCode(), iopResponse, RequestStatusEnums.SUCCESS.getCode(), false);
            return success(responseVO);
        } catch (Exception e) {
            responseVO.failure(getPlatForm().getName(), logisticsOrderVO.getDeliveryNo(), e.getMessage());
            pullPushLog(logisticsOrderVO, BusinessTypeEnum.CREATE_ORDER.getCode(),
                    Objects.nonNull(iopResponse) ? iopResponse : e, RequestStatusEnums.FAILED.getCode(), true);
            return failure(responseVO);
        }
    }

    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) {
        List<LogisticsPrintLabelResponse> result = new ArrayList<>();
        boolean successFlag = true;
        for (LogisticsGetLabelVO item : logisticsQueryVO) {
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            IopResponse iopResponse = null;
            try {
                String requestList = buildDocumentRequestList(item);
                iopResponse = aliExpressShipperService.queryOverseasManagedLogisticsDocument(
                        item.getAuthMap(), requestList, getLocale(item.getAuthMap()));
                JSONObject payload = parsePayload(iopResponse, DOCUMENT_QUERY_RESPONSE_KEY);
                if (!isCommonSuccess(payload)) {
                    successFlag = false;
                    response.failure(getPlatForm().getName(), item.getDeliveryNo(), getCommonError(payload, iopResponse));
                    result.add(response);
                    pullGetLabelLog(item, iopResponse, RequestStatusEnums.FAILED.getCode());
                    continue;
                }
                JSONObject data = findObject(payload, "data");
                String labelUrl = findString(data, "fileUrl", "file_url");
                if (StringUtils.isBlank(labelUrl)) {
                    labelUrl = uploadLabel(findString(data, "bytes"), item.getDeliveryNo());
                }
                if (StringUtils.isBlank(labelUrl)) {
                    successFlag = false;
                    response.failure(getPlatForm().getName(), item.getDeliveryNo(), "面单文件为空");
                    result.add(response);
                    pullGetLabelLog(item, payload, RequestStatusEnums.FAILED.getCode());
                    continue;
                }
                response.setLabelUrl(labelUrl);
                response.setDeliveryNoList(Collections.singletonList(item.getDeliveryNo()));
                response.setTransportNoList(Collections.singletonList(item.getTransportNo()));
                response.setTrackNoList(Collections.singletonList(item.getTrackNo()));
                response.success();
                result.add(response);
                pullGetLabelLog(item, iopResponse, RequestStatusEnums.SUCCESS.getCode());
            } catch (Exception e) {
                successFlag = false;
                response.failure(getPlatForm().getName(), item.getDeliveryNo(), e.getMessage());
                result.add(response);
                pullGetLabelLog(item, Objects.nonNull(iopResponse) ? iopResponse : e, RequestStatusEnums.FAILED.getCode());
            }
        }
        return successFlag ? success(result) : failure(result);
    }

    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVO) {
        List<CancelResponseVO> result = new ArrayList<>();
        boolean successFlag = true;
        for (LogisticsCancelOrderVO item : logisticsQueryVO) {
            CancelResponseVO response = CancelResponseVO.builder()
                    .deliveryNo(item.getDeliveryNo())
                    .transportNo(item.getTransportNo())
                    .trackNo(item.getTrackNo())
                    .build();
            IopResponse iopResponse = null;
            try {
                validateCancelParam(item);
                iopResponse = aliExpressShipperService.cancelOverseasManagedLogisticsOrder(item.getAuthMap(),
                        getPackageId(item), item.getPlatformCode(), item.getTrackNo(), getLocale(item.getAuthMap()));
                JSONObject payload = parsePayload(iopResponse, REPACK_RESPONSE_KEY);
                if (!isCommonSuccess(payload)) {
                    successFlag = false;
                    response.failure(getPlatForm().getName(), item.getDeliveryNo(), getCommonError(payload, iopResponse));
                    result.add(response);
                    pullCancelLog(item, iopResponse, RequestStatusEnums.FAILED.getCode(), false);
                    continue;
                }
                response.success();
                result.add(response);
                pullCancelLog(item, iopResponse, RequestStatusEnums.SUCCESS.getCode(), false);
            } catch (Exception e) {
                successFlag = false;
                response.failure(getPlatForm().getName(), item.getDeliveryNo(), e.getMessage());
                result.add(response);
                pullCancelLog(item, Objects.nonNull(iopResponse) ? iopResponse : e, RequestStatusEnums.FAILED.getCode(), true);
            }
        }
        return successFlag ? success(result) : failure(result);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.ALI_EXPRESS_OVERSEAS_MANAGED;
    }

    private CfgAppClientEntity getAliExpressLogisticsClient() {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_OVERSEAS_MANAGED_LOGISTICS;
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

    private void validateCreateOrderParam(String sellerId, String tradeOrderId, String addressId, String refundAddressId, String sendOption) {
        if (StringUtils.isBlank(sellerId)) {
            throw new IllegalArgumentException("sellerId不能为空");
        }
        if (StringUtils.isBlank(tradeOrderId)) {
            throw new IllegalArgumentException("平台订单号不能为空");
        }
        if (StringUtils.isBlank(addressId)) {
            throw new IllegalArgumentException("揽收地址ID不能为空");
        }
        if (StringUtils.isBlank(refundAddressId)) {
            throw new IllegalArgumentException("退货地址ID不能为空");
        }
        if (StringUtils.isBlank(sendOption)) {
            throw new IllegalArgumentException("发货方式不能为空");
        }
    }

    private String getFirstShipmentMethod(LogisticsOrderVO logisticsOrderVO) {
        if (Objects.isNull(logisticsOrderVO.getLogisticsSaleChannel())) {
            return "";
        }
        String shipmentMethod = logisticsOrderVO.getLogisticsSaleChannel().getShipmentMethod();
        if (StringUtils.isBlank(shipmentMethod)) {
            return "";
        }
        return shipmentMethod.split(",")[0];
    }

    private void validateCancelParam(LogisticsCancelOrderVO item) {
        if (StringUtils.isBlank(item.getPlatformCode())) {
            throw new IllegalArgumentException("平台订单号不能为空");
        }
        if (StringUtils.isBlank(getPackageId(item))) {
            throw new IllegalArgumentException("包裹ID不能为空");
        }
        if (StringUtils.isBlank(item.getTrackNo())) {
            throw new IllegalArgumentException("跟踪号不能为空");
        }
    }

    private String buildTradeOrderItemSupportItemDTOS(List<LogisticsProductVO> productList) {
        if (CollUtil.isEmpty(productList)) {
            throw new IllegalArgumentException("商品明细不能为空");
        }
        JSONArray array = new JSONArray();
        for (LogisticsProductVO productVO : productList) {
            if (Objects.isNull(productVO) || Objects.isNull(productVO.getChildOrderId())) {
                continue;
            }
            Integer qty = Objects.nonNull(productVO.getDeliveryQty()) ? productVO.getDeliveryQty() : productVO.getQuantity();
            if (Objects.isNull(qty) || qty <= 0) {
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("tradeOrderItemId", String.valueOf(productVO.getChildOrderId()));
            item.put("quantity", String.valueOf(qty));
            array.add(item);
        }
        if (array.isEmpty()) {
            throw new IllegalArgumentException("交易子单明细不能为空");
        }
        return array.toJSONString();
    }

    private String buildDocumentRequestList(LogisticsGetLabelVO item) {
        if (StringUtils.isBlank(item.getPlatformCode())) {
            throw new IllegalArgumentException("平台订单号不能为空");
        }
        String packageId = getPackageId(item);
        if (StringUtils.isBlank(packageId)) {
            throw new IllegalArgumentException("包裹ID不能为空");
        }
        if (StringUtils.isBlank(item.getTrackNo())) {
            throw new IllegalArgumentException("跟踪号不能为空");
        }
        JSONObject request = new JSONObject();
        request.put("tradeOrderId", item.getPlatformCode());
        request.put("packageId", packageId);
        request.put("trackingNumber", item.getTrackNo());
        JSONArray array = new JSONArray();
        array.add(request);
        return array.toJSONString();
    }

    private String getPackageId(LogisticsQueryBaseVO item) {
        return firstNotBlank(item.getPackageId(), item.getTransportNo());
    }

    private JSONObject parsePayload(IopResponse response, String responseKey) {
        if (Objects.isNull(response) || StringUtils.isBlank(response.getBody())) {
            return null;
        }
        JSONObject root = JSON.parseObject(response.getBody());
        JSONObject payload = root.getJSONObject(responseKey);
        if (Objects.isNull(payload)) {
            payload = root.getJSONObject("result");
        }
        if (Objects.isNull(payload)) {
            payload = root;
        } else {
            putIfAbsent(payload, "code", root.getString("code"));
            putIfAbsent(payload, "request_id", root.getString("request_id"));
        }
        return payload;
    }

    private JSONObject findObject(JSONObject payload, String key) {
        if (Objects.isNull(payload)) {
            return null;
        }
        JSONObject data = payload.getJSONObject(key);
        if (Objects.nonNull(data)) {
            return data;
        }
        for (String payloadKey : payload.keySet()) {
            Object value = payload.get(payloadKey);
            if (value instanceof JSONObject) {
                JSONObject found = findObject((JSONObject) value, key);
                if (Objects.nonNull(found)) {
                    return found;
                }
            }
        }
        return null;
    }

    private boolean isCommonSuccess(JSONObject payload) {
        if (Objects.isNull(payload)) {
            return false;
        }
        Object success = firstNotBlank(payload.getString("success"), payload.getString("result_success"));
        boolean successFlag = StringUtils.isBlank(String.valueOf(success))
                || (!Boolean.FALSE.equals(success) && !"false".equalsIgnoreCase(String.valueOf(success)));
        return successFlag
                && (StringUtils.isBlank(payload.getString("code")) || "0".equals(payload.getString("code")))
                && StringUtils.isBlank(payload.getString("errorCode"))
                && StringUtils.isBlank(payload.getString("error_code"));
    }

    private String getCommonError(JSONObject payload, IopResponse response) {
        if (Objects.nonNull(response) && StringUtils.isNotBlank(response.getMessage())) {
            return response.getMessage();
        }
        if (Objects.isNull(payload)) {
            return "接口返回为空";
        }
        return firstNotBlank(payload.getString("errorMessage"), payload.getString("errorCode"),
                payload.getString("error_desc"), payload.getString("error_code"), "接口调用失败");
    }

    private String uploadLabel(String base64, String deliveryNo) {
        if (StringUtils.isBlank(base64)) {
            return "";
        }
        FileDTO.UploadBase64 uploadBase64 = FileDTO.UploadBase64.builder()
                .base64(base64)
                .fileName(firstNotBlank(deliveryNo, "aliexpressOverseasManaged") + ".pdf")
                .build();
        return fileFeign.uploadFileByBase64(uploadBase64);
    }

    private String getLocale(Map<String, String> authMap) {
        if (Objects.isNull(authMap)) {
            return "zh_CN";
        }
        return firstNotBlank(authMap.get("locale"), "zh_CN");
    }

    private void pullPushLog(LogisticsOrderVO logisticsOrderVO, String businessType, Object response, String status, boolean exception) {
        logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(), logisticsOrderVO.getDeliveryNo(), businessType,
                getPlatForm().getCode(), status, JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(response), exception);
    }

    private void pullGetLabelLog(LogisticsGetLabelVO labelVO, Object response, String status) {
        logisticsOperateService.pullOperateLog(labelVO.getOrderId(), labelVO.getDeliveryNo(),
                BusinessTypeEnum.GET_LABEL_LIST.getCode(), getPlatForm().getCode(),
                status, JSONUtil.toJsonStr(labelVO), JSONUtil.toJsonStr(response));
    }

    private void pullCancelLog(LogisticsCancelOrderVO cancelOrderVO, Object response, String status, boolean exception) {
        logisticsOperateService.pushOperateLog(cancelOrderVO.getOrderId(), cancelOrderVO.getDeliveryNo(),
                BusinessTypeEnum.CANCEL_ORDER.getCode(), getPlatForm().getCode(),
                status, JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(response), exception);
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
