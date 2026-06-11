package com.sdk.wms.wego.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.wms.dto.WegoInOrderCancelDTO;
import com.erp.model.wms.dto.WegoInOrderQueryPageDTO;
import com.erp.model.wms.dto.WegoInOrderSaveDTO;
import com.erp.model.wms.dto.WegoInventoryQueryDTO;
import com.erp.model.wms.dto.WegoOutboundInterceptDTO;
import com.erp.model.wms.dto.WegoOutboundQueryPageDTO;
import com.erp.model.wms.dto.WegoOutboundSaveDTO;
import com.erp.model.wms.dto.WegoOutboundSearchDTO;
import com.erp.model.wms.dto.WegoTransportQueryDTO;
import com.erp.model.wms.dto.WegoSkuQueryDTO;
import com.erp.model.wms.dto.WegoWarehouseQueryDTO;
import com.sdk.wms.wego.constants.WeGoConstants;
import com.sdk.wms.wego.dto.response.WegoInboundResp;
import com.sdk.wms.wego.dto.response.WegoOutboundResp;
import com.sdk.wms.wego.utils.WeGoSignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.*;

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
     * 调用 WEGO inorder.queryPage 分页查询入库单。
     * <p>
     * 服务端约束：单页最大 {@value WegoInOrderQueryPageDTO#MAX_PAGE_SIZE} 条；
     * 调用方需根据响应 {@link WegoInboundResp.PageResultDTO#getPages()} 字段判断是否还有下一页，
     * 当 {@code pageNum >= pages} 或 {@code list} 为空时结束分页。
     *
     * @param dto 入参，包含 accessToken / secret / 日期范围 / pageNum / pageSize
     * @return WEGO 接口原始响应解析后的强类型 {@link WegoInboundResp}
     */
    public WegoInboundResp queryInorderPage(@Valid WegoInOrderQueryPageDTO.QueryReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("pageNum", dto.getPageNum());
        bizParams.put("pageSize", dto.getPageSize());
        putIfNotNull(bizParams, "finishDateBegin", dto.getFinishDateBegin());
        putIfNotNull(bizParams, "finishDateEnd", dto.getFinishDateEnd());
        putIfNotNull(bizParams, "orderDateBegin", dto.getOrderDateBegin());
        putIfNotNull(bizParams, "orderDateEnd", dto.getOrderDateEnd());
        putIfNotNull(bizParams, "upDateBegin", dto.getUpDateBegin());
        putIfNotNull(bizParams, "upDateEnd", dto.getUpDateEnd());
        JSONObject response = doQuery(dto.getAccessToken(), dto.getSecret(),
                WeGoConstants.INORDER_QUERY_PAGE, bizParams, "分页查询入库单");
        if (response == null) {
            return null;
        }
        try {
            return response.toJavaObject(WegoInboundResp.class);
        } catch (Exception ex) {
            log.error("[WEGO分页查询入库单] 响应JSON转换WegoInboundResp失败, response={}", response, ex);
            throw new ServiceException("WEGO 分页查询入库单接口响应转换失败: " + ex.getMessage());
        }
    }

    /**
     * 调用 WEGO inorder.cancel 取消入库单。
     * <p>
     * WEGO 服务端按 {@code no} 定位入库单并执行取消，仅支持未发起作业的单据。
     * 业务侧应保证传入的单号已在本地切换为取消状态，避免远端取消成功后本地状态不一致。
     *
     * @param dto 入库单取消请求，包含 accessToken / secret / no
     * @return WEGO 接口原始响应解析后的 JSONObject（含 success / errorCode / errorMsg / serverTime / result 等字段）
     */
    public JSONObject cancelInorder(@Valid WegoInOrderCancelDTO.CancelReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("no", dto.getNo());
        return doQuery(dto.getAccessToken(), dto.getSecret(), WeGoConstants.INORDER_CANCEL, bizParams, "取消入库单");
    }

    // ===================== 2C 出库单相关接口 =====================

    /**
     * 调用 WEGO 2c.order.save 创建或修改 2C 出库单。
     * <p>
     * {@link WegoOutboundSaveDTO.SaveReqDTO#getNo()} 为空时创建新出库单；非空时修改。
     * 创建成功后响应 {@code result} 字符串即为 WEGO 出库单号（即 {@code no}），
     * 调用方应将其存入 {@code third_warehouse_delivery.shipping_order_no}。
     *
     * @param dto 2C 出库单创建/修改请求
     * @return WEGO 接口原始响应（含 success / errorCode / errorMsg / result=WEGO单号或错误详情）
     */
    public JSONObject save2cOrder(@Valid WegoOutboundSaveDTO.SaveReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        putIfNotNull(bizParams, "warehouseBusiness", dto.getWarehouseBusiness());
        putIfNotNull(bizParams, "warehouseCode", dto.getWarehouseCode());
        putIfNotNull(bizParams, "no", dto.getNo());
        putIfNotNull(bizParams, "shopName", dto.getShopName());
        putIfNotNull(bizParams, "receiver", dto.getReceiver());
        putIfNotNull(bizParams, "receiverPhone", dto.getReceiverPhone());
        putIfNotNull(bizParams, "receiverPostCode", dto.getReceiverPostCode());
        putIfNotNull(bizParams, "receiverEmail", dto.getReceiverEmail());
        putIfNotNull(bizParams, "receiverProvince", dto.getReceiverProvince());
        putIfNotNull(bizParams, "receiverCity", dto.getReceiverCity());
        putIfNotNull(bizParams, "receiverArea", dto.getReceiverArea());
        putIfNotNull(bizParams, "receiverAddress", dto.getReceiverAddress());
        putIfNotNull(bizParams, "referenceCode", dto.getReferenceCode());
        putIfNotNull(bizParams, "referenceCode2", dto.getReferenceCode2());
        putIfNotNull(bizParams, "remark", dto.getRemark());
        putIfNotNull(bizParams, "sender", dto.getSender());
        putIfNotNull(bizParams, "senderPhone", dto.getSenderPhone());
        putIfNotNull(bizParams, "senderEmail", dto.getSenderEmail());
        putIfNotNull(bizParams, "receiveDate", dto.getReceiveDate());
        putIfNotNull(bizParams, "receiveTime", dto.getReceiveTime());
        putIfNotNull(bizParams, "needSendFlag", dto.getNeedSendFlag());
        putIfNotNull(bizParams, "needPackFlag", dto.getNeedPackFlag());
        putIfNotNull(bizParams, "wayBillType", dto.getWayBillType());
        putIfNotNull(bizParams, "logisticsName", dto.getLogisticsName());
        putIfNotNull(bizParams, "trackRemark", dto.getTrackRemark());
        putIfNotNull(bizParams, "wayBillBase64", dto.getWayBillBase64());
        if (dto.getWayBillUrl() != null) {
            bizParams.put("wayBillUrl", JSON.toJSON(dto.getWayBillUrl()));
        }
        if (dto.getProducts() != null) {
            bizParams.put("products", JSON.toJSON(dto.getProducts()));
        }
        return doQuery(dto.getAccessToken(), dto.getSecret(), WeGoConstants.TWO_C_ORDER_SAVE, bizParams, "创建2C出库单");
    }

    /**
     * 调用 WEGO 2c.order.search 按单号列表精确查询 2C 出库单。
     * <p>
     * 主要用于 Handler 的 {@code queryOutboundBill}：传入 WEGO 出库单号（存于
     * {@code third_warehouse_delivery.shipping_order_no}），返回当前状态、物流跟踪号等信息。
     * 物流跟踪号在响应的 {@code logisticsList[].trackingNum} 字段中，取第一条非空值即可。
     * <p>
     * WEGO 限制：单次最多查询 100 条。
     * <p>
     * 注意：该接口响应的 {@code result} 字段是数组，与 queryPage 的分页对象结构不同，
     * 此处直接解包为 {@link WegoOutboundResp.OutboundOrderDTO} 列表返回，避免 FastJSON 同名字段冲突。
     *
     * @param dto 查询请求，包含 accessToken / secret / noList（WEGO 出库单号列表）
     * @return 出库单详情列表；失败或无数据时返回空列表
     */
    public List<WegoOutboundResp.OutboundOrderDTO> search2cOrder(@Valid WegoOutboundSearchDTO.SearchReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("noList", JSON.toJSON(dto.getNoList()));
        JSONObject response = doQuery(dto.getAccessToken(), dto.getSecret(),
                WeGoConstants.TWO_C_ORDER_SEARCH, bizParams, "查询2C出库单");
        if (response == null || !Boolean.TRUE.equals(response.getBoolean("success"))) {
            return Collections.emptyList();
        }
        JSONArray resultArray = response.getJSONArray("result");
        if (resultArray == null || resultArray.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return resultArray.toJavaList(WegoOutboundResp.OutboundOrderDTO.class);
        } catch (Exception ex) {
            log.error("[WEGO查询2C出库单] result数组转换OutboundOrderDTO失败, response={}", response, ex);
            throw new ServiceException("WEGO 查询2C出库单接口响应转换失败: " + ex.getMessage());
        }
    }

    /**
     * 调用 WEGO 2c.order.queryPage 分页查询 2C 出库单。
     * <p>
     * 主要用于 DMP 定时轮询：传入待轮询的 WEGO 出库单号列表（{@code noList}），
     * 或按 {@code finishDateBegin/End}、{@code orderDateBegin/End} 时间范围拉取有变化的订单。
     * <p>
     * 调用方根据 {@link WegoOutboundResp.PageResultDTO#getPages()} 判断总页数，
     * 当 {@code pageNum >= pages} 或 {@code emptyFlag == true} 时结束分页。
     * <p>
     * WEGO 限制：单次最多返回 100 条（pageSize ≤ 100）。
     *
     * @param dto 分页查询请求（pageNum / pageSize 必填，其余过滤条件可选）
     * @return 分页结果（{@code result} 为分页对象）；接口返回失败或无响应时返回 null，解析失败时抛出 ServiceException
     */
    public WegoOutboundResp query2cOrderPage(@Valid WegoOutboundQueryPageDTO.QueryReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("pageNum", dto.getPageNum());
        bizParams.put("pageSize", dto.getPageSize());
        if (dto.getNoList() != null && !dto.getNoList().isEmpty()) {
            bizParams.put("noList", JSON.toJSON(dto.getNoList()));
        }
        putIfNotNull(bizParams, "finishDateBegin", dto.getFinishDateBegin());
        putIfNotNull(bizParams, "finishDateEnd", dto.getFinishDateEnd());
        putIfNotNull(bizParams, "orderDateBegin", dto.getOrderDateBegin());
        putIfNotNull(bizParams, "orderDateEnd", dto.getOrderDateEnd());
        JSONObject response = doQuery(dto.getAccessToken(), dto.getSecret(),
                WeGoConstants.TWO_C_ORDER_QUERY_PAGE, bizParams, "分页查询2C出库单");
        if (response == null || !Boolean.TRUE.equals(response.getBoolean("success"))) {
            log.warn("[WEGO分页查询2C出库单] 接口返回失败或无响应, response={}", response);
            return null;
        }
        try {
            return response.toJavaObject(WegoOutboundResp.class);
        } catch (Exception ex) {
            log.error("[WEGO分页查询2C出库单] 响应JSON转换WegoOutboundResp失败, response={}", response, ex);
            throw new ServiceException("WEGO 分页查询2C出库单接口响应转换失败: " + ex.getMessage());
        }
    }

    /**
     * 调用 WEGO 2c.order.intercept 截单（取消）2C 出库单。
     * <p>
     * 截单规则（来自 WEGO 官方文档）：
     * <ul>
     *     <li>「已提交」前的订单可直接取消；</li>
     *     <li>「拣货中/已拣货」视为仓内拦截，产生操作费，无物流费用；</li>
     *     <li>「已出库」无法线上拦截。</li>
     * </ul>
     * 截单成功返回 {@code success=true}；失败返回 {@code success=false}
     * （如 errorCode=2004 errorMsg="截单失败"）。
     * 调用方根据 {@code success} 字段返回 {@code ThirdWarehouseCancelResultEnum}。
     *
     * @param dto 截单请求，仅需 WEGO 出库单号 {@code no}
     * @return WEGO 接口原始响应（含 success / errorCode / errorMsg / result=截单后订单数组）
     */
    public JSONObject intercept2cOrder(@Valid WegoOutboundInterceptDTO.InterceptReqDTO dto) {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("no", dto.getNo());
        return doQuery(dto.getAccessToken(), dto.getSecret(), WeGoConstants.TWO_C_ORDER_INTERCEPT, bizParams, "截单2C出库单");
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
