package com.sdk.wms.aiya.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.aiya.constants.AiyaConstants;
import com.sdk.wms.aiya.dto.response.AiyaInboundResp;
import com.sdk.wms.aiya.dto.response.AiyaOutboundResp;
import com.sdk.wms.aiya.dto.response.AiyaReturnOrderResp;
import com.sdk.wms.aiya.utils.AiyaSignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AIYA（爱亚）海外仓开放接口 SDK（骨架）。
 * <p>
 * 参照 {@code WegoOpenApiService} 搭建，采用「单网关 + bizData 业务报文 + 签名」模式，
 * 所有接口统一 POST 到 {@link AiyaConstants#ROUTER_PATH}，请求字段为 customerCode / method / bizData / sign。
 * <p>
 * 签名规则见 {@link AiyaSignUtils}：{@code sign = MD5(bizData + partnerKey)}，32 位小写十六进制。
 * <p>
 * 骨架约定：
 * <ul>
 *     <li>凭证不走 OAuth，{@code customerCode}（客户编码）/ {@code partnerKey}（合作方密钥）
 *         由调用方从 overseas_provider.auth_json 取出后传入（对应下方参数 accessToken / secret）；</li>
 *     <li>{@code partnerKey} 仅用于本地签名，不会发给第三方；</li>
 *     <li>业务参数统一通过 {@code bizParams} 透传，序列化为 {@code bizData} 后参与签名。</li>
 * </ul>
 * TODO：对接 AIYA 真实文档时，需确认 bizData 报文格式（XML/JSON）、外层请求字段名及各接口标识，
 * 并可参照 wego 将 {@code bizParams} 收敛为强类型请求 DTO（建议放 erp-model-wms，命名 {@code Aiya*DTO}）。
 */
@Slf4j
@Component
@Validated
public class AiyaOpenApiService {

    /**
     * AIYA 外层请求保留字段，禁止由业务透传参数覆盖。
     */
    private static final Set<String> AIYA_RESERVED_PARAM_KEYS =
            new HashSet<>(Arrays.asList("customerCode", "method", "bizData", AiyaSignUtils.SIGN_FIELD));

    /**
     * 分页查询保留参数，避免分页参数被业务透传覆盖。
     */
    private static final Set<String> PAGE_RESERVED_PARAM_KEYS =
            new HashSet<>(Arrays.asList("pageNum", "pageSize"));

    /**
     * 原始响应字符串在日志中打印的最大长度。
     */
    private static final int RAW_RESPONSE_LOG_MAX_LEN = 500;

    /**
     * 根据当前激活的 Spring profile 选择 AIYA 接口域名。
     *
     * @return AIYA 网关地址（生产或测试）
     */
    private String getPreUrl() {
        if (BusinessCommonConstants.hasProfile("prod")) {
            return AiyaConstants.BASE_URL_PROD;
        } else {
            return AiyaConstants.BASE_URL;
        }
    }

    /**
     * 调用 AIYA warehouse.get 查询仓库列表。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret（仅用于本地签名）
     * @param bizParams   业务扩展参数（可为 null）
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject queryWarehouse(String accessToken, String secret, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        mergeBizParams(params, bizParams, "查询仓库", Collections.emptySet());
        return doQuery(accessToken, secret, AiyaConstants.GLINK_QUERY_WAREHOUSE_NOTIFY, params, "查询仓库");
    }

    /**
     * 调用 AIYA product.search 分页查询 SKU 列表。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param pageNum     页码（从 1 开始）
     * @param pageSize    每页数量
     * @param bizParams   业务扩展参数（可为 null）
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject querySku(String accessToken, String secret, int pageNum, int pageSize, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        mergeBizParams(params, bizParams, "查询SKU", PAGE_RESERVED_PARAM_KEYS);
        return doQuery(accessToken, secret, AiyaConstants.GLINK_QUERY_ITEM_NOTIFY, params, "查询SKU");
    }

    /**
     * 调用 AIYA 2c.inventory.search 分页查询 2C 库存列表。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param pageNum     页码（从 1 开始）
     * @param pageSize    每页数量
     * @param bizParams   业务扩展参数（如 warehouseCode，可为 null）
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject queryInventory(String accessToken, String secret, int pageNum, int pageSize, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        mergeBizParams(params, bizParams, "查询库存", PAGE_RESERVED_PARAM_KEYS);
        return doQuery(accessToken, secret, AiyaConstants.TWO_C_INVENTORY_SEARCH, params, "查询库存");
    }

    /**
     * 调用 AIYA transport.get 查询派送渠道列表。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param bizParams   业务扩展参数（可为 null）
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject queryTransport(String accessToken, String secret, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        mergeBizParams(params, bizParams, "查询派送渠道", Collections.emptySet());
        return doQuery(accessToken, secret, AiyaConstants.GLINK_QUERY_CARRIER_NOTIFY, params, "查询派送渠道");
    }

    /**
     * 调用 AIYA inorder.save 创建或修改入库单。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param bizParams   入库单业务字段
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject saveInorder(String accessToken, String secret, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        mergeBizParams(params, bizParams, "保存入库单", Collections.emptySet());
        return doQuery(accessToken, secret, AiyaConstants.INORDER_SAVE, params, "保存入库单");
    }

    /**
     * 调用 AIYA inorder.queryPage 分页查询入库单。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param pageNum     页码（从 1 开始）
     * @param pageSize    每页数量
     * @param bizParams   过滤条件（可为 null）
     * @return AIYA 接口原始响应解析后的强类型 {@link AiyaInboundResp}；无响应时返回 null
     */
    public AiyaInboundResp queryInorderPage(String accessToken, String secret, int pageNum, int pageSize, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        mergeBizParams(params, bizParams, "分页查询入库单", PAGE_RESERVED_PARAM_KEYS);
        JSONObject response = doQuery(accessToken, secret, AiyaConstants.INORDER_QUERY_PAGE, params, "分页查询入库单");
        if (response == null) {
            return null;
        }
        try {
            return response.toJavaObject(AiyaInboundResp.class);
        } catch (Exception ex) {
            log.error("[AIYA分页查询入库单] 响应JSON转换AiyaInboundResp失败, response={}", safeResponseLog(response), ex);
            throw new ServiceException(ApiError.WH_AIYA_SDK_INBOUND_PAGE_CONVERT_FAILED, ex.getMessage());
        }
    }

    /**
     * 调用 AIYA inorder.cancel 取消入库单。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param no          AIYA 入库单号
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject cancelInorder(String accessToken, String secret, String no) {
        Map<String, Object> params = new HashMap<>();
        params.put("no", no);
        return doQuery(accessToken, secret, AiyaConstants.INORDER_CANCEL, params, "取消入库单");
    }

    // ===================== 2C 出库单相关接口 =====================

    /**
     * 调用 AIYA 2c.order.save 创建或修改 2C 出库单。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param bizParams   出库单业务字段
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject save2cOrder(String accessToken, String secret, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        mergeBizParams(params, bizParams, "创建2C出库单", Collections.emptySet());
        return doQuery(accessToken, secret, AiyaConstants.TWO_C_ORDER_SAVE, params, "创建2C出库单");
    }

    /**
     * 调用 AIYA 2c.order.search 按单号列表精确查询 2C 出库单。
     * <p>
     * 与 wego 保持一致：接口返回 {@code success=false} 视为真实失败，抛出 {@link ServiceException}；
     * 仅当调用成功但 {@code result} 为空数组时返回空列表。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param noList      AIYA 出库单号列表
     * @return 出库单详情列表；调用成功但无匹配单据时返回空列表
     */
    public List<AiyaOutboundResp.OutboundOrderDTO> search2cOrder(String accessToken, String secret, List<String> noList) {
        Map<String, Object> params = new HashMap<>();
        params.put("noList", JSON.toJSON(noList));
        JSONObject response = doQuery(accessToken, secret, AiyaConstants.TWO_C_ORDER_SEARCH, params, "查询2C出库单");
        if (response == null) {
            log.error("[AIYA查询2C出库单] 接口无响应");
            throw new ServiceException(ApiError.WH_AIYA_SDK_OUTBOUND_SEARCH_NO_RESPONSE);
        }
        if (!Boolean.TRUE.equals(response.getBoolean("success"))) {
            log.error("[AIYA查询2C出库单] 接口返回失败, {}", safeResponseLog(response));
            throw new ServiceException(ApiError.WH_AIYA_SDK_OUTBOUND_SEARCH_FAILED, response.getString("errorMsg"));
        }
        JSONArray resultArray = response.getJSONArray("result");
        if (resultArray == null || resultArray.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return resultArray.toJavaList(AiyaOutboundResp.OutboundOrderDTO.class);
        } catch (Exception ex) {
            log.error("[AIYA查询2C出库单] result数组转换OutboundOrderDTO失败, {}", safeResponseLog(response), ex);
            throw new ServiceException(ApiError.WH_AIYA_SDK_OUTBOUND_SEARCH_CONVERT_FAILED, ex.getMessage());
        }
    }

    /**
     * 调用 AIYA 2c.order.queryPage 分页查询 2C 出库单。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param pageNum     页码（从 1 开始）
     * @param pageSize    每页数量
     * @param bizParams   过滤条件（可为 null）
     * @return 分页结果；无响应时返回 null
     */
    public AiyaOutboundResp query2cOrderPage(String accessToken, String secret, int pageNum, int pageSize, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        mergeBizParams(params, bizParams, "分页查询2C出库单", PAGE_RESERVED_PARAM_KEYS);
        JSONObject response = doQuery(accessToken, secret, AiyaConstants.TWO_C_ORDER_QUERY_PAGE, params, "分页查询2C出库单");
        if (response == null) {
            log.warn("[AIYA分页查询2C出库单] 接口无响应");
            return null;
        }
        try {
            return response.toJavaObject(AiyaOutboundResp.class);
        } catch (Exception ex) {
            log.error("[AIYA分页查询2C出库单] 响应JSON转换AiyaOutboundResp失败, {}", safeResponseLog(response), ex);
            throw new ServiceException(ApiError.WH_AIYA_SDK_OUTBOUND_PAGE_CONVERT_FAILED, ex.getMessage());
        }
    }

    /**
     * 调用 AIYA 2c.order.intercept 截单（取消）2C 出库单。
     *
     * @param accessToken AIYA accessToken
     * @param secret      AIYA secret
     * @param no          AIYA 出库单号
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject intercept2cOrder(String accessToken, String secret, String no) {
        Map<String, Object> params = new HashMap<>();
        params.put("no", no);
        return doQuery(accessToken, secret, AiyaConstants.TWO_C_ORDER_INTERCEPT, params, "截单2C出库单");
    }

    /**
     * 调用 AIYA returnorder.queryPage 分页查询退货订单。
     *
     * @param accessToken      AIYA accessToken
     * @param secret           AIYA secret
     * @param arrivalDateBegin 到仓日期开始（YYYY-MM-DD，可为 null）
     * @param arrivalDateEnd   到仓日期结束（YYYY-MM-DD，可为 null）
     * @param pageNum          页码（从 1 开始）
     * @param pageSize         每页数量
     * @return 分页结果；无响应时返回 null
     */
    public AiyaReturnOrderResp queryReturnOrderPage(String accessToken, String secret,
                                                    String arrivalDateBegin, String arrivalDateEnd,
                                                    int pageNum, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        putIfNotNull(params, "arrivalDateBegin", arrivalDateBegin);
        putIfNotNull(params, "arrivalDateEnd", arrivalDateEnd);
        JSONObject response = doQuery(accessToken, secret, AiyaConstants.RETURN_ORDER_QUERY_PAGE, params, "分页查询退货订单");
        if (response == null) {
            log.warn("[AIYA分页查询退货订单] 接口无响应");
            return null;
        }
        try {
            return response.toJavaObject(AiyaReturnOrderResp.class);
        } catch (Exception ex) {
            log.error("[AIYA分页查询退货订单] 响应JSON转换AiyaReturnOrderResp失败, {}", safeResponseLog(response), ex);
            throw new ServiceException(ApiError.WH_AIYA_SDK_RETURN_ORDER_PAGE_CONVERT_FAILED, ex.getMessage());
        }
    }

    /**
     * 仅在 value 非 null 时写入 map。
     */
    private void putIfNotNull(Map<String, Object> params, String key, Object value) {
        if (value == null) {
            return;
        }
        params.put(key, value);
    }

    /**
     * AIYA 通用查询方法：将业务参数序列化为 bizData、按 {@code MD5(bizData + partnerKey)} 计算签名并执行 HTTP 请求。
     *
     * @param accessToken   AIYA customerCode（客户编码，随请求发送）
     * @param secret        AIYA partnerKey（合作方密钥，仅用于本地签名，不发送）
     * @param interfaceType AIYA 接口标识（外层 method 字段），如 warehouse.get / product.search
     * @param bizParams     业务参数（序列化为 bizData 后参与签名）
     * @param actionName    日志中的业务动作名
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    private JSONObject doQuery(String accessToken, String secret, String interfaceType, Map<String, Object> bizParams, String actionName) {
        long start = System.currentTimeMillis();
        Map<String, Object> bizDataMap = new HashMap<>();
        mergeBizParams(bizDataMap, bizParams, actionName, Collections.emptySet());
        // AIYA：bizData 为业务参数序列化字符串，签名 = MD5(bizData + partnerKey)
        String bizData = JSON.toJSONString(bizDataMap);
        String sign = AiyaSignUtils.sign(bizData, secret);

        Map<String, Object> params = new HashMap<>();
        params.put("customerCode", accessToken);
        params.put("method", interfaceType);
        params.put("bizData", bizData);
        params.put(AiyaSignUtils.SIGN_FIELD, sign);

        String url = buildRouterUrl(getPreUrl());
        String requestJson = JSON.toJSONString(params);
        String logRequestJson = JSON.toJSONString(maskLogParams(params));
        log.info("[AIYA{}] 请求开始, url={}, params={}", actionName, url, logRequestJson);
        String response;
        try {
            response = OkHttpUtils.doPostJson(url, params, null);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[AIYA{}] HTTP调用异常, url={}, cost={}ms, params={}", actionName, url, cost, logRequestJson, e);
            throw new ServiceException(e, ApiError.WH_AIYA_SDK_API_CALL_ERROR, actionName, e.getMessage());
        }
        long cost = System.currentTimeMillis() - start;
        log.info("[AIYA{}] 请求结束, cost={}ms", actionName, cost);
        if (response == null || response.isEmpty()) {
            log.error("[AIYA{}] 接口返回为空, url={}, params={}", actionName, url, logRequestJson);
            throw new ServiceException(ApiError.WH_AIYA_SDK_API_RESPONSE_EMPTY, actionName);
        }
        ThirdWarehouseContext.setRequestJson(requestJson);
        ThirdWarehouseContext.setResponseJson(response);
        try {
            return JSON.parseObject(response);
        } catch (Exception ex) {
            log.error("[AIYA{}] 响应JSON解析失败, response={}", actionName, truncateRawResponse(response), ex);
            throw new ServiceException(ApiError.WH_AIYA_SDK_API_RESPONSE_NOT_JSON, actionName);
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
                log.warn("[AIYA{}] 忽略受保护参数key={}", actionName, key);
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
        if (AIYA_RESERVED_PARAM_KEYS.contains(key)) {
            return true;
        }
        return extraReservedKeys != null && extraReservedKeys.contains(key);
    }

    /**
     * 日志参数脱敏：customerCode / sign 打码；bizData 可能含收件人等 PII，仅记录截断内容。
     */
    private Map<String, Object> maskLogParams(Map<String, Object> params) {
        Map<String, Object> logParams = new HashMap<>(params);
        if (logParams.containsKey("customerCode")) {
            logParams.put("customerCode", "***");
        }
        if (logParams.containsKey(AiyaSignUtils.SIGN_FIELD)) {
            logParams.put(AiyaSignUtils.SIGN_FIELD, "***");
        }
        Object bizData = logParams.get("bizData");
        if (bizData instanceof String) {
            logParams.put("bizData", truncateRawResponse((String) bizData));
        }
        return logParams;
    }

    /**
     * 截断原始响应字符串，避免解析失败时将大体积/含 PII 的完整响应写入日志。
     */
    private String truncateRawResponse(String response) {
        if (response == null) {
            return "null";
        }
        if (response.length() <= RAW_RESPONSE_LOG_MAX_LEN) {
            return response;
        }
        return response.substring(0, RAW_RESPONSE_LOG_MAX_LEN) + "...(truncated, length=" + response.length() + ")";
    }

    /**
     * 从 AIYA 响应中提取可安全打印的字段（success / errorCode / errorMsg）。
     */
    private String safeResponseLog(JSONObject response) {
        if (response == null) {
            return "response=null";
        }
        return String.format("success=%s, errorCode=%s, errorMsg=%s",
                response.get("success"),
                response.get("errorCode"),
                response.get("errorMsg"));
    }

    /**
     * 构造 AIYA Router 请求地址。
     *
     * @param domain AIYA 网关域名
     * @return 标准化后的 router 完整地址
     */
    private String buildRouterUrl(String domain) {
        String normalized = domain == null ? "" : domain.trim();
        if (normalized.isEmpty()) {
            throw new ServiceException(ApiError.WH_AIYA_SDK_DOMAIN_EMPTY);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized + AiyaConstants.ROUTER_PATH;
    }
}
