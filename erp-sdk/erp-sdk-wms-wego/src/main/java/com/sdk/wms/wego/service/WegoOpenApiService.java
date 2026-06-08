package com.sdk.wms.wego.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.wms.dto.WegoInOrderSaveDTO;
import com.erp.model.wms.dto.WegoInventoryQueryDTO;
import com.erp.model.wms.dto.WegoTransportQueryDTO;
import com.erp.model.wms.dto.WegoSkuQueryDTO;
import com.erp.model.wms.dto.WegoWarehouseQueryDTO;
import com.sdk.wms.wego.constants.WeGoConstants;
import com.sdk.wms.wego.utils.WeGoSignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
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
     * 调用 WEGO 2c.inventory.search 分页查询 2C 库存列表。
     * <p>
     * 响应结构示例：
     * <pre>
     * {
     *   "success": true,
     *   "result": {
     *     "pageNum": 1, "pageSize": 200, "pages": 5, "total": 900, "emptyFlag": false,
     *     "list": [ { "sku": "SKU001", "warehouseCode": "W01", "availableQty": 100, ... } ]
     *   }
     * }
     * </pre>
     *
     * @param dto 入参，包含 accessToken / secret / warehouseCode（可选）/ pageNum / pageSize
     * @return WEGO 接口原始响应解析后的 JSONObject
     */
    public JSONObject queryInventory(WegoInventoryQueryDTO.QueryReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("pageSize", dto.getPageSize());
        bizParams.put("pageNum", dto.getPageNum());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dto.getWarehouseCode())) {
            bizParams.put(WegoInventoryQueryDTO.BIZ_KEY_WAREHOUSE_CODE, dto.getWarehouseCode());
        }
        mergeBizParams(bizParams, dto.getBizParams(), "查询库存",
                new HashSet<>(Arrays.asList("pageNum", "pageSize")));
        return doQuery(dto.getAccessToken(), dto.getSecret(), WeGoConstants.TWO_C_INVENTORY_SEARCH, bizParams, "查询库存");
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
        Map<String, Object> bizParams = new HashMap<>();
        mergeBizParams(bizParams, dto.getBizParams(), "查询派送渠道", Collections.emptySet());
        return doQuery(dto.getAccessToken(), dto.getSecret(), WeGoConstants.TRANSPORT_GET, bizParams, "查询派送渠道");
    }

    /**
     * 调用 WEGO inorder.save 创建或修改入库单。
     * <p>
     * 入参 {@link WegoInOrderSaveDTO.SaveReqDTO#getNo()} 为空时表示新增；非空时表示修改对应单据。
     * <p>
     * 注意：明细 {@code details} 会通过 fastjson 转为 {@code List<Map>} 后再参与签名，
     * 与 {@link WeGoSignUtils#buildSignContent(Map)} 中对嵌套 Map 的递归 ASCII 排序保持一致，
     * 避免 POJO 字段顺序导致客户端与服务端签名不一致。
     *
     * @param dto 入库单创建/修改请求，包含 accessToken / secret / 业务字段
     * @return WEGO 接口原始响应解析后的 JSONObject（含 success / errorCode / errorMsg / serverTime / result 等字段）
     */
    public JSONObject saveInorder(@Valid WegoInOrderSaveDTO.SaveReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        putIfNotNull(bizParams, "no", dto.getNo());
        putIfNotNull(bizParams, "warehouseBusiness", dto.getWarehouseBusiness());
        putIfNotNull(bizParams, "warehouseCode", dto.getWarehouseCode());
        putIfNotNull(bizParams, "warehouseDelivery", dto.getWarehouseDelivery());
        putIfNotNull(bizParams, "inventoryType", dto.getInventoryType());
        putIfNotNull(bizParams, "expectedArrivalDate", dto.getExpectedArrivalDate());
        putIfNotNull(bizParams, "trackNumber", dto.getTrackNumber());
        putIfNotNull(bizParams, "referenceNumber", dto.getReferenceNumber());
        putIfNotNull(bizParams, "notes", dto.getNotes());
        if (dto.getDetails() != null) {
            // 将 POJO 列表转换为 JSONArray (List<JSONObject>)，让内层 Map 统一为 LinkedHashMap：
            // 这样后续两次 fastjson 序列化（实际发送 + 签名计算）输出的字段顺序完全一致，
            // 与服务端 Jackson writeValueAsString(JsonNode) 保留原始顺序的行为对齐。
            bizParams.put("details", JSON.parse(JSON.toJSONString(dto.getDetails())));
        }
        return doQuery(dto.getAccessToken(), dto.getSecret(), WeGoConstants.INORDER_SAVE, bizParams, "保存入库单");
    }

    /**
     * 仅在 value 非 null 时写入 map。
     * <p>
     * 说明：根据 WEGO 官方示例 {@code signRequest} 的实现，服务端对所有非 null 顶层字段
     * （含空字符串）都参与签名（{@code JsonNode.asText()} 对空串返回 {@code ""}），
     * 因此客户端只过滤 null，空字符串字段需要保留并参与签名 / 透传，与服务端字段集对齐。
     * <p>
     * fastjson 默认不会把 null 字段序列化到请求体，所以这里跳过 null 后，
     * 「实际发送的字段集」与「签名计算的字段集」严格一致。
     */
    private void putIfNotNull(Map<String, Object> params, String key, Object value) {
        if (value == null) {
            return;
        }
        params.put(key, value);
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
