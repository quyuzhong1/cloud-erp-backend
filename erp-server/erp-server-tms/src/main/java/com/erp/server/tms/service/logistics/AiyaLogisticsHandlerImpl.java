package com.erp.server.tms.service.logistics;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.ChannelTypeEnum;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.wms.aiya.service.AiyaOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AIYA（百世）物流接口处理器。
 * <p>
 * 授权属于特殊场景，无需调用第三方授权接口，仅校验并落库授权字段。
 * {@code /logisticsAuth/add} 爱亚 fieldMap 约定：
 * {@code appKey}、{@code appSecret}、{@code partnerId}、{@code customerCode}。
 * SDK 调用时：外层 partnerId 取 {@code partnerId}（为空时回退 {@code appKey}），
 * 签名密钥取 {@code appSecret}，业务参数 customerCode 取 {@code customerCode}。
 * <p>
 * 物流渠道同步走 {@code LogisticsBaseServiceImpl#syncSingleChannel} 通用流程，
 * 由本 handler 的 {@link #getChannel(ChanelQueryVO)} 调用 AIYA
 * {@code GLINK_QUERY_CARRIER_NOTIFY} 拉取派送渠道并转换为 {@link LogisticsSaleChannelEntity}。
 * <p>
 * 因 AIYA 派送渠道接口要求必填 {@code warehouseCode}，本处理器会先调用
 * {@code GLINK_QUERY_WAREHOUSE_NOTIFY} 获取仓库列表，再按仓逐个查询渠道。
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.AIYA)
public class AiyaLogisticsHandlerImpl extends AbstractLogisticsHandler {

    private static final String PARTNER_ID = "partnerId";
    private static final String APP_KEY = "appKey";
    private static final String CUSTOMER_CODE = "customerCode";
    private static final String APP_SECRET = "appSecret";
    private static final String PARTNER_KEY = "partnerKey";

    /**
     * tms 侧 ERP 服务平台标识，统一与其它 tms 物流商保持一致
     */
    private static final String SERVICE_PLATFORM_TMS = "tms";

    @Resource
    private AiyaOpenApiService aiyaOpenApiService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public ApiResult<Object> authorization(Map<String, String> authMap) {
        if (authMap == null) {
            throw new ServiceException(ApiError.LOGISTICS_AIYA_CHANNEL_AUTH_INFO_EMPTY);
        }
        String partnerId = resolvePartnerId(authMap);
        String customerCode = authMap.get(CUSTOMER_CODE);
        String partnerKey = resolvePartnerKey(authMap);
        if (CharSequenceUtil.hasBlank(partnerId, customerCode, partnerKey)) {
            throw new ServiceException(ApiError.LOGISTICS_AIYA_CHANNEL_TOKEN_SECRET_MISSING);
        }
        return success("授权成功");
    }

    /**
     * 调用 AIYA {@code GLINK_QUERY_CARRIER_NOTIFY} 拉取派送渠道。
     * <p>
     * 由 {@code LogisticsBaseServiceImpl#syncSingleChannel} 在轮询每一份
     * {@code logistics_auth} 授权配置时回调，返回的列表会被通用流程统一
     * 落库到 {@code logistics_sale_channel}。
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, String> authMap = chanelQueryVO == null ? null : chanelQueryVO.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            return ApiResult.error(ApiError.LOGISTICS_AIYA_CHANNEL_AUTH_INFO_EMPTY);
        }
        String partnerId = resolvePartnerId(authMap);
        String customerCode = authMap.get(CUSTOMER_CODE);
        String partnerKey = resolvePartnerKey(authMap);
        if (CharSequenceUtil.hasBlank(partnerId, customerCode, partnerKey)) {
            return ApiResult.error(ApiError.LOGISTICS_AIYA_CHANNEL_TOKEN_SECRET_MISSING);
        }

        List<String> warehouseCodes;
        try {
            warehouseCodes = queryWarehouseCodes(partnerId, partnerKey, customerCode, authMap.get("id"));
        } catch (ServiceException e) {
            return ApiResult.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            log.error("[AIYA渠道同步] 查询仓库列表异常, authId={}", authMap.get("id"), e);
            return ApiResult.error(ApiError.LOGISTICS_AIYA_CHANNEL_QUERY_ERROR);
        }
        if (warehouseCodes.isEmpty()) {
            return ApiResult.error(ApiError.LOGISTICS_AIYA_CHANNEL_WAREHOUSE_EMPTY);
        }

        String platform = LogisticsPlatformEnum.AIYA.getCode();
        List<LogisticsSaleChannelEntity> channelList = new ArrayList<>();
        for (String warehouseCode : warehouseCodes) {
            JSONObject response;
            try {
                Map<String, Object> bizParams = new HashMap<>(2);
                bizParams.put("warehouseCode", warehouseCode);
                response = aiyaOpenApiService.queryTransport(partnerId, partnerKey, customerCode, bizParams);
            } catch (Exception e) {
                log.error("[AIYA渠道同步] 调用 GLINK_QUERY_CARRIER_NOTIFY 异常, authId={}, warehouseCode={}",
                        authMap.get("id"), warehouseCode, e);
                return ApiResult.error(ApiError.LOGISTICS_AIYA_CHANNEL_QUERY_ERROR);
            }
            if (Objects.isNull(response)) {
                return ApiResult.error(ApiError.LOGISTICS_AIYA_CHANNEL_RESPONSE_EMPTY);
            }
            if (!Boolean.TRUE.equals(response.getBoolean("success"))) {
                String errorMsg = CharSequenceUtil.blankToDefault(response.getString("message"),
                        response.getString("errorMsg"));
                String errorCode = CharSequenceUtil.blankToDefault(response.getString("code"),
                        response.getString("errorCode"));
                log.error("[AIYA渠道同步] 接口返回失败, authId={}, warehouseCode={}, code={}, message={}",
                        authMap.get("id"), warehouseCode, errorCode, errorMsg);
                return ApiResult.error(ApiError.LOGISTICS_AIYA_CHANNEL_QUERY_FAILED.getCode(),
                        CharSequenceUtil.format(ApiError.LOGISTICS_AIYA_CHANNEL_QUERY_FAILED.getMsg(),
                                CharSequenceUtil.blankToDefault(errorMsg, "")));
            }
            JSONArray resultList = response.getJSONArray("resultList");
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            for (int i = 0; i < resultList.size(); i++) {
                JSONObject item = resultList.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                channelList.addAll(buildSaleChannels(platform, warehouseCode, item));
            }
        }

        fillOverseasWarehouseId(channelList);
        return success(channelList);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.AIYA;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(ApiError.LOGISTICS_AIYA_SERVICE_NOT_OPEN);
    }

    /**
     * 先查 AIYA 仓库列表，提取非空 warehouseCode。
     */
    private List<String> queryWarehouseCodes(String partnerId, String partnerKey, String customerCode, String authId) {
        JSONObject response = aiyaOpenApiService.queryWarehouse(partnerId, partnerKey, customerCode, null);
        if (Objects.isNull(response)) {
            throw new ServiceException(ApiError.LOGISTICS_AIYA_CHANNEL_RESPONSE_EMPTY);
        }
        if (!Boolean.TRUE.equals(response.getBoolean("success"))) {
            String errorMsg = CharSequenceUtil.blankToDefault(response.getString("message"),
                    response.getString("errorMsg"));
            log.error("[AIYA渠道同步] 查询仓库失败, authId={}, code={}, message={}",
                    authId, response.get("code"), errorMsg);
            throw new ServiceException(ApiError.LOGISTICS_AIYA_CHANNEL_QUERY_FAILED,
                    CharSequenceUtil.blankToDefault(errorMsg, ""));
        }
        JSONArray resultList = response.getJSONArray("resultList");
        if (resultList == null || resultList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> warehouseCodes = new ArrayList<>(resultList.size());
        for (int i = 0; i < resultList.size(); i++) {
            JSONObject item = resultList.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String warehouseCode = item.getString("warehouseCode");
            if (CharSequenceUtil.isNotBlank(warehouseCode) && !warehouseCodes.contains(warehouseCode)) {
                warehouseCodes.add(warehouseCode);
            }
        }
        return warehouseCodes;
    }

    /**
     * 将 AIYA resultList 单条（含 carrierServiceList）映射为渠道实体列表。
     * <p>
     * 落库 upsert 唯一键（受 {@code saveOrUpdateSaleChannel} 约束）：
     * {@code logisticsPlatform + code + platformWarehouseCode}。
     */
    private List<LogisticsSaleChannelEntity> buildSaleChannels(String platform, String warehouseCode, JSONObject item) {
        List<LogisticsSaleChannelEntity> list = new ArrayList<>();
        String logisticsProvider = item.getString("logisticsProvider");
        JSONArray carrierServiceList = item.getJSONArray("carrierServiceList");
        if (carrierServiceList == null || carrierServiceList.isEmpty()) {
            log.warn("[AIYA渠道同步] carrierServiceList为空, warehouseCode={}, item={}",
                    warehouseCode, item.toJSONString());
            return list;
        }
        for (int i = 0; i < carrierServiceList.size(); i++) {
            JSONObject service = carrierServiceList.getJSONObject(i);
            if (service == null) {
                continue;
            }
            LogisticsSaleChannelEntity entity = buildSaleChannel(platform, warehouseCode, logisticsProvider, service);
            if (entity != null) {
                list.add(entity);
            }
        }
        return list;
    }

    private LogisticsSaleChannelEntity buildSaleChannel(String platform, String warehouseCode,
                                                        String logisticsProvider, JSONObject service) {
        String carrier = service.getString("carrier");
        String carrierService = service.getString("carrierService");
        String carrierType = service.getString("carrierType");
        if (CharSequenceUtil.hasBlank(carrier, carrierService)) {
            log.warn("[AIYA渠道同步] 派送渠道关键字段为空, 跳过: warehouseCode={}, service={}",
                    warehouseCode, service.toJSONString());
            return null;
        }
        String channelCode = buildChannelCode(carrier, carrierService);
        String displayName = CharSequenceUtil.blankToDefault(service.getString("carrierServiceDescription"),
                carrierService);

        LogisticsSaleChannelEntity entity = new LogisticsSaleChannelEntity();
        entity.setLogisticsPlatform(platform);
        entity.setServicePlatform(SERVICE_PLATFORM_TMS);
        entity.setPlatformChannelId(channelCode);
        entity.setCode(channelCode);
        entity.setCnName(displayName);
        entity.setEnName(displayName);
        entity.setSupplierName(CharSequenceUtil.blankToDefault(logisticsProvider, carrier));
        entity.setSupplierCode(carrier);
        entity.setPlatformWarehouseCode(warehouseCode);
        entity.setCarrierType(CharSequenceUtil.blankToDefault(carrierType, carrier));
        entity.setChannelType(ChannelTypeEnum.LAST_MILE.getCode());
        entity.setChannelStatus(MathUtil.ZERO);
        entity.setSourceData(service.toJSONString());
        return entity;
    }

    /**
     * 构造 AIYA 渠道 code：{@code carrier|carrierService}。
     * {@code platformWarehouseCode} 已单独保留，code 不再叠加仓库编码。
     */
    private String buildChannelCode(String carrier, String carrierService) {
        return CharSequenceUtil.nullToEmpty(carrier) + "|" + CharSequenceUtil.nullToEmpty(carrierService);
    }

    /**
     * 按平台仓库编码回填 overseasWarehouseId，便于物流商同步时挂载海外仓维度。
     */
    private void fillOverseasWarehouseId(List<LogisticsSaleChannelEntity> channelList) {
        if (channelList == null || channelList.isEmpty()) {
            return;
        }
        List<String> warehouseCodeList = channelList.stream()
                .map(LogisticsSaleChannelEntity::getPlatformWarehouseCode)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (warehouseCodeList.isEmpty()) {
            return;
        }
        List<OverseasProviderWarehouseEntity> warehouseList =
                overseasWarehouseFeign.getOverseasWarehouseListByPlatformCodes(warehouseCodeList, getPlatForm().getCode());
        if (warehouseList == null || warehouseList.isEmpty()) {
            return;
        }
        Map<String, String> warehouseIdMap = warehouseList.stream()
                .filter(w -> CharSequenceUtil.isNotBlank(w.getPlatformWarehouseCode())
                        && CharSequenceUtil.isNotBlank(w.getId()))
                .collect(Collectors.toMap(OverseasProviderWarehouseEntity::getPlatformWarehouseCode,
                        OverseasProviderWarehouseEntity::getId, (v1, v2) -> v1));
        for (LogisticsSaleChannelEntity channel : channelList) {
            String warehouseId = warehouseIdMap.get(channel.getPlatformWarehouseCode());
            if (CharSequenceUtil.isNotBlank(warehouseId)) {
                channel.setOverseasWarehouseId(warehouseId);
            }
        }
    }

    /**
     * 外层 partnerId：优先取 logisticsAuth.fieldMap.partnerId，为空时回退 appKey。
     */
    private String resolvePartnerId(Map<String, String> authMap) {
        String partnerId = authMap.get(PARTNER_ID);
        if (CharSequenceUtil.isNotBlank(partnerId)) {
            return partnerId;
        }
        return authMap.get(APP_KEY);
    }

    /**
     * 签名密钥：优先取 logisticsAuth.fieldMap.appSecret，海外仓 auth_json 可能存 partnerKey。
     */
    private String resolvePartnerKey(Map<String, String> authMap) {
        String appSecret = authMap.get(APP_SECRET);
        if (CharSequenceUtil.isNotBlank(appSecret)) {
            return appSecret;
        }
        return authMap.get(PARTNER_KEY);
    }
}
