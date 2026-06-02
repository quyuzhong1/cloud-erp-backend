package com.sdk.wms.wego.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.wms.dto.WegoTransportQueryDTO;
import com.erp.model.wms.dto.WegoWarehouseQueryDTO;
import com.sdk.wms.wego.constants.WeGoConstants;
import com.sdk.wms.wego.utils.WeGoSignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * WEGO 海外仓开放接口 SDK
 */
@Slf4j
@Component
@Validated
public class WegoOpenApiService {

    /**
     * 根据当前激活的 Spring profile 选择 WEGO 接口域名
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
        long start = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("accessToken", dto.getAccessToken());
        params.put("interfaceType", WeGoConstants.WAREHOUSE_GET);
        if (dto.getBizParams() != null && !dto.getBizParams().isEmpty()) {
            params.putAll(dto.getBizParams());
        }
        String sign = WeGoSignUtils.sign(params, dto.getSecret());
        params.put(WeGoSignUtils.SIGN_FIELD, sign);

        String url = buildRouterUrl(getPreUrl());
        String requestJson = JSON.toJSONString(params);
        log.info("[WEGO查询仓库] 请求开始, url={}, params={}", url, requestJson);
        String response;
        try {
            response = OkHttpUtils.doPostJson(url, params, null);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[WEGO查询仓库] HTTP调用异常, url={}, cost={}ms, params={}", url, cost, requestJson, e);
            throw new ServiceException("WEGO 查询仓库接口调用异常: " + e.getMessage());
        }
        long cost = System.currentTimeMillis() - start;
        ThirdWarehouseContext.setRequestJson(requestJson);
        ThirdWarehouseContext.setResponseJson(response);
        log.info("[WEGO查询仓库] 请求结束, cost={}ms, response={}", cost, response);
        if (response == null || response.isEmpty()) {
            log.error("[WEGO查询仓库] 接口返回为空, url={}, params={}", url, requestJson);
            throw new ServiceException("WEGO 查询仓库接口返回为空");
        }
        try {
            return JSON.parseObject(response);
        } catch (Exception ex) {
            log.error("[WEGO查询仓库] 响应JSON解析失败, response={}", response, ex);
            throw new ServiceException("WEGO 查询仓库接口返回非JSON格式");
        }
    }

    /**
     * 调用 WEGO transport.get 查询派送渠道列表。
     * <p>
     * 业务参数（warehouseBusiness / warehouseCode / transportationType）均为可选，
     * 调用方按需通过 {@link WegoTransportQueryDTO.QueryReqDTO#getBizParams()} 透传。
     *
     * @param dto 入参，包含 accessToken / secret / 业务扩展参数
     * @return WEGO 接口原始响应解析后的 JSONObject（含 success / errorCode / errorMsg / serverTime / result 等字段）
     */
    public JSONObject queryTransport(WegoTransportQueryDTO.QueryReqDTO dto) {
        long start = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("accessToken", dto.getAccessToken());
        params.put("interfaceType", WeGoConstants.TRANSPORT_GET);
        if (dto.getBizParams() != null && !dto.getBizParams().isEmpty()) {
            params.putAll(dto.getBizParams());
        }
        String sign = WeGoSignUtils.sign(params, dto.getSecret());
        params.put(WeGoSignUtils.SIGN_FIELD, sign);

        String url = buildRouterUrl(getPreUrl());
        String requestJson = JSON.toJSONString(params);
        log.info("[WEGO查询派送渠道] 请求开始, url={}, params={}", url, requestJson);
        String response;
        try {
            response = OkHttpUtils.doPostJson(url, params, null);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[WEGO查询派送渠道] HTTP调用异常, url={}, cost={}ms, params={}", url, cost, requestJson, e);
            throw new ServiceException("WEGO 查询派送渠道接口调用异常: " + e.getMessage());
        }
        long cost = System.currentTimeMillis() - start;
        ThirdWarehouseContext.setRequestJson(requestJson);
        ThirdWarehouseContext.setResponseJson(response);
        log.info("[WEGO查询派送渠道] 请求结束, cost={}ms, response={}", cost, response);
        if (response == null || response.isEmpty()) {
            log.error("[WEGO查询派送渠道] 接口返回为空, url={}, params={}", url, requestJson);
            throw new ServiceException("WEGO 查询派送渠道接口返回为空");
        }
        try {
            return JSON.parseObject(response);
        } catch (Exception ex) {
            log.error("[WEGO查询派送渠道] 响应JSON解析失败, response={}", response, ex);
            throw new ServiceException("WEGO 查询派送渠道接口返回非JSON格式");
        }
    }

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
