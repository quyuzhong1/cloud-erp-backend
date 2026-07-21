package com.sdk.wms.aiya.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.wms.dto.AiyaInboundCancelDTO;
import com.erp.model.wms.dto.AiyaInboundQueryDTO;
import com.erp.model.wms.dto.AiyaInventoryQueryDTO;
import com.erp.model.wms.dto.AiyaSkuQueryDTO;
import com.sdk.wms.aiya.constants.AiyaConstants;
import com.sdk.wms.aiya.dto.response.AiyaInboundResp;
import com.sdk.wms.aiya.dto.response.AiyaOutboundResp;
import com.sdk.wms.aiya.dto.response.AiyaReturnOrderResp;
import com.sdk.wms.aiya.utils.AiyaSignUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.*;

/**
 * AIYA（爱亚）海外仓开放接口 SDK（骨架）。
 * <p>
 * 参照 {@code WegoOpenApiService} 搭建，采用「单网关 + bizData 业务报文 + 签名」模式，
 * 所有接口统一以 {@code x-www-form-urlencoded} 表单 POST 到爱亚网关地址（{@code getPreUrl()}），
 * 外层表单字段为 partnerId / serviceType / bizData / sign（非 JSON body），
 * 业务字段序列化进 bizData，其中 {@code customerCode}（客户code）为爱亚所有接口必填业务参数，由 SDK 统一注入。
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
     * <p>
     * 真实网关外层字段为 partnerId（客户ID）/ serviceType（接口类型）/ bizData（业务数据）/ sign（签名），
     * 业务字段（如 customerCode）统一放入 bizData，不属于保留字段。
     */
    private static final Set<String> AIYA_RESERVED_PARAM_KEYS =
            new HashSet<>(Arrays.asList("partnerId", "serviceType", "bizData", AiyaSignUtils.SIGN_FIELD));

    /**
     * 爱亚所有接口必填的业务参数 key：客户code。
     */
    private static final String BIZ_PARAM_CUSTOMER_CODE = "customerCode";

    /**
     * 分页查询保留参数，避免分页参数被业务透传覆盖。
     */
    private static final Set<String> PAGE_RESERVED_PARAM_KEYS =
            new HashSet<>(Arrays.asList("pageNum", "pageSize"));

    /**
     * SKU 查询专用保留参数：文档字段名为 {@code page}（而非其它接口的 {@code pageNum}），
     * 避免分页/状态过滤/时间范围参数被业务透传覆盖。
     * <p>
     * {@code createdTimeFrom}/{@code createdTimeTo}：2026-07-16 联调发现文档未列出的真实必填约束
     * （爱亚网关底层 QERP Open API Platform {@code GLINK_QUERY_ITEM_NOTIFY} 规范）——{@code skus}、
     * createdTime 范围、updatedTime 范围三者至少要有一组非空，否则报
     * {@code INVALID_DATA: Created time and Updated time and SKUs cannot be both empty}。
     */
    private static final Set<String> SKU_QUERY_RESERVED_PARAM_KEYS =
            new HashSet<>(Arrays.asList("page", "pageSize", "status", "createdTimeFrom", "createdTimeTo"));

    /**
     * 库存查询专用保留参数：文档字段名同样为 {@code page}（而非 {@code pageNum}），
     * 且 {@code warehouseCode}/{@code ignoreZero}/{@code skus}/{@code stockStatus}/{@code domainCode}
     * 均由 DTO 一等字段承载，避免业务透传参数覆盖。
     * <p>
     * 注意：不含 {@code status}——2026-07-16 核对接口文档截图后确认库存查询接口没有该字段
     * （与 SKU 查询接口混淆所致），已从 DTO 移除，故此处不再保留。
     */
    private static final Set<String> INVENTORY_QUERY_RESERVED_PARAM_KEYS =
            new HashSet<>(Arrays.asList("page", "pageSize", "warehouseCode", "ignoreZero", "skus", "stockStatus", "domainCode"));

    /**
     * 入库单批量查询专用保留参数：文档字段名为 {@code page}（而非 {@code pageNum}），
     * 以及分页/仓库/时间范围等由 DTO 一等字段承载的 key，避免业务透传参数覆盖。
     */
    private static final Set<String> BATCH_QUERY_ASN_RESERVED_PARAM_KEYS =
            new HashSet<>(Arrays.asList("page", "pageSize", "warehouseCode",
                    "putawayCompletedTimeFrom", "putawayCompletedTimeTo",
                    "asnNumbers", "asnType", "receiveTimeFrom", "receiveTimeTo",
                    "createdTimeFrom", "createdTimeTo", "lastUpdatedTimeFrom", "lastUpdatedTimeTo",
                    "stage", "ifNeedBatchInfo", "refNumbers", "extUserId"));

    /**
     * 原始响应字符串在日志中打印的最大长度。
     * SKU 查询排查时空结果需要看到 total/itemList 结构，适当加大截断上限。
     */
    private static final int RAW_RESPONSE_LOG_MAX_LEN = 2000;

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
     * 调用 AIYA {@code GLINK_QUERY_WAREHOUSE_NOTIFY} 查询仓库列表。
     * <p>
     * 响应结构：{@code {code, message, success, resultList:[{warehouseCode, warehouseDescription, country, ...}]}}。
     *
     * @param partnerId    AIYA partnerId（客户ID）
     * @param partnerKey   AIYA partnerKey（仅用于本地签名）
     * @param customerCode AIYA 客户code（必填业务参数）
     * @param bizParams    其它业务参数（可为 null）
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject queryWarehouse(String partnerId, String partnerKey, String customerCode, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        mergeBizParams(params, bizParams, "查询仓库", Collections.emptySet());
        return doQuery(partnerId, partnerKey, customerCode, AiyaConstants.GLINK_QUERY_WAREHOUSE_NOTIFY, params, "查询仓库");
    }

    /**
     * 调用 AIYA {@code GLINK_QUERY_ITEM_NOTIFY} 分页查询 SKU（商品）列表。
     * <p>
     * 按《爱亚海外仓对接方案文档》商品注册/查询接口：请求字段为 {@code page}（注意不是其它接口的
     * {@code pageNum}）/ {@code pageSize}（默认200）/ 可选 {@code status}（商品使用状态）；
     * 响应结构为 {@code {code, message, success, itemList:[...]}}，与 warehouse/inventory
     * 接口的 {@code result}/{@code resultList} 结构不同，调用方需按 {@code itemList} 解析。
     * <p>
     * 2026-07-16 联调发现文档未列出的真实必填约束（爱亚网关底层 QERP Open API Platform
     * {@code GLINK_QUERY_ITEM_NOTIFY} 规范）——{@code skus}、{@code createdTimeFrom}/
     * {@code createdTimeTo}、{@code updatedTimeFrom}/{@code updatedTimeTo} 三者至少要有一组非空，
     * 否则报 {@code INVALID_DATA: Created time and Updated time and SKUs cannot be both empty}；
     * 本方法固定按 {@link AiyaSkuQueryDTO.QueryReqDTO#getCreatedTimeFrom()}/
     * {@link AiyaSkuQueryDTO.QueryReqDTO#getCreatedTimeTo()} 满足该约束，调用方需保证成对非空传入
     * （具体锚点时间由 {@code AiyaSkuInitHandler} 决定，用于全量拉取）。
     * <p>
     * TODO：文档未说明分页是否有 {@code total}/{@code pages} 等终止字段，翻页终止条件（如
     * {@code itemList.size() < pageSize}）需联调真实接口后确认。
     *
     * @param dto 查询请求，包含 accessToken / secret / customerCode / pageNum(对应文档page) / pageSize /
     *            status / createdTimeFrom / createdTimeTo
     * @return AIYA 接口原始响应解析后的 JSONObject（含 code / message / success / itemList 等字段）
     */
    public JSONObject querySku(@Valid AiyaSkuQueryDTO.QueryReqDTO dto) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", dto.getPageNum());
        params.put("pageSize", dto.getPageSize());
        if (StringUtils.isNotBlank(dto.getStatus())) {
            params.put("status", dto.getStatus());
        }
        if (StringUtils.isNotBlank(dto.getCreatedTimeFrom())) {
            params.put("createdTimeFrom", dto.getCreatedTimeFrom());
        }
        if (StringUtils.isNotBlank(dto.getCreatedTimeTo())) {
            params.put("createdTimeTo", dto.getCreatedTimeTo());
        }
        mergeBizParams(params, dto.getBizParams(), "查询SKU", SKU_QUERY_RESERVED_PARAM_KEYS);
        return doQuery(dto.getAccessToken(), dto.getSecret(), dto.getCustomerCode(),
                AiyaConstants.GLINK_QUERY_ITEM_NOTIFY, params, "查询SKU");
    }

    /**
     * 调用 AIYA {@code 2c.inventory.search} 分页查询库存概要列表。
     * <p>
     * 请求/响应字段已按爱亚开放平台接口文档页面截图核对（2026-07-16），比《爱亚海外仓对接方案文档》
     * 6.2.4「库存数据」翻译稿更权威，请求字段为 {@code customerCode}（必填）/ {@code warehouseCode}
     * （必填）/ {@code page}（可选，{@code skus} 不存在时必填，注意不是其它接口的 {@code pageNum}）/
     * {@code pageSize}（可选，{@code skus} 不存在时必填，默认200）/ 可选 {@code ignoreZero}/{@code skus}/
     * {@code domainCode}（截图新增字段，未给出参数描述，用途未知）/ {@code stockStatus}（文档标"必填"，
     * 但 2026-07-16 真实接口联调已确认实测<b>非必传、推荐不传</b>，不传即查全部状态库存，详见
     * {@link AiyaInventoryQueryDTO}）；<b>没有</b> {@code status} 字段（翻译稿里有，疑似跟
     * SKU 查询接口混淆，已从本方法与 DTO 移除）。
     * <p>
     * 响应结构为 {@code {code, message, success, inventoryVOList:[...]}}（2026-07-16 真实联调响应样例
     * 确认顶层字段为小写 {@code code}，此前文档/注释写的大写 {@code Code} 有误，已修正），与
     * warehouse/inbound 等接口的 {@code resultList}/{@code result} 结构不同，调用方需按
     * {@code inventoryVOList} 解析；真实样例确认 {@code inventoryVOList} 每条明细字段为
     * {@code customerCode}/{@code warehouseCode}/{@code sku}/{@code skuDescription}/
     * {@code barcode}（商品条码，多个用逗号拼接）/{@code skuStatus}（商品状态，真实样例已出现取值
     * {@code GOOD}，疑似与 6.3.2 入库签收段的 {@code skuStatus}（GOOD/DAMAGE）同义——是否会让同一
     * sku 按状态拆成多条明细仍未验证，下游按 warehouseCode+sku 去重时需一并确认是否要把 skuStatus
     * 纳入唯一键，避免良品/不良品明细互相覆盖）/{@code totalQty}/{@code occupiedQty}/
     * {@code salableQty}/{@code duePutawayQty}/{@code unavailableQty}，本方法仍原样返回原始 JSON，
     * 调用方（{@link com.erp.model.wms.dto.AiyaInventoryQueryDTO} 使用方）按需解析。
     * <p>
     * TODO：真实响应样例顶层只有 {@code code}/{@code success}/{@code inventoryVOList} 三个字段，没有
     * {@code total}/{@code pages} 等分页终止字段，翻页终止条件（如
     * {@code inventoryVOList.size() < pageSize}）仍按此实现，尚未验证接口对同批全部返回时的边界表现。
     *
     * @param dto 查询请求，包含 accessToken / secret / customerCode / warehouseCode / pageNum(对应文档page) / pageSize 等
     * @return AIYA 接口原始响应解析后的 JSONObject（含 code / message / success / inventoryVOList 等字段）
     */
    public JSONObject queryInventory(@Valid AiyaInventoryQueryDTO.QueryReqDTO dto) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", dto.getPageNum());
        params.put("pageSize", dto.getPageSize());
        params.put("warehouseCode", dto.getWarehouseCode());
        if (dto.getIgnoreZero() != null) {
            params.put("ignoreZero", dto.getIgnoreZero());
        }
        if (dto.getSkus() != null && !dto.getSkus().isEmpty()) {
            params.put("skus", dto.getSkus());
        }
        if (StringUtils.isNotBlank(dto.getDomainCode())) {
            params.put("domainCode", dto.getDomainCode());
        }
        if (StringUtils.isNotBlank(dto.getStockStatus())) {
            params.put("stockStatus", dto.getStockStatus());
        }
        mergeBizParams(params, dto.getBizParams(), "查询库存", INVENTORY_QUERY_RESERVED_PARAM_KEYS);
        return doQuery(dto.getAccessToken(), dto.getSecret(), dto.getCustomerCode(),
                AiyaConstants.TWO_C_INVENTORY_SEARCH, params, "查询库存");
    }

    /**
     * 调用 AIYA {@code GLINK_QUERY_CARRIER_NOTIFY} 查询派送渠道列表。
     * <p>
     * 请求业务参数（写入 bizData）：{@code warehouseCode}（必填）、{@code customerCode}（由 SDK 注入）、
     * {@code needActualLogistics}（可选）。
     * 响应结构：{@code {code, message, success, resultList:[{logisticsProvider, carrierServiceList:[...]}]}}。
     *
     * @param partnerId    AIYA partnerId（客户ID）
     * @param partnerKey   AIYA partnerKey（仅用于本地签名）
     * @param customerCode AIYA 客户code（必填业务参数）
     * @param bizParams    业务扩展参数（须含 warehouseCode，可为 null 时由调用方保证）
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject queryTransport(String partnerId, String partnerKey, String customerCode, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        mergeBizParams(params, bizParams, "查询派送渠道", Collections.emptySet());
        return doQuery(partnerId, partnerKey, customerCode, AiyaConstants.GLINK_QUERY_CARRIER_NOTIFY, params, "查询派送渠道");
    }

    /**
     * 调用 AIYA inorder.save 创建或修改入库单。
     *
     * @param accessToken  AIYA partnerId（客户ID）
     * @param secret       AIYA partnerKey（仅用于本地签名）
     * @param customerCode AIYA 客户code（必填业务参数）
     * @param bizParams    入库单业务字段
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject saveInorder(String accessToken, String secret, String customerCode, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        mergeBizParams(params, bizParams, "保存入库单", Collections.emptySet());
        return doQuery(accessToken, secret, customerCode, AiyaConstants.GLINK_CREATE_ASN_NOTIFY, params, "保存入库单");
    }

    /**
     * 调用 AIYA {@code GLINK_BATCH_QUERY_ASN_NOTIFY} 按「仓库 + 上架完成时间」范围分页批量查询入库单。
     * <p>
     * 请求业务参数按接口文档承载：{@code customerCode}（由 SDK 注入）/{@code page}/{@code pageSize}/
     * {@code warehouseCode} 为必填，{@code putawayCompletedTimeFrom}/{@code putawayCompletedTimeTo}
     * 本项目固定按上架完成时间窗口拉取（成对必传）；其余字段（asnNumbers/asnType/receiveTime 范围/
     * createdTime 范围/lastUpdatedTime 范围/stage/ifNeedBatchInfo/refNumbers/extUserId）为可选，
     * 非空时才写入 bizData。{@code pageSize} 单页最大 200，调用方按总数多次翻页。
     * <p>
     * {@code GLINK_BATCH_QUERY_ASN_NOTIFY} 响应结构调整解析。
     *
     * @param dto 入库单批量查询请求（含 accessToken/secret/customerCode/warehouseCode/pageNum(对应文档page)/
     *            pageSize/putawayCompletedTimeFrom/putawayCompletedTimeTo 等）
     * @return AIYA 接口原始响应解析后的强类型 {@link AiyaInboundResp}；无响应时返回 null
     */
    public AiyaInboundResp batchQueryAsn(@Valid AiyaInboundQueryDTO.QueryReqDTO dto) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", dto.getPageNum());
        params.put("pageSize", dto.getPageSize());
        params.put("warehouseCode", dto.getWarehouseCode());
        params.put("putawayCompletedTimeFrom", dto.getPutawayCompletedTimeFrom());
        params.put("putawayCompletedTimeTo", dto.getPutawayCompletedTimeTo());
        if (dto.getAsnNumbers() != null && !dto.getAsnNumbers().isEmpty()) {
            params.put("asnNumbers", JSON.toJSON(dto.getAsnNumbers()));
        }
        if (dto.getRefNumbers() != null && !dto.getRefNumbers().isEmpty()) {
            params.put("refNumbers", JSON.toJSON(dto.getRefNumbers()));
        }
        if (dto.getIfNeedBatchInfo() != null) {
            params.put("ifNeedBatchInfo", dto.getIfNeedBatchInfo());
        }
        putIfNotBlank(params, "asnType", dto.getAsnType());
        putIfNotBlank(params, "receiveTimeFrom", dto.getReceiveTimeFrom());
        putIfNotBlank(params, "receiveTimeTo", dto.getReceiveTimeTo());
        putIfNotBlank(params, "createdTimeFrom", dto.getCreatedTimeFrom());
        putIfNotBlank(params, "createdTimeTo", dto.getCreatedTimeTo());
        putIfNotBlank(params, "lastUpdatedTimeFrom", dto.getLastUpdatedTimeFrom());
        putIfNotBlank(params, "lastUpdatedTimeTo", dto.getLastUpdatedTimeTo());
        putIfNotBlank(params, "stage", dto.getStage());
        putIfNotBlank(params, "extUserId", dto.getExtUserId());
        mergeBizParams(params, dto.getBizParams(), "批量查询入库单", BATCH_QUERY_ASN_RESERVED_PARAM_KEYS);
        JSONObject response = doQuery(dto.getAccessToken(), dto.getSecret(), dto.getCustomerCode(),
                AiyaConstants.GLINK_BATCH_QUERY_ASN_NOTIFY, params, "批量查询入库单");
        if (response == null) {
            return null;
        }
        try {
            return response.toJavaObject(AiyaInboundResp.class);
        } catch (Exception ex) {
            log.error("[AIYA批量查询入库单] 响应JSON转换AiyaInboundResp失败, response={}", safeResponseLog(response), ex);
            throw new ServiceException(ApiError.WH_AIYA_SDK_INBOUND_PAGE_CONVERT_FAILED, ex.getMessage());
        }
    }

    /**
     * 调用 AIYA {@code GLINK_CANCEL_ASN_NOTIFY} 取消入库单。
     * <p>
     * 按接口文档仅传必填业务字段：{@code asnNumbers[]}（ASN 编码数组，单个 ≤ 64）与
     * {@code customerCode}（由 {@link #doQuery} 统一注入）。响应为 {@code {success, code, message}}。
     *
     * @param accessToken  AIYA partnerId（客户ID）
     * @param secret       AIYA partnerKey（仅用于本地签名）
     * @param customerCode AIYA 客户code（必填业务参数）
     * @param request      取消入库单请求（必填 asnNumbers）
     * @return AIYA 接口原始响应解析后的 JSONObject（含 success / code / message）
     */
    public JSONObject cancelInorder(String accessToken, String secret, String customerCode, @Valid AiyaInboundCancelDTO request) {
        Map<String, Object> params = new HashMap<>();
        params.put("asnNumbers", JSON.toJSON(request.getAsnNumbers()));
        return doQuery(accessToken, secret, customerCode, AiyaConstants.GLINK_CANCEL_ASN_NOTIFY, params, "取消入库单");
    }

    // ===================== 2C 出库单相关接口 =====================

    /**
     * 调用 AIYA 2c.order.save 创建或修改 2C 出库单。
     *
     * @param accessToken  AIYA partnerId（客户ID）
     * @param secret       AIYA partnerKey（仅用于本地签名）
     * @param customerCode AIYA 客户code（必填业务参数）
     * @param bizParams    出库单业务字段
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject save2cOrder(String accessToken, String secret, String customerCode, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        mergeBizParams(params, bizParams, "创建2C出库单", Collections.emptySet());
        return doQuery(accessToken, secret, customerCode, AiyaConstants.TWO_C_ORDER_SAVE, params, "创建2C出库单");
    }

    /**
     * 调用 AIYA 2c.order.search 按单号列表精确查询 2C 出库单。
     * <p>
     * 与 wego 保持一致：接口返回 {@code success=false} 视为真实失败，抛出 {@link ServiceException}；
     * 仅当调用成功但 {@code result} 为空数组时返回空列表。
     *
     * @param accessToken  AIYA partnerId（客户ID）
     * @param secret       AIYA partnerKey（仅用于本地签名）
     * @param customerCode AIYA 客户code（必填业务参数）
     * @param noList       AIYA 出库单号列表
     * @return 出库单详情列表；调用成功但无匹配单据时返回空列表
     */
    public List<AiyaOutboundResp.OutboundOrderDTO> search2cOrder(String accessToken, String secret, String customerCode, List<String> noList) {
        Map<String, Object> params = new HashMap<>();
        params.put("noList", JSON.toJSON(noList));
        JSONObject response = doQuery(accessToken, secret, customerCode, AiyaConstants.TWO_C_ORDER_SEARCH, params, "查询2C出库单");
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
     * @param accessToken  AIYA partnerId（客户ID）
     * @param secret       AIYA partnerKey（仅用于本地签名）
     * @param customerCode AIYA 客户code（必填业务参数）
     * @param pageNum      页码（从 1 开始）
     * @param pageSize     每页数量
     * @param bizParams    过滤条件（可为 null）
     * @return 分页结果；无响应时返回 null
     */
    public AiyaOutboundResp query2cOrderPage(String accessToken, String secret, String customerCode, int pageNum, int pageSize, Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        mergeBizParams(params, bizParams, "分页查询2C出库单", PAGE_RESERVED_PARAM_KEYS);
        JSONObject response = doQuery(accessToken, secret, customerCode, AiyaConstants.TWO_C_ORDER_QUERY_PAGE, params, "分页查询2C出库单");
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
     * @param accessToken  AIYA partnerId（客户ID）
     * @param secret       AIYA partnerKey（仅用于本地签名）
     * @param customerCode AIYA 客户code（必填业务参数）
     * @param no           AIYA 出库单号
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    public JSONObject intercept2cOrder(String accessToken, String secret, String customerCode, String no) {
        Map<String, Object> params = new HashMap<>();
        params.put("no", no);
        return doQuery(accessToken, secret, customerCode, AiyaConstants.TWO_C_ORDER_INTERCEPT, params, "截单2C出库单");
    }

    /**
     * 调用 AIYA returnorder.queryPage 分页查询退货订单。
     *
     * @param accessToken      AIYA partnerId（客户ID）
     * @param secret           AIYA partnerKey（仅用于本地签名）
     * @param customerCode     AIYA 客户code（必填业务参数）
     * @param arrivalDateBegin 到仓日期开始（YYYY-MM-DD，可为 null）
     * @param arrivalDateEnd   到仓日期结束（YYYY-MM-DD，可为 null）
     * @param pageNum          页码（从 1 开始）
     * @param pageSize         每页数量
     * @return 分页结果；无响应时返回 null
     */
    public AiyaReturnOrderResp queryReturnOrderPage(String accessToken, String secret, String customerCode,
                                                    String arrivalDateBegin, String arrivalDateEnd,
                                                    int pageNum, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        putIfNotNull(params, "arrivalDateBegin", arrivalDateBegin);
        putIfNotNull(params, "arrivalDateEnd", arrivalDateEnd);
        JSONObject response = doQuery(accessToken, secret, customerCode, AiyaConstants.RETURN_ORDER_QUERY_PAGE, params, "分页查询退货订单");
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
     * 仅在 value 非空白字符串时写入 map。
     */
    private void putIfNotBlank(Map<String, Object> params, String key, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        params.put(key, value);
    }

    /**
     * AIYA 通用查询方法：将业务参数序列化为 bizData、按 {@code MD5(bizData + partnerKey)} 计算签名并执行 HTTP 请求。
     *
     * @param partnerId    AIYA partnerId（客户ID，作为外层 partnerId 字段随请求发送）
     * @param partnerKey   AIYA partnerKey（合作方密钥，仅用于本地签名，不发送）
     * @param customerCode AIYA 客户code（爱亚所有接口必填的业务参数，统一注入 bizData）
     * @param serviceType  AIYA 接口标识（外层 serviceType 字段），如 GLINK_QUERY_WAREHOUSE_NOTIFY
     * @param bizParams    业务参数（序列化为 bizData 后参与签名）
     * @param actionName   日志中的业务动作名
     * @return AIYA 接口原始响应解析后的 JSONObject
     */
    private JSONObject doQuery(String partnerId, String partnerKey, String customerCode, String serviceType,
                               Map<String, Object> bizParams, String actionName) {
        long start = System.currentTimeMillis();
        Map<String, Object> bizDataMap = new HashMap<>();
        mergeBizParams(bizDataMap, bizParams, actionName, Collections.emptySet());
        // 爱亚所有接口业务报文必含 customerCode（客户code），最后写入避免被业务透传参数覆盖
        bizDataMap.put(BIZ_PARAM_CUSTOMER_CODE, customerCode);
        // AIYA：bizData 为业务参数序列化字符串，签名 = MD5(bizData + partnerKey)
        String bizData = JSON.toJSONString(bizDataMap);
        String sign = AiyaSignUtils.sign(bizData, partnerKey);

        Map<String, Object> params = new HashMap<>();
        params.put("partnerId", partnerId);
        params.put("serviceType", serviceType);
        params.put("bizData", bizData);
        params.put(AiyaSignUtils.SIGN_FIELD, sign);

        String url = getRequestUrl();
        String requestJson = JSON.toJSONString(params);
        String logRequestJson = JSON.toJSONString(maskLogParams(params));
        // SKU 查询用 warn：任务拉到 0 条而手工测试有数据时，需从日志直接核对 bizData/响应
        if ("查询SKU".equals(actionName)) {
            log.warn("[AIYA{}] 请求开始, url={}, params={}", actionName, url, logRequestJson);
        } else {
            log.info("[AIYA{}] 请求开始, url={}, params={}", actionName, url, logRequestJson);
        }
        String response;
        try {
            // 爱亚网关要求 partnerId/serviceType/bizData/sign 以 x-www-form-urlencoded 表单字段提交，
            // 而非 JSON body，故使用表单 POST（OkHttpUtils.doPost 内部走 FormBody）。
            response = OkHttpUtils.doPost(url, params, null);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[AIYA{}] HTTP调用异常, url={}, cost={}ms, params={}", actionName, url, cost, logRequestJson, e);
            throw new ServiceException(e, ApiError.WH_AIYA_SDK_API_CALL_ERROR, actionName, e.getMessage());
        }
        long cost = System.currentTimeMillis() - start;
        if ("查询SKU".equals(actionName)) {
            log.warn("[AIYA{}] 请求结束, cost={}ms, response={}", actionName, cost, truncateRawResponse(response));
        } else {
            log.info("[AIYA{}] 请求结束, cost={}ms", actionName, cost);
        }
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
     * 日志参数脱敏：partnerId / sign 打码；bizData 可能含收件人等 PII，仅记录截断内容。
     */
    private Map<String, Object> maskLogParams(Map<String, Object> params) {
        Map<String, Object> logParams = new HashMap<>(params);
        if (logParams.containsKey("partnerId")) {
            logParams.put("partnerId", "***");
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
     * 构造 AIYA 请求地址。爱亚网关地址即完整请求地址，无需额外拼接 router 路径。
     *
     * @return 标准化后的请求地址（去除末尾多余的 /）
     */
    private String getRequestUrl() {
        String domain = getPreUrl();
        String normalized = domain == null ? "" : domain.trim();
        if (normalized.isEmpty()) {
            throw new ServiceException(ApiError.WH_AIYA_SDK_DOMAIN_EMPTY);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
