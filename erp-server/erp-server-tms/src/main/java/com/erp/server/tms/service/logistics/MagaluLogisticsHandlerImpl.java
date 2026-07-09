package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.oms.magalu.dto.MagaluShopInfoDTO;
import com.sdk.oms.magalu.service.MagaluService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.MAGALU)
public class MagaluLogisticsHandlerImpl extends AbstractLogisticsHandler {

    private static final String DEFAULT_API_BASE_URL = "https://api.magalu.com";
    private static final String DEFAULT_LABEL_FORMAT = "pdf";
    private static final String DEFAULT_LABEL_TYPE = "full";

    @Resource
    private MagaluService magaluService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        CfgAppClientEntity cfgAppClient = getCfgAppClient();
        Map<String, String> map = new HashMap<>();
        map.put("id", cfgAppClient.getId());
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("clientId", cfgAppClient.getClientId());
        map.put("clientSecret", cfgAppClient.getClientSecret());
        map.put("url", cfgAppClient.getUrl());
        map.put("apiBaseUrl", getExtendValue(cfgAppClient, "apiBaseUrl", DEFAULT_API_BASE_URL));
        map.put("channelId", getExtendValue(cfgAppClient, "channelId", ""));
        if (CharSequenceUtil.isNotBlank(shopId)) {
            ShopAuthEntity shopAuth = shopInfoFeign.getShopAuthByShopId(shopId);
            if (Objects.nonNull(shopAuth)) {
                map.put("shopId", shopAuth.getShopId());
                map.put("token", shopAuth.getAccessToken());
                map.put("refreshToken", shopAuth.getRefreshToken());
            }
        }
        return map;
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        String deliveryId = getDeliveryId(logisticsOrderVO.getPackageId(), logisticsOrderVO.getPackageNumber());
        MagaluShopInfoDTO shopInfoDTO = buildShopInfoDTO(logisticsOrderVO.getAuthMap());
        try {
            JSONObject response = magaluService.createShippingLabel(shopInfoDTO, Collections.singletonList(deliveryId), DEFAULT_LABEL_FORMAT, DEFAULT_LABEL_TYPE);
            String trackingCode = getTrackingCode(response, deliveryId);
            if (CharSequenceUtil.isBlank(trackingCode)) {
                throw new ServiceException(formatMagaluError(response, "Magalu未返回跟踪号"));
            }
            return success(LogisticsOrderResponseVO.builder()
                    .deliveryNo(logisticsOrderVO.getDeliveryNo())
                    .trackNo(trackingCode)
                    .transportNo(trackingCode)
                    .build());
        } catch (Exception e) {
            throw new ServiceException("Magalu【{}】获取跟踪号异常:{}", deliveryId, e.getMessage());
        }
    }

    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsGetLabelVOList) throws IOException {
        LogisticsGetLabelVO authVO = logisticsGetLabelVOList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        if (authVO == null) {
            return failure("Magalu授权信息为空");
        }
        MagaluShopInfoDTO shopInfoDTO = buildShopInfoDTO(authVO.getAuthMap());
        List<LogisticsPrintLabelResponse> responseList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        for (LogisticsGetLabelVO vo : logisticsGetLabelVOList) {
            String deliveryId = getDeliveryId(vo.getPackageId());
            try {
                JSONObject response = magaluService.createShippingLabel(shopInfoDTO, Collections.singletonList(deliveryId), getLabelFormat(vo), DEFAULT_LABEL_TYPE);
                String signedUrl = getSignedUrl(response);
                if (CharSequenceUtil.isBlank(signedUrl)) {
                    throw new ServiceException(formatMagaluError(response, "Magalu未返回面单下载地址"));
                }
                String trackingCode = getTrackingCode(response, deliveryId);
                responseList.add(LogisticsPrintLabelResponse.builder()
                        .deliveryNoList(Collections.singletonList(vo.getDeliveryNo()))
                        .trackNoList(CharSequenceUtil.isBlank(trackingCode) ? Collections.emptyList() : Collections.singletonList(trackingCode))
                        .transportNoList(CharSequenceUtil.isBlank(trackingCode) ? Collections.emptyList() : Collections.singletonList(trackingCode))
                        .labelUrl(signedUrl)
                        .build());
            } catch (Exception e) {
                log.error("Magalu获取面单失败,deliveryId={}", deliveryId, e);
                errorList.add(e.getMessage());
            }
        }
        return CollUtil.isNotEmpty(responseList) ? success(responseList) : failure(String.join(";", errorList));
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return ApiResult.success(Collections.emptyList());
    }

    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> cancelOrderVOList) {
        return ApiResult.success();
    }

    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        return ApiResult.success();
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.MAGALU;
    }

    private CfgAppClientEntity getCfgAppClient() {
        CfgAppClientDTO.FindDTO findDTO = CfgAppClientDTO.FindDTO.init(AppClientEnum.MAGALU_ACCESS_TOKEN);
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("Magalu应用授权配置不存在");
        }
        return cfgAppClient;
    }

    private MagaluShopInfoDTO buildShopInfoDTO(Map<String, String> authMap) {
        if (authMap == null || CharSequenceUtil.isBlank(authMap.get("token"))) {
            throw new ServiceException("Magalu授权token为空");
        }
        return new MagaluShopInfoDTO()
                .setId(authMap.get("shopId"))
                .setAccessToken(authMap.get("token"))
                .setRefreshToken(authMap.get("refreshToken"))
                .setApiBaseUrl(firstNotBlank(authMap.get("apiBaseUrl"), DEFAULT_API_BASE_URL))
                .setChannelId(authMap.get("channelId"));
    }

    private String getDeliveryId(String... values) {
        String deliveryId = firstNotBlank(values);
        if (CharSequenceUtil.isBlank(deliveryId)) {
            throw new ServiceException("Magalu订单delivery.id不能为空");
        }
        return deliveryId;
    }

    private String getTrackingCode(JSONObject response, String deliveryId) {
        JSONArray deliveries = response.getJSONArray("deliveries");
        if (CollUtil.isEmpty(deliveries)) {
            return "";
        }
        for (Object obj : deliveries) {
            JSONObject delivery = (JSONObject) obj;
            if (deliveryId.equals(delivery.getString("id")) || deliveries.size() == 1) {
                JSONObject tracking = delivery.getJSONObject("tracking");
                return tracking == null ? "" : tracking.getString("code");
            }
        }
        return "";
    }

    private String getSignedUrl(JSONObject response) {
        JSONObject label = response.getJSONObject("label");
        return label == null ? "" : label.getString("signed_url");
    }

    private String formatMagaluError(JSONObject response, String defaultMessage) {
        if (response == null || response.isEmpty()) {
            return defaultMessage;
        }
        String slug = response.getString("slug");
        String message = response.getString("message");
        JSONArray details = response.getJSONArray("details");
        if (CollUtil.isNotEmpty(details)) {
            JSONObject detail = details.getJSONObject(0);
            String detailSlug = detail.getString("slug");
            String detailMessage = detail.getString("message");
            if (CharSequenceUtil.isNotBlank(detailSlug) || CharSequenceUtil.isNotBlank(detailMessage)) {
                return defaultMessage + ":" + firstNotBlank(detailSlug, slug) + "-" + firstNotBlank(detailMessage, message);
            }
        }
        if (CharSequenceUtil.isNotBlank(slug) || CharSequenceUtil.isNotBlank(message)) {
            return defaultMessage + ":" + firstNotBlank(slug, "") + "-" + firstNotBlank(message, "");
        }
        return defaultMessage + ",response=" + JSONUtil.toJsonStr(response);
    }

    private String getLabelFormat(LogisticsGetLabelVO vo) {
        String labelType = vo.getLabelType();
        if (CharSequenceUtil.isBlank(labelType)) {
            return DEFAULT_LABEL_FORMAT;
        }
        String lower = labelType.toLowerCase();
        if (lower.contains("zpl")) {
            return "zpl";
        }
        return DEFAULT_LABEL_FORMAT;
    }

    private String getExtendValue(CfgAppClientEntity cfgAppClient, String key, String defaultValue) {
        Map<String, Object> extendData = cfgAppClient.getExtendData();
        if (extendData == null || Objects.isNull(extendData.get(key))) {
            return defaultValue;
        }
        String value = extendData.get(key).toString();
        return CharSequenceUtil.isBlank(value) ? defaultValue : value;
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (CharSequenceUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }
}
