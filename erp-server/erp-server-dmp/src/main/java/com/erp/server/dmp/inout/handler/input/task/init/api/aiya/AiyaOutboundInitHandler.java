package com.erp.server.dmp.inout.handler.input.task.init.api.aiya;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AiyaOutboundQueryDTO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.wms.aiya.dto.response.AiyaOutboundResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AIYA（爱亚）2C 出库单状态轮询 InitHandler，对齐 {@code WegoOutboundInitHandler} 结构，
 * 复用 {@link AbstractAiyaInitHandler} 的授权解析。
 * <p>
 * 以 [date-1]~[date] 作为「创建时间」窗口（{@code createdTimeFrom}/{@code createdTimeTo}），
 * 对齐 WEGO 按订单日期拉取，可覆盖已提交未发货单；勿单独依赖 {@code shippingTime*}（仅已发货有值）。
 * 按服务商下每个已启用仓库分页拉取（{@code warehouseCode} 文档必填）。
 * 翻页终止：暂以「本页条数 &lt; pageSize」判断末页。
 * <p>
 * 本次拉取聚合结果里若同一 {@code orderNumber} 出现多条（正常流程不应出现，接口异常/联调测试数据
 * 可能触发），按 {@code createTime}/{@code orderCreatedTime} 保留最新一条，两者均缺失/解析失败时
 * 按数组顺序兜底保留最后一条，见 {@link #dedupeLatestByOrderNumber}。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaOutboundInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "2C出库单";

    /** 创建/发运时间格式：{@code yyyy-MM-dd HH:mm:ss} */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int DEFAULT_PAGE_SIZE = AiyaOutboundQueryDTO.DEFAULT_PAGE_SIZE;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();

        List<OverseasProviderWarehouseEntity> warehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, auth.getAuthId())
                .eq(OverseasProviderWarehouseEntity::getDisabled, Boolean.FALSE)
                .list();
        if (CollUtil.isEmpty(warehouseList)) {
            log.warn("[AIYA出库] 服务商[id={}] 下无可用仓库，跳过", auth.getAuthId());
            return Collections.emptyList();
        }

        String createdTimeFrom = resolveCreatedTimeFrom();
        String createdTimeTo = resolveCreatedTimeTo();

        List<AiyaOutboundResp.OutboundOrderDTO> allResult = new ArrayList<>();
        for (OverseasProviderWarehouseEntity warehouse : warehouseList) {
            String warehouseCode = warehouse.getPlatformWarehouseCode();
            if (StringUtils.isBlank(warehouseCode)) {
                log.warn("[AIYA出库] 服务商[id={}] 仓库[id={}] platformWarehouseCode 为空，跳过",
                        auth.getAuthId(), warehouse.getId());
                continue;
            }
            allResult.addAll(fetchOutboundByWarehouse(auth, warehouseCode, createdTimeFrom, createdTimeTo));
        }

        if (allResult.isEmpty()) {
            log.warn("[AIYA出库] 服务商[id={}] 创建时间[{} ~ {}] 未拉到任何出库单",
                    auth.getAuthId(), createdTimeFrom, createdTimeTo);
            return Collections.emptyList();
        }

        allResult = dedupeLatestByOrderNumber(allResult);

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allResult));
        log.warn("[AIYA出库] 服务商[id={}] 创建时间[{} ~ {}] 共拉取={}条",
                auth.getAuthId(), createdTimeFrom, createdTimeTo, result.size());
        return Collections.singletonList(buildInitDTO(result, auth.getAuthId()));
    }

    /**
     * 按仓库 + 创建时间窗口分页拉取出库单。
     */
    private List<AiyaOutboundResp.OutboundOrderDTO> fetchOutboundByWarehouse(
            AiyaAuth auth, String warehouseCode, String createdTimeFrom, String createdTimeTo) {
        List<AiyaOutboundResp.OutboundOrderDTO> orderList = new ArrayList<>();
        int pageNum = 1;
        while (pageNum <= MAX_PAGE_LIMIT) {
            AiyaOutboundQueryDTO.QueryReqDTO req = AiyaOutboundQueryDTO.QueryReqDTO.builder()
                    .accessToken(auth.getPartnerId())
                    .secret(auth.getPartnerKey())
                    .customerCode(auth.getCustomerCode())
                    .warehouseCode(warehouseCode)
                    .createdTimeFrom(createdTimeFrom)
                    .createdTimeTo(createdTimeTo)
                    .pageNum(pageNum)
                    .pageSize(DEFAULT_PAGE_SIZE)
                    .build();
            List<AiyaOutboundResp.OutboundOrderDTO> page;
            try {
                page = aiyaOpenApiService.query2cOrder(req);
            } catch (Exception e) {
                log.error("[AIYA出库] 服务商[id={}] 仓库[{}] query2cOrder 调用异常, pageNum={}",
                        auth.getAuthId(), warehouseCode, pageNum, e);
                throw new ServiceException(e, ApiError.WH_AIYA_PAGE_QUERY_ERROR, ACTION, pageNum);
            }
            if (CollUtil.isNotEmpty(page)) {
                orderList.addAll(page);
            }
            // 文档有 total 字段，但 SDK 成功路径只返回 list；暂以本页条数 < pageSize 判断末页。
            if (CollUtil.isEmpty(page) || page.size() < DEFAULT_PAGE_SIZE) {
                break;
            }
            pageNum++;
        }
        if (pageNum > MAX_PAGE_LIMIT) {
            log.error("[AIYA出库] 服务商[id={}] 仓库[{}] 已达最大翻页上限({})，任务中止",
                    auth.getAuthId(), warehouseCode, MAX_PAGE_LIMIT);
            throw new ServiceException(ApiError.WH_AIYA_PAGE_LIMIT_EXCEEDED, ACTION, MAX_PAGE_LIMIT, orderList.size());
        }
        return orderList;
    }

    /**
     * 按 {@code orderNumber} 去重：同一 {@code orderNumber} 出现多条时只保留"最新"一条，
     * 避免重复/陈旧记录同批写入下游 DMP 造成重复处理或状态被旧数据覆盖。判定依据见 {@link #isNewer}。
     * {@code orderNumber} 为空（异常数据，正常流程必填）时不参与去重，原样保留，避免丢单。
     *
     * @param orderList 本次拉取聚合的全量出库单列表（跨仓库、跨分页）
     * @return 去重后的出库单列表
     */
    private List<AiyaOutboundResp.OutboundOrderDTO> dedupeLatestByOrderNumber(
            List<AiyaOutboundResp.OutboundOrderDTO> orderList) {
        Map<String, AiyaOutboundResp.OutboundOrderDTO> latestByOrderNumber = new LinkedHashMap<>();
        List<AiyaOutboundResp.OutboundOrderDTO> noOrderNumberList = new ArrayList<>();
        int duplicateCount = 0;
        for (AiyaOutboundResp.OutboundOrderDTO order : orderList) {
            String orderNumber = order.getOrderNumber();
            if (StringUtils.isBlank(orderNumber)) {
                noOrderNumberList.add(order);
                continue;
            }
            AiyaOutboundResp.OutboundOrderDTO existing = latestByOrderNumber.get(orderNumber);
            if (existing != null) {
                duplicateCount++;
            }
            if (existing == null || isNewer(order, existing)) {
                latestByOrderNumber.put(orderNumber, order);
            }
        }
        if (duplicateCount > 0) {
            log.warn("[AIYA出库] 本次拉取发现重复orderNumber共{}条，已按createTime/orderCreatedTime"
                    + "（缺失则按数组顺序）仅保留每个单号最新一条", duplicateCount);
        }
        List<AiyaOutboundResp.OutboundOrderDTO> result = new ArrayList<>(latestByOrderNumber.values());
        result.addAll(noOrderNumberList);
        return result;
    }

    /**
     * 判断 candidate 是否比 current 更"新"：优先比较 {@code createTime}，为空则比较
     * {@code orderCreatedTime}；双方时间都无法解析时，视 candidate（数组中排在后面）为更新，
     * 与去重前的遍历顺序兼容。
     *
     * @param candidate 待比较的新记录
     * @param current   当前已保留的记录
     * @return {@code true} 表示 candidate 应替换 current
     */
    private boolean isNewer(AiyaOutboundResp.OutboundOrderDTO candidate, AiyaOutboundResp.OutboundOrderDTO current) {
        LocalDateTime candidateTime = resolveRecencyTime(candidate);
        LocalDateTime currentTime = resolveRecencyTime(current);
        if (candidateTime == null) {
            // 都解析不到时间，数组顺序在后的（candidate）胜出
            return currentTime == null;
        }
        if (currentTime == null) {
            return true;
        }
        return !candidateTime.isBefore(currentTime);
    }

    /**
     * 解析用于去重判定的"最近创建时间"：优先取 {@code createTime}，为空/解析失败则退回
     * {@code orderCreatedTime}。
     */
    private LocalDateTime resolveRecencyTime(AiyaOutboundResp.OutboundOrderDTO order) {
        LocalDateTime time = parseRecencyTime(order.getCreateTime());
        return time != null ? time : parseRecencyTime(order.getOrderCreatedTime());
    }

    /**
     * 按 {@link #TIME_FORMATTER}（{@code yyyy-MM-dd HH:mm:ss}）解析时间字符串，为空或格式不符时返回
     * {@code null}（不中断主流程，去重判定退回其他依据）。
     */
    private LocalDateTime parseRecencyTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("[AIYA出库] 去重判定时间字段解析失败，原始值={}", value, e);
            return null;
        }
    }

    /**
     * 「创建开始时间」：dmp 任务有 startTime 则取 startTime 当天 00:00:00，否则回退为「昨天 00:00:00」。
     */
    private String resolveCreatedTimeFrom() {
        LocalDateTime startTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getStartTime();
        LocalDateTime begin = startTime != null
                ? startTime.toLocalDate().atStartOfDay()
                : LocalDate.now().minusDays(1).atStartOfDay();
        return begin.format(TIME_FORMATTER);
    }

    /**
     * 「创建结束时间」：dmp 任务有 endTime 则取 endTime 当天 00:00:00，否则回退为「今天 00:00:00」。
     */
    private String resolveCreatedTimeTo() {
        LocalDateTime endTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getEndTime();
        LocalDateTime end = endTime != null
                ? endTime.toLocalDate().atStartOfDay()
                : LocalDate.now().atStartOfDay();
        return end.format(TIME_FORMATTER);
    }
}
