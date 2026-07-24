package com.erp.oms.aliexpress.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.InventoryLogDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.ScItemDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.ScItemInfoDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.ScItemRelationDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.ShopItemRelationDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.StoreInfoDTO;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.oms.aliexpress.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 速卖通海外托管官方仓货品及库存流水服务。
 */
@Slf4j
@Component
public class AliExpressWarehouseInventoryService {

    private static final int ITEM_PAGE_SIZE = 200;
    private static final int INVENTORY_LOG_PAGE_SIZE = 20;
    private static final int MAX_TRADE_IDS_PER_REQUEST = 5;
    private static final int MAX_PAGE_COUNT = 1000;
    private static final String GOOD_INVENTORY_TYPE = "0";
    private static final String OUTBOUND_BIZ_TYPE = "soDecrease";
    private static final String BOUND_STATUS = "2";
    private static final String PLATFORM_NULL_MARKER = "\\N";
    private static final String DEFAULT_MERCHANT_CODE = "AETK";

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Value("${aliexpress.overseas-managed.outstock.merchant-code:" + DEFAULT_MERCHANT_CODE + "}")
    private String merchantCode;

    @Value("${aliexpress.overseas-managed.outstock.max-qps:20}")
    private int maxQps;

    private final AtomicLong nextRequestNanos = new AtomicLong();

    /**
     * 查询店铺的全托管卖家关系及全部货品关系。
     *
     * @param shopId 店铺 ID
     * @return 店铺卖家关系及货品列表
     */
    public ShopItemRelationDTO queryShopItemRelation(String shopId) {
        AliExpressShopInfoDTO shopInfo = getShopInfo(shopId);
        ShopItemRelationDTO shopRelation = querySellerRelation(shopId, shopInfo);
        shopRelation.setScItemList(queryAllScItems(shopId, shopInfo, shopRelation));
        return shopRelation;
    }

    /**
     * 分页查询指定订单与货品的良品出库流水。
     *
     * @param shopId         店铺 ID
     * @param channelSellerId 全托管渠道卖家 ID
     * @param tradeIds       平台主订单号，单次最多 5 个
     * @param scItemIds      速卖通货品 ID
     * @param beginTime      查询开始时间
     * @param endTime        查询结束时间
     * @return 有效的库存扣减流水
     */
    public List<InventoryLogDTO> queryInventoryLogs(String shopId,
                                                    String channelSellerId,
                                                    Collection<String> tradeIds,
                                                    Collection<String> scItemIds,
                                                    LocalDateTime beginTime,
                                                    LocalDateTime endTime) {
        List<String> distinctTradeIds = distinctNotBlank(tradeIds);
        List<String> distinctScItemIds = distinctNotBlank(scItemIds);
        if (CollUtil.isEmpty(distinctTradeIds) || CollUtil.isEmpty(distinctScItemIds)) {
            return Collections.emptyList();
        }
        if (distinctTradeIds.size() > MAX_TRADE_IDS_PER_REQUEST) {
            throw new ServiceException("速卖通官方仓库存流水单次最多查询5个订单");
        }
        if (CharSequenceUtil.isBlank(channelSellerId)
                || Objects.isNull(beginTime)
                || Objects.isNull(endTime)) {
            throw new ServiceException("速卖通官方仓库存流水查询参数不完整");
        }

        AliExpressShopInfoDTO shopInfo = getShopInfo(shopId);
        List<InventoryLogDTO> result = new ArrayList<>();
        int pageIndex = 1;
        while (pageIndex <= MAX_PAGE_COUNT) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("merchant_code",
                    CharSequenceUtil.blankToDefault(merchantCode, DEFAULT_MERCHANT_CODE));
            params.put("inventory_type", GOOD_INVENTORY_TYPE);
            params.put("biz_trade_ids", String.join(",", distinctTradeIds));
            params.put("page_size", String.valueOf(INVENTORY_LOG_PAGE_SIZE));
            params.put("biz_types", OUTBOUND_BIZ_TYPE);
            params.put("channel_seller_id", channelSellerId);
            params.put("page_index", String.valueOf(pageIndex));
            params.put("sc_item_ids", String.join(",", distinctScItemIds));
            params.put("begin_time", toEpochMilli(beginTime));
            params.put("end_time", toEpochMilli(endTime));

            JSONObject payload = execute(
                    shopId,
                    shopInfo,
                    AliexpressConstants.AIC_INVENTORY_LOG_QUERY,
                    Constants.METHOD_POST,
                    params,
                    "global_merchant_aic_invLog_response",
                    "global_merchant_aic_inv_log_response",
                    "global_merchant_aic_invlog_response"
            );
            assertSuccess(payload, "查询速卖通官方仓库存流水失败");
            List<InventoryLogDTO> pageData = parseInventoryLogs(
                    normalizeArray(payload.get("result"), "dto", "data", "data_list", "list"));
            result.addAll(pageData.stream().filter(this::isValidOutboundLog).collect(Collectors.toList()));

            Integer totalPage = payload.getInt("total_page");
            log.info("查询速卖通官方仓库存流水完成, shopId={}, pageIndex={}, pageSize={}, validCount={}, totalPage={}",
                    shopId, pageIndex, pageData.size(), result.size(), totalPage);
            if ((Objects.nonNull(totalPage) && pageIndex >= totalPage)
                    || (Objects.isNull(totalPage) && pageData.size() < INVENTORY_LOG_PAGE_SIZE)) {
                break;
            }
            pageIndex++;
        }
        if (pageIndex > MAX_PAGE_COUNT) {
            throw new ServiceException("查询速卖通官方仓库存流水超过最大分页限制");
        }
        return result;
    }

    /**
     * 查询库存流水使用的卖家关系，优先使用 ONE_STOP_SERVICE。
     * 平台可能忽略 business_type 并返回唯一的其他关系，此时允许回退使用。
     *
     * @param shopId   店铺 ID
     * @param shopInfo 店铺授权信息
     * @return 唯一可用的卖家关系
     */
    private ShopItemRelationDTO querySellerRelation(String shopId, AliExpressShopInfoDTO shopInfo) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("business_type", AliexpressConstants.ONE_STOP_SERVICE);
        params.put("simplify", Boolean.TRUE.toString());
        JSONObject payload = execute(
                shopId,
                shopInfo,
                AliexpressConstants.SELLER_RELATION_QUERY,
                Constants.METHOD_GET,
                params,
                "global_seller_relation_query_response"
        );
        assertSuccess(payload, "查询速卖通全托管卖家关系失败");

        List<ShopItemRelationDTO> relationList = new ArrayList<>();
        JSONObject relationPayload = nestedPayload(payload, "result", "data");
        addDirectSellerRelation(relationPayload, relationList);
        JSONArray relationArray = normalizeArray(
                relationPayload.get("seller_relation_list"), "data", "list");
        if (Objects.nonNull(relationArray)) {
            for (Object item : relationArray) {
                JSONObject relation = JSONUtil.parseObj(item);
                String businessType = relation.getStr("business_type");
                ShopItemRelationDTO dto = new ShopItemRelationDTO();
                dto.setChannelSellerId(stringValue(firstNotNull(
                        relation.get("channel_seller_id"), relation.get("channelSellerId"))));
                dto.setChannel(relation.getStr("channel"));
                dto.setBusinessType(businessType);
                relationList.add(dto);
            }
        }

        List<ShopItemRelationDTO> validRelations = relationList.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getChannelSellerId())
                        && CharSequenceUtil.isNotBlank(e.getChannel()))
                .collect(Collectors.toList());
        List<ShopItemRelationDTO> oneStopRelations = validRelations.stream()
                .filter(e -> AliexpressConstants.ONE_STOP_SERVICE.equalsIgnoreCase(e.getBusinessType()))
                .collect(Collectors.toList());
        List<ShopItemRelationDTO> candidateRelations = CollUtil.isNotEmpty(oneStopRelations)
                ? oneStopRelations : validRelations;
        if (CollUtil.isEmpty(candidateRelations)) {
            throw new ServiceException(StrUtil.format("速卖通海外托管店铺{}未获取到可用卖家关系", shopId));
        }
        if (CollUtil.isEmpty(oneStopRelations)) {
            log.warn("速卖通海外托管店铺未返回ONE_STOP_SERVICE关系，回退使用唯一卖家关系, shopId={}, businessTypes={}",
                    shopId, candidateRelations.stream()
                            .map(ShopItemRelationDTO::getBusinessType)
                            .filter(CharSequenceUtil::isNotBlank)
                            .distinct()
                            .collect(Collectors.toList()));
        }

        Map<String, ShopItemRelationDTO> distinctRelations = candidateRelations.stream()
                .collect(Collectors.toMap(
                        e -> e.getChannelSellerId() + ":" + e.getChannel(),
                        e -> e,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        if (distinctRelations.size() != 1) {
            throw new ServiceException(StrUtil.format("速卖通海外托管店铺{}存在多个可用卖家关系", shopId));
        }
        return distinctRelations.values().iterator().next();
    }

    /**
     * 分页查询店铺全托管货品。
     *
     * @param shopId      店铺 ID
     * @param shopInfo    店铺授权信息
     * @param shopRelation 店铺卖家关系
     * @return 全部货品
     */
    private List<ScItemDTO> queryAllScItems(String shopId,
                                           AliExpressShopInfoDTO shopInfo,
                                           ShopItemRelationDTO shopRelation) {
        List<ScItemDTO> result = new ArrayList<>();
        long pageIndex = 1L;
        while (pageIndex <= MAX_PAGE_COUNT) {
            Map<String, Object> query = new LinkedHashMap<>();
            query.put("channel_user_id", shopRelation.getChannelSellerId());
            query.put("channel", shopRelation.getChannel());
            query.put("biz_type", AliexpressConstants.FULL_MANAGED_ITEM_BIZ_TYPE);
            query.put("page_index", pageIndex);
            query.put("page_size", ITEM_PAGE_SIZE);

            Map<String, String> params = new LinkedHashMap<>();
            params.put("sc_item_query", JSONUtil.toJsonStr(query));
            JSONObject payload = execute(
                    shopId,
                    shopInfo,
                    AliexpressConstants.ASCP_ITEM_QUERY,
                    Constants.METHOD_POST,
                    params,
                    "aliexpress_ascp_item_query_response"
            );
            JSONObject queryResult = nestedPayload(payload, "result");
            assertSuccess(queryResult, "查询速卖通全托管货品失败");
            Object dataList = queryResult.get("data_list");
            List<ScItemDTO> pageData = parseScItems(
                    normalizeArray(dataList, "data", "list", "items"));
            result.addAll(pageData);

            Long totalCount = firstNotNullLong(
                    queryResult.get("total_count"),
                    dataList instanceof JSONObject
                            ? ((JSONObject) dataList).get("total_count") : null);
            log.info("查询速卖通全托管货品完成, shopId={}, pageIndex={}, pageSize={}, totalCount={}",
                    shopId, pageIndex, pageData.size(), totalCount);
            if ((Objects.nonNull(totalCount) && result.size() >= totalCount)
                    || (Objects.isNull(totalCount) && pageData.size() < ITEM_PAGE_SIZE)) {
                break;
            }
            pageIndex++;
        }
        if (pageIndex > MAX_PAGE_COUNT) {
            throw new ServiceException("查询速卖通全托管货品超过最大分页限制");
        }
        return result;
    }

    /**
     * 执行速卖通 TOP 请求并解包响应。
     *
     * @param shopId      店铺 ID，仅用于日志
     * @param shopInfo    店铺授权信息
     * @param apiName     API 名称
     * @param httpMethod  HTTP 方法
     * @param params      业务参数
     * @param responseKeys 可能的响应包装键
     * @return 解包后的响应
     */
    private JSONObject execute(String shopId,
                               AliExpressShopInfoDTO shopInfo,
                               String apiName,
                               String httpMethod,
                               Map<String, String> params,
                               String... responseKeys) {
        IopClient client = new IopClientImpl(
                shopInfo.getBaseUrl(),
                shopInfo.getClientId(),
                shopInfo.getClientSecret()
        );
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        request.setHttpMethod(httpMethod);
        params.forEach(request::addApiParameter);

        acquirePermit();
        IopResponse response;
        try {
            response = client.execute(request, shopInfo.getToken(), Protocol.TOP);
        } catch (ApiException e) {
            log.error("调用速卖通官方仓接口失败, shopId={}, apiName={}, params={}",
                    shopId, apiName, JSONUtil.toJsonStr(params), e);
            throw new ServiceException(StrUtil.format("调用速卖通接口{}失败：{}", apiName, e.getMessage()));
        }
        if (Objects.isNull(response) || !response.isSuccess() || CharSequenceUtil.isBlank(response.getBody())) {
            String message = Objects.isNull(response) ? "响应为空" : response.getMessage();
            throw new ServiceException(StrUtil.format("调用速卖通接口{}失败：{}", apiName, message));
        }
        JSONObject root = JSONUtil.parseObj(response.getBody());
        JSONObject payload = unwrapResponse(root, responseKeys);
        if (Boolean.FALSE.equals(payload.getBool("success"))) {
            log.error("调用速卖通官方仓接口返回失败, shopId={}, apiName={}, params={}, response={}",
                    shopId, apiName, JSONUtil.toJsonStr(params), response.getBody());
        }
        return payload;
    }

    /**
     * 兼容 TOP 响应包装和直接响应两种结构。
     *
     * @param root         原始响应
     * @param responseKeys 响应包装键
     * @return 业务响应对象
     */
    private JSONObject unwrapResponse(JSONObject root, String... responseKeys) {
        for (String responseKey : responseKeys) {
            JSONObject payload = root.getJSONObject(responseKey);
            if (Objects.nonNull(payload)) {
                return payload;
            }
        }
        return root;
    }

    /**
     * 校验平台业务响应。
     *
     * @param payload 响应对象
     * @param prefix  异常前缀
     */
    private void assertSuccess(JSONObject payload, String prefix) {
        String code = payload.getStr("code");
        String errorCode = firstNotBlank(
                payload.getStr("error_code"), payload.getStr("errorCode"));
        Object success = firstNotNull(payload.get("success"), payload.get("result_success"));
        boolean successFlag = Objects.isNull(success)
                || (!Boolean.FALSE.equals(success)
                && !"false".equalsIgnoreCase(String.valueOf(success)));
        boolean codeSuccess = CharSequenceUtil.isBlank(code) || "0".equals(code);
        boolean noErrorCode = CharSequenceUtil.isBlank(errorCode) || "0".equals(errorCode);
        if (successFlag && codeSuccess && noErrorCode) {
            return;
        }
        String errorMessage = firstNotBlank(
                payload.getStr("error_message"),
                payload.getStr("errorMessage"),
                payload.getStr("error_msg"),
                payload.getStr("error_desc"));
        throw new ServiceException(StrUtil.format(
                "{}：{} {}", prefix, firstNotBlank(errorCode, code), errorMessage));
    }

    /**
     * 转换货品列表并仅保留已绑定关系。
     *
     * @param dataArray 平台货品数组
     * @return 货品列表
     */
    private List<ScItemDTO> parseScItems(JSONArray dataArray) {
        if (Objects.isNull(dataArray)) {
            return Collections.emptyList();
        }
        List<ScItemDTO> result = new ArrayList<>();
        for (Object item : dataArray) {
            JSONObject data = JSONUtil.parseObj(item);
            ScItemDTO scItem = new ScItemDTO();
            scItem.setScItemId(stringValue(data.get("sc_item_id")));
            scItem.setSupplierSkuCode(data.getStr("supplier_sku_code"));
            scItem.setItemCode(data.getStr("item_code"));
            scItem.setWhcBarCode(data.getStr("whc_bar_code"));

            List<ScItemRelationDTO> relationList = new ArrayList<>();
            JSONArray relationArray = normalizeArray(
                    data.get("relation_list"), "relation", "data", "list");
            if (Objects.nonNull(relationArray)) {
                for (Object relationItem : relationArray) {
                    JSONObject relationData = JSONUtil.parseObj(relationItem);
                    if (!BOUND_STATUS.equals(relationData.getStr("bind_status"))) {
                        continue;
                    }
                    ScItemRelationDTO relation = new ScItemRelationDTO();
                    relation.setItemId(relationData.getStr("item_id"));
                    relation.setSkuId(relationData.getStr("sku_id"));
                    relation.setBindStatus(relationData.getStr("bind_status"));
                    relationList.add(relation);
                }
            }
            scItem.setRelationList(relationList);
            result.add(scItem);
        }
        return result;
    }

    /**
     * 转换库存流水。
     *
     * @param resultArray 平台流水数组
     * @return 库存流水列表
     */
    private List<InventoryLogDTO> parseInventoryLogs(JSONArray resultArray) {
        if (Objects.isNull(resultArray)) {
            return Collections.emptyList();
        }
        List<InventoryLogDTO> result = new ArrayList<>();
        for (Object item : resultArray) {
            JSONObject data = JSONUtil.parseObj(item);
            InventoryLogDTO inventoryLog = new InventoryLogDTO();
            inventoryLog.setInventoryType(data.getInt("inventory_type"));
            inventoryLog.setWhOrderCode(data.getStr("wh_order_code"));
            inventoryLog.setBizType(data.getStr("biz_type"));
            inventoryLog.setBizTradeId(data.getStr("biz_trade_id"));
            inventoryLog.setOperateTime(data.getLong("operate_time"));
            inventoryLog.setBizSubTradeId(data.getStr("biz_sub_trade_id"));
            inventoryLog.setChangeQuantity(data.getInt("change_quantity"));
            inventoryLog.setScItemInfo(parseScItemInfo(data.getJSONObject("sc_item_info")));
            inventoryLog.setStoreInfo(parseStoreInfo(data.getJSONObject("store_info")));
            result.add(inventoryLog);
        }
        return result;
    }

    /**
     * 转换流水货品信息。
     *
     * @param data 平台货品信息
     * @return 货品信息
     */
    private ScItemInfoDTO parseScItemInfo(JSONObject data) {
        if (Objects.isNull(data)) {
            return null;
        }
        ScItemInfoDTO dto = new ScItemInfoDTO();
        dto.setScItemId(stringValue(data.get("sc_item_id")));
        dto.setScItemCode(data.getStr("sc_item_code"));
        dto.setScItemName(data.getStr("sc_item_name"));
        dto.setScItemBarcode(data.getStr("sc_item_barcode"));
        return dto;
    }

    /**
     * 转换流水仓库信息。
     *
     * @param data 平台仓库信息
     * @return 仓库信息
     */
    private StoreInfoDTO parseStoreInfo(JSONObject data) {
        if (Objects.isNull(data)) {
            return null;
        }
        StoreInfoDTO dto = new StoreInfoDTO();
        dto.setStoreCode(data.getStr("store_code"));
        dto.setStoreName(data.getStr("store_name"));
        dto.setStoreType(data.getStr("store_type"));
        return dto;
    }

    /**
     * 校验是否为可生成销售出库单的良品扣减流水。
     *
     * @param inventoryLog 库存流水
     * @return 是否有效
     */
    private boolean isValidOutboundLog(InventoryLogDTO inventoryLog) {
        return Objects.nonNull(inventoryLog)
                && Objects.equals(0, inventoryLog.getInventoryType())
                && OUTBOUND_BIZ_TYPE.equalsIgnoreCase(inventoryLog.getBizType())
                && Objects.nonNull(inventoryLog.getChangeQuantity())
                && inventoryLog.getChangeQuantity() < 0
                && CharSequenceUtil.isNotBlank(inventoryLog.getWhOrderCode())
                && !PLATFORM_NULL_MARKER.equalsIgnoreCase(inventoryLog.getWhOrderCode().trim())
                && CharSequenceUtil.isNotBlank(inventoryLog.getBizTradeId())
                && CharSequenceUtil.isNotBlank(inventoryLog.getBizSubTradeId())
                && Objects.nonNull(inventoryLog.getOperateTime())
                && Objects.nonNull(inventoryLog.getScItemInfo())
                && CharSequenceUtil.isNotBlank(inventoryLog.getScItemInfo().getScItemId())
                && Objects.nonNull(inventoryLog.getStoreInfo())
                && CharSequenceUtil.isNotBlank(inventoryLog.getStoreInfo().getStoreName());
    }

    /**
     * 获取并校验店铺授权信息。
     *
     * @param shopId 店铺 ID
     * @return 授权信息
     */
    private AliExpressShopInfoDTO getShopInfo(String shopId) {
        AliExpressShopInfoDTO shopInfo = aliExpressOrderService.getShopInfoByShopId(shopId);
        if (Objects.isNull(shopInfo)
                || CharSequenceUtil.isBlank(shopInfo.getBaseUrl())
                || CharSequenceUtil.isBlank(shopInfo.getClientId())
                || CharSequenceUtil.isBlank(shopInfo.getClientSecret())
                || CharSequenceUtil.isBlank(shopInfo.getToken())) {
            throw new ServiceException(StrUtil.format("速卖通海外托管店铺{}授权信息不完整", shopId));
        }
        return shopInfo;
    }

    /**
     * 对所有官方仓请求进行进程级限速。
     */
    private void acquirePermit() {
        int permitsPerSecond = Math.min(1000, Math.max(1, maxQps));
        long intervalNanos = TimeUnit.SECONDS.toNanos(1) / permitsPerSecond;
        while (true) {
            long now = System.nanoTime();
            long previous = nextRequestNanos.get();
            long permitAt = Math.max(now, previous);
            if (!nextRequestNanos.compareAndSet(previous, permitAt + intervalNanos)) {
                continue;
            }
            long waitNanos = permitAt - now;
            if (waitNanos <= 0) {
                return;
            }
            try {
                TimeUnit.NANOSECONDS.sleep(waitNanos);
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("调用速卖通官方仓接口被中断");
            }
        }
    }

    /**
     * 集合去空、去重并保持原顺序。
     *
     * @param values 原始集合
     * @return 去重后的字符串列表
     */
    private List<String> distinctNotBlank(Collection<String> values) {
        if (CollUtil.isEmpty(values)) {
            return Collections.emptyList();
        }
        Set<String> result = values.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ArrayList<>(result);
    }

    /**
     * LocalDateTime 转平台要求的毫秒时间戳。
     *
     * @param time 本地时间
     * @return 毫秒时间戳
     */
    private String toEpochMilli(LocalDateTime time) {
        return String.valueOf(time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    /**
     * 安全转换 Long。
     *
     * @param value 原始值
     * @return Long 值
     */
    private Long parseLong(Object value) {
        if (Objects.isNull(value) || CharSequenceUtil.isBlank(String.valueOf(value))) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ServiceException(StrUtil.format("速卖通接口数值格式错误：{}", value));
        }
    }

    /**
     * JSON 数字或字符串统一转字符串。
     *
     * @param value 原始值
     * @return 字符串值
     */
    private String stringValue(Object value) {
        return Objects.isNull(value) ? "" : String.valueOf(value);
    }

    /**
     * 将直接返回的卖家关系加入候选列表。
     *
     * @param payload 卖家关系响应
     * @param relationList 候选关系
     */
    private void addDirectSellerRelation(JSONObject payload,
                                         List<ShopItemRelationDTO> relationList) {
        String channelSellerId = stringValue(firstNotNull(
                payload.get("channel_seller_id"), payload.get("channelSellerId")));
        String channel = payload.getStr("channel");
        if (CharSequenceUtil.isBlank(channelSellerId) || CharSequenceUtil.isBlank(channel)) {
            return;
        }
        ShopItemRelationDTO relation = new ShopItemRelationDTO();
        relation.setChannelSellerId(channelSellerId);
        relation.setChannel(channel);
        relation.setBusinessType(payload.getStr("business_type"));
        relationList.add(relation);
    }

    /**
     * 逐层解包常见的 result/data 对象。
     *
     * @param payload 原始业务响应
     * @param keys 包装键
     * @return 最内层业务对象
     */
    private JSONObject nestedPayload(JSONObject payload, String... keys) {
        JSONObject result = payload;
        for (String key : keys) {
            JSONObject nested = result.getJSONObject(key);
            if (Objects.isNull(nested)) {
                break;
            }
            result = nested;
        }
        return result;
    }

    /**
     * 将数组或 data/list 包装对象统一转换为数组。
     *
     * @param value 原始数组或包装对象
     * @param nestedKeys 可能的数组键
     * @return 标准数组
     */
    private JSONArray normalizeArray(Object value, String... nestedKeys) {
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        if (value instanceof Collection) {
            return JSONUtil.parseArray(value);
        }
        if (!(value instanceof JSONObject)) {
            return new JSONArray();
        }
        JSONObject object = (JSONObject) value;
        for (String nestedKey : nestedKeys) {
            Object nested = object.get(nestedKey);
            if (nested instanceof JSONArray) {
                return (JSONArray) nested;
            }
        }
        return new JSONArray();
    }

    /**
     * 返回首个非空对象。
     *
     * @param values 候选值
     * @return 首个非空值
     */
    private Object firstNotNull(Object... values) {
        for (Object value : values) {
            if (Objects.nonNull(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 返回首个非空字符串。
     *
     * @param values 候选值
     * @return 首个非空字符串
     */
    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (CharSequenceUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    /**
     * 返回首个可转换的 Long。
     *
     * @param values 候选值
     * @return Long 值
     */
    private Long firstNotNullLong(Object... values) {
        for (Object value : values) {
            if (Objects.nonNull(value)) {
                return parseLong(value);
            }
        }
        return null;
    }
}
