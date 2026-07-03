package com.sdk.oms.magalu.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.enums.ApiError;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.magalu.dto.MagaluShopInfoDTO;
import com.sdk.oms.magalu.dto.MagaluTokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class MagaluService {

    private static final String DEFAULT_API_BASE_URL = "https://api.magalu.com";
    private static final String TOKEN_PATH = "/oauth/token";
    private static final String SKU_LIST_PATH = "/seller/v1/portfolios/skus";
    private static final String ORDER_LIST_PATH = "/seller/v1/orders";
    private static final String SHIPPING_LABEL_PATH = "/seller/v1/logistics/shipping-labels";
    private static final int PAGE_SIZE = 100;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    public MagaluTokenDTO createToken(Map<String, String> paramMap) {
        Map<String, Object> params = new HashMap<>(8);
        params.put("grant_type", "authorization_code");
        params.put("client_id", paramMap.get("clientId"));
        params.put("client_secret", paramMap.get("clientSecret"));
        params.put("redirect_uri", paramMap.get("redirectUri"));
        params.put("code", paramMap.get("code"));

        return requestToken(paramMap.get("baseUrl"), params, "获取accessToken");
    }

    public MagaluTokenDTO refreshToken(Map<String, String> paramMap) {
        Map<String, Object> params = new HashMap<>(8);
        params.put("grant_type", "refresh_token");
        params.put("client_id", paramMap.get("clientId"));
        params.put("client_secret", paramMap.get("clientSecret"));
        params.put("refresh_token", paramMap.get("refreshToken"));
        if (StringUtils.isNotBlank(paramMap.get("redirectUri"))) {
            params.put("redirect_uri", paramMap.get("redirectUri"));
        }

        return requestToken(paramMap.get("baseUrl"), params, "刷新token");
    }

    public MagaluShopInfoDTO getShopInfoByShopId(String shopId) {
        String tokenKey = CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MAGALU.getCode(), shopId);
        Object tokenObj = redisUtil.get(tokenKey);
        if (tokenObj instanceof MagaluShopInfoDTO) {
            MagaluShopInfoDTO shopInfoDTO = (MagaluShopInfoDTO) tokenObj;
            if (StringUtils.isBlank(shopInfoDTO.getApiBaseUrl())) {
                shopInfoDTO.setApiBaseUrl(DEFAULT_API_BASE_URL);
            }
            return shopInfoDTO;
        }

        ShopAuthEntity shopAuth = shopInfoFeign.getShopAuthByShopId(shopId);
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(shopId);
        if (Objects.isNull(shopAuth) || Objects.isNull(shopInfo)) {
            return null;
        }

        CfgAppClientEntity cfgAppClient = getCfgAppClient();
        MagaluShopInfoDTO shopInfoDTO = new MagaluShopInfoDTO()
                .setId(shopId)
                .setName(shopInfo.getName())
                .setClientId(cfgAppClient.getClientId())
                .setClientSecret(cfgAppClient.getClientSecret())
                .setBaseUrl(cfgAppClient.getUrl())
                .setApiBaseUrl(getApiBaseUrl(cfgAppClient))
                .setChannelId(getChannelId(cfgAppClient))
                .setRedirectUrl(cfgAppClient.getRedirectUrl())
                .setAccessToken(shopAuth.getAccessToken())
                .setRefreshToken(shopAuth.getRefreshToken());
        redisUtil.set(tokenKey, shopInfoDTO, getCacheSeconds(shopAuth.getExpiresIn()));
        return shopInfoDTO;
    }

    public List<JSONObject> listSkus(MagaluShopInfoDTO shopInfoDTO) {
        List<JSONObject> resultList = new ArrayList<>();
        int offset = 0;
        while (true) {
            List<JSONObject> pageList = listSkuPage(shopInfoDTO, SKU_LIST_PATH, offset, PAGE_SIZE);
            if (pageList.isEmpty()) {
                break;
            }
            resultList.addAll(pageList);
            if (pageList.size() < PAGE_SIZE) {
                break;
            }
            offset += PAGE_SIZE;
        }
        return resultList;
    }

    public List<JSONObject> listSkuPage(MagaluShopInfoDTO shopInfoDTO, String apiPath, int offset, int limit) {
        String path = StringUtils.isBlank(apiPath) ? SKU_LIST_PATH : apiPath;
        String url = trimEndSlash(getApiBaseUrl(shopInfoDTO)) + addStartSlash(path);
        Map<String, Object> params = new HashMap<>(4);
        params.put("limit", limit);
        params.put("_offset", offset);

        String response = OkHttpUtils.doGet(url, params, buildApiHeaders(shopInfoDTO));
        return parseSkuList(response);
    }

    public List<JSONObject> listOrderPage(MagaluShopInfoDTO shopInfoDTO, String apiPath, int offset, int limit, String startTime, String endTime) {
        String path = StringUtils.isBlank(apiPath) ? ORDER_LIST_PATH : apiPath;
        String url = trimEndSlash(getApiBaseUrl(shopInfoDTO)) + addStartSlash(path);
        Map<String, Object> params = new HashMap<>(8);
        params.put("limit", limit);
        params.put("_offset", offset);
        if (StringUtils.isNotBlank(startTime)) {
            params.put("updated_at__ge", startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            params.put("updated_at__le", endTime);
        }

        String response = OkHttpUtils.doGet(url, params, buildApiHeaders(shopInfoDTO));
        return parseDataList(response);
    }

    public JSONObject getOrderDetail(MagaluShopInfoDTO shopInfoDTO, String apiPath, String orderCode) {
        String path = StringUtils.defaultIfBlank(apiPath, ORDER_LIST_PATH + "/{code}");
        path = path.replace("{code}", orderCode).replace("{id}", orderCode);
        String url = trimEndSlash(getApiBaseUrl(shopInfoDTO)) + addStartSlash(path);
        String response = OkHttpUtils.doGet(url, new HashMap<>(), buildApiHeaders(shopInfoDTO));
        return JSON.parseObject(response);
    }

    public JSONObject createShippingLabel(MagaluShopInfoDTO shopInfoDTO, List<String> deliveryIdList, String format, String type) {
        String url = trimEndSlash(getApiBaseUrl(shopInfoDTO)) + SHIPPING_LABEL_PATH;
        Map<String, Object> body = new HashMap<>(4);
        Map<String, Object> channel = new HashMap<>(1);
        channel.put("id", shopInfoDTO.getChannelId());
        body.put("channel", channel);

        List<Map<String, Object>> deliveries = new ArrayList<>();
        for (String deliveryId : deliveryIdList) {
            Map<String, Object> delivery = new HashMap<>(1);
            delivery.put("id", deliveryId);
            deliveries.add(delivery);
        }
        body.put("deliveries", deliveries);

        Map<String, Object> label = new HashMap<>(2);
        label.put("format", StringUtils.defaultIfBlank(format, "pdf"));
        label.put("type", StringUtils.defaultIfBlank(type, "full"));
        body.put("label", label);

        String response = OkHttpUtils.doPostJson(url, body, buildApiHeaders(shopInfoDTO));
        return JSON.parseObject(response);
    }

    private MagaluTokenDTO requestToken(String baseUrl, Map<String, Object> params, String action) {
        String url = trimEndSlash(baseUrl) + TOKEN_PATH;
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        headerMap.put("Accept", "application/json");

        String response = OkHttpUtils.doPost(url, params, headerMap);
        MagaluTokenDTO tokenDTO = JSON.parseObject(response, MagaluTokenDTO.class);
        if (tokenDTO == null || StringUtils.isBlank(tokenDTO.getAccessToken())) {
            throw new ServiceException("Magalu" + action + "失败,response=" + response);
        }
        return tokenDTO;
    }

    private List<JSONObject> parseSkuList(String response) {
        JSONArray dataArray = findDataArray(JSON.parse(response));
        List<JSONObject> resultList = new ArrayList<>();
        if (dataArray == null) {
            return resultList;
        }
        for (Object item : dataArray) {
            if (item instanceof JSONObject) {
                resultList.add((JSONObject) item);
            }
        }
        return resultList;
    }

    private List<JSONObject> parseDataList(String response) {
        JSONArray dataArray = findDataArray(JSON.parse(response));
        List<JSONObject> resultList = new ArrayList<>();
        if (dataArray == null) {
            return resultList;
        }
        for (Object item : dataArray) {
            if (item instanceof JSONObject) {
                resultList.add((JSONObject) item);
            }
        }
        return resultList;
    }

    private JSONArray findDataArray(Object parsed) {
        if (parsed instanceof JSONArray) {
            return (JSONArray) parsed;
        }
        if (!(parsed instanceof JSONObject)) {
            return null;
        }
        JSONObject jsonObject = (JSONObject) parsed;
        for (String key : new String[]{"items", "results", "data", "skus", "content"}) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONArray) {
                return (JSONArray) value;
            }
            if (value instanceof JSONObject) {
                JSONArray nested = findDataArray(value);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private CfgAppClientEntity getCfgAppClient() {
        CfgAppClientDTO.FindDTO findDTO = CfgAppClientDTO.FindDTO.init(AppClientEnum.MAGALU_ACCESS_TOKEN);
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("Magalu应用授权配置不存在");
        }
        return cfgAppClient;
    }

    private String getApiBaseUrl(CfgAppClientEntity cfgAppClient) {
        Map<String, Object> extendData = cfgAppClient.getExtendData();
        if (extendData == null || Objects.isNull(extendData.get("apiBaseUrl"))) {
            return DEFAULT_API_BASE_URL;
        }
        String apiBaseUrl = extendData.get("apiBaseUrl").toString();
        return StringUtils.isBlank(apiBaseUrl) ? DEFAULT_API_BASE_URL : apiBaseUrl;
    }

    private String getChannelId(CfgAppClientEntity cfgAppClient) {
        Map<String, Object> extendData = cfgAppClient.getExtendData();
        if (extendData == null || Objects.isNull(extendData.get("channelId"))) {
            return "";
        }
        return extendData.get("channelId").toString();
    }

    private String getApiBaseUrl(MagaluShopInfoDTO shopInfoDTO) {
        return StringUtils.isBlank(shopInfoDTO.getApiBaseUrl()) ? DEFAULT_API_BASE_URL : shopInfoDTO.getApiBaseUrl();
    }

    private Map<String, String> buildApiHeaders(MagaluShopInfoDTO shopInfoDTO) {
        Map<String, String> headerMap = new HashMap<>(4);
        headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
        headerMap.put("Accept", "application/json");
        String tenantId = getTenantId(shopInfoDTO.getAccessToken());
        if (StringUtils.isNotBlank(tenantId)) {
            headerMap.put("X-Tenant-Id", tenantId);
        }
        if (StringUtils.isNotBlank(shopInfoDTO.getChannelId())) {
            headerMap.put("X-Channel-Id", shopInfoDTO.getChannelId());
        }
        return headerMap;
    }

    private String getTenantId(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return "";
        }
        String[] parts = accessToken.split("\\.");
        if (parts.length < 2) {
            return "";
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), "UTF-8");
            JSONObject payloadJson = JSON.parseObject(payload);
            return payloadJson.getString("tenant");
        } catch (Exception e) {
            throw new ServiceException(ApiError.SHOP_AUTH_REQUIRED);
        }
    }

    private long getCacheSeconds(Integer expiresIn) {
        if (expiresIn == null || expiresIn <= 0) {
            return 3600L;
        }
        return expiresIn;
    }

    private String trimEndSlash(String url) {
        if (StringUtils.isBlank(url)) {
            throw new ServiceException("Magalu授权地址未配置");
        }
        return StringUtils.removeEnd(url, "/");
    }

    private String addStartSlash(String path) {
        if (StringUtils.isBlank(path)) {
            return SKU_LIST_PATH;
        }
        return StringUtils.startsWith(path, "/") ? path : "/" + path;
    }
}
