package com.sdk.wms.wego.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.wms.dto.WegoSkuQueryDTO;
import com.erp.model.wms.dto.WegoWarehouseQueryDTO;
import com.sdk.wms.wego.constants.WeGoConstants;
import com.sdk.wms.wego.utils.WeGoSignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * WEGO 海外仓开放接口 SDK
 */
@Slf4j
@Component
@Validated
public class WegoOpenApiService {

    /**
     * WEGO 请求保留参数，禁止由业务透传参数覆盖。
     */
    private static final Set<String> WEGO_RESERVED_PARAM_KEYS =
            new HashSet<>(Arrays.asList("accessToken", "interfaceType", WeGoSignUtils.SIGN_FIELD));

    /**
     * SKU 查询专用保留参数，避免分页参数被业务透传覆盖。
     */
    private static final Set<String> SKU_QUERY_RESERVED_PARAM_KEYS =
            new HashSet<>(Arrays.asList("pageNum", "pageSize"));

    /**
     * 根据当前激活的 Spring profile 选择 WEGO 接口域名。
     *
     * @return WEGO 网关地址（生产或测试）
     */
    private String getPreUrl() {
        if (BusinessCommonConstants.hasProfile("prod")) {
            return WeGoConstants.BASE_URL_PROD;
        } else {
            return WeGoConstants.BASE_URL;
        }
    }

    /**
     * 调用 WEGO warehouse.get 查询仓库列表。
     *
     * @param dto 入参，包含 accessToken / secret / 业务扩展参数
     * @return WEGO 接口原始响应解析后的 JSONObject（含 success / errorCode / errorMsg / serverTime / result 等字段）
     */
    public JSONObject queryWarehouse(WegoWarehouseQueryDTO.QueryReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        mergeBizParams(bizParams, dto.getBizParams(), "查询仓库", Collections.emptySet());
        return doQuery(dto.getAccessToken(), dto.getSecret(), WeGoConstants.WAREHOUSE_GET, bizParams, "查询仓库");
    }

    /**
     * 调用 WEGO product.search 查询 SKU 列表。
     *
     * @param dto 入参，包含 accessToken / secret / pageSize / pageNum / 业务扩展参数
     * @return WEGO 接口原始响应解析后的 JSONObject（含 success / errorCode / errorMsg / serverTime / result 等字段）
     */
    public JSONObject querySku(WegoSkuQueryDTO.QueryReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("pageSize", dto.getPageSize());
        bizParams.put("pageNum", dto.getPageNum());
        mergeBizParams(bizParams, dto.getBizParams(), "查询SKU", SKU_QUERY_RESERVED_PARAM_KEYS);
        return doQuery(dto.getAccessToken(), dto.getSecret(), WeGoConstants.PRODUCT_SEARCH, bizParams, "查询SKU");
    }

    /**
     * WEGO 通用查询方法：组装公共参数、签名并执行 HTTP 请求。
     *
     * @param accessToken   WEGO accessToken
     * @param secret        WEGO secret（仅用于本地签名）
     * @param interfaceType WEGO 接口标识，如 warehouse.get / product.search
     * @param bizParams     业务参数（会参与签名）
     * @param actionName    日志中的业务动作名
     * @return WEGO 接口原始响应解析后的 JSONObject
     */
    private JSONObject doQuery(String accessToken, String secret, String interfaceType, Map<String, Object> bizParams, String actionName) {
        long start = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("accessToken", accessToken);
        params.put("interfaceType", interfaceType);
        mergeBizParams(params, bizParams, actionName, Collections.emptySet());
        // WEGO 签名需覆盖所有请求参数（含业务参数）
        String sign = WeGoSignUtils.sign(params, secret);
        params.put(WeGoSignUtils.SIGN_FIELD, sign);

        String url = buildRouterUrl(getPreUrl());
        String requestJson = JSON.toJSONString(params);
        //数据脱敏打印日志
        String logRequestJson = JSON.toJSONString(maskLogParams(params));
        log.info("[WEGO{}] 请求开始, url={}, params={}", actionName, url, logRequestJson);
        String response;
        try {
            response = OkHttpUtils.doPostJson(url, params, null);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[WEGO{}] HTTP调用异常, url={}, cost={}ms, params={}", actionName, url, cost, logRequestJson, e);
            throw new ServiceException("WEGO " + actionName + "接口调用异常: " + e.getMessage());
        }
        long cost = System.currentTimeMillis() - start;
        log.info("[WEGO{}] 请求结束, cost={}ms, response={}", actionName, cost, response);
        if (response == null || response.isEmpty()) {
            log.error("[WEGO{}] 接口返回为空, url={}, params={}", actionName, url, logRequestJson);
            throw new ServiceException("WEGO " + actionName + "接口返回为空");
        }
        ThirdWarehouseContext.setRequestJson(requestJson);
        ThirdWarehouseContext.setResponseJson(response);
        try {
            return JSON.parseObject(response);
        } catch (Exception ex) {
            log.error("[WEGO{}] 响应JSON解析失败, response={}", actionName, response, ex);
            throw new ServiceException("WEGO " + actionName + "接口返回非JSON格式");
        }
    }

    /**
     * 合并业务参数，过滤受保护参数，避免覆盖系统关键字段。
     */
    private void mergeBizParams(Map<String, Object> targetParams, Map<String, Object> sourceParams,
                                String actionName, Set<String> extraReservedKeys) {
        if (sourceParams == null || sourceParams.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : sourceParams.entrySet()) {
            String key = entry.getKey();
            if (isReservedBizParamKey(key, extraReservedKeys)) {
                log.warn("[WEGO{}] 忽略受保护参数key={}", actionName, key);
                continue;
            }
            targetParams.put(key, entry.getValue());
        }
    }

    /**
     * 判断参数是否属于受保护参数。
     */
    private boolean isReservedBizParamKey(String key, Set<String> extraReservedKeys) {
        if (key == null) {
            return true;
        }
        if (WEGO_RESERVED_PARAM_KEYS.contains(key)) {
            return true;
        }
        return extraReservedKeys != null && extraReservedKeys.contains(key);
    }

    /**
     * 日志参数脱敏，避免打印 accessToken 与 sign。
     */
    private Map<String, Object> maskLogParams(Map<String, Object> params) {
        Map<String, Object> logParams = new HashMap<>(params);
        if (logParams.containsKey("accessToken")) {
            logParams.put("accessToken", "***");
        }
        if (logParams.containsKey(WeGoSignUtils.SIGN_FIELD)) {
            logParams.put(WeGoSignUtils.SIGN_FIELD, "***");
        }
        return logParams;
    }

    /**
     * 构造 WEGO Router 请求地址。
     *
     * @param domain WEGO 网关域名
     * @return 标准化后的 router 完整地址
     */
    private String buildRouterUrl(String domain) {
        String normalized = domain == null ? "" : domain.trim();
        if (normalized.isEmpty()) {
            throw new ServiceException("WEGO域名不能为空");
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized + WeGoConstants.ROUTER_PATH;
    }
}
