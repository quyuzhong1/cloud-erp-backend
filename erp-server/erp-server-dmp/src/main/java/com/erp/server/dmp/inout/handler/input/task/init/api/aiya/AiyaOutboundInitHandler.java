package com.erp.server.dmp.inout.handler.input.task.init.api.aiya;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.wms.aiya.dto.response.AiyaOutboundResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AIYA（爱亚）2C 出库单状态轮询 InitHandler，对齐 {@code WegoOutboundInitHandler} 的整体结构
 * （按订单时间窗口分页拉取），复用 {@link AbstractAiyaInitHandler} 的授权解析。
 * <p>
 * 按 dmp 任务的 {@code startTime}/{@code endTime}（缺省回退为 [昨天0点] ~ [今天0点]）作为
 * {@code orderTime} 范围窗口，调用新合并的
 * {@code AiyaOpenApiService#query2cOrder(accessToken, secret, customerCode, orderNumbers,
 * orderTimeFrom, orderTimeTo, pageNum, pageSize)} 分页拉取该时间窗口内的全部出库单最新状态。
 * <p>
 * TODO：以下均为无真实接口响应样例支撑的推测实现，需联调后修正：
 * <ul>
 *     <li>{@code orderTimeFrom}/{@code orderTimeTo} 是否为真实过滤参数名、格式是否与建单 orderTime
 *         一致（{@code yyyy-MM-dd'T'HH:mm:ssZ}）；</li>
 *     <li>翻页终止条件：文档未提供 {@code total}/{@code pages}/{@code emptyFlag} 等字段，暂以
 *         "本页返回条数 &lt; pageSize" 判断已到最后一页（与 {@code AiyaInventoryInitHandler} 一致）；</li>
 *     <li>是否按「订单时间」而非「完结时间」拉取更合适（参照 WEGO 的选择：避免已取消单因未回填完结时间
 *         而漏拉），当前先沿用订单时间窗口。</li>
 * </ul>
 * 详见 docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaOutboundInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "2C出库单";

    /** AIYA orderTime 官方格式：{@code yyyy-MM-dd'T'HH:mm:ssZ} */
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private static final int DEFAULT_PAGE_SIZE = 100;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();

        String orderTimeFrom = resolveOrderTimeFrom();
        String orderTimeTo = resolveOrderTimeTo();

        List<AiyaOutboundResp.OutboundOrderDTO> allResult = fetchOutboundPages(auth, orderTimeFrom, orderTimeTo);
        if (allResult.isEmpty()) {
            log.info("[AIYA出库] 服务商[id={}] 订单时间[{} ~ {}] 未拉到任何出库单", auth.getAuthId(), orderTimeFrom, orderTimeTo);
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allResult));
        log.info("[AIYA出库] 服务商[id={}] 订单时间[{} ~ {}] 共拉取={}条", auth.getAuthId(), orderTimeFrom, orderTimeTo, result.size());
        return Collections.singletonList(buildInitDTO(result, auth.getAuthId()));
    }

    /**
     * 按订单时间窗口分页拉取出库单；无 pages/total 字段，以"本页返回条数 &lt; pageSize"判断已到最后一页。
     */
    private List<AiyaOutboundResp.OutboundOrderDTO> fetchOutboundPages(AiyaAuth auth, String orderTimeFrom, String orderTimeTo) {
        List<AiyaOutboundResp.OutboundOrderDTO> orderList = new ArrayList<>();
        int pageNum = 1;
        while (pageNum <= MAX_PAGE_LIMIT) {
            List<AiyaOutboundResp.OutboundOrderDTO> page;
            try {
                page = aiyaOpenApiService.query2cOrder(auth.getPartnerId(), auth.getPartnerKey(), auth.getCustomerCode(),
                        null, orderTimeFrom, orderTimeTo, pageNum, DEFAULT_PAGE_SIZE);
            } catch (Exception e) {
                log.error("[AIYA出库] 服务商[id={}] query2cOrder 调用异常, pageNum={}", auth.getAuthId(), pageNum, e);
                throw new ServiceException(e, ApiError.WH_AIYA_PAGE_QUERY_ERROR, ACTION, pageNum);
            }
            if (CollUtil.isNotEmpty(page)) {
                orderList.addAll(page);
            }
            if (CollUtil.isEmpty(page) || page.size() < DEFAULT_PAGE_SIZE) {
                break;
            }
            pageNum++;
        }
        if (pageNum > MAX_PAGE_LIMIT) {
            log.error("[AIYA出库] 服务商[id={}] 已达最大翻页上限({})，任务中止", auth.getAuthId(), MAX_PAGE_LIMIT);
            throw new ServiceException(ApiError.WH_AIYA_PAGE_LIMIT_EXCEEDED, ACTION, MAX_PAGE_LIMIT, orderList.size());
        }
        return orderList;
    }

    /**
     * 「订单开始时间」：dmp 任务有 startTime 则取 startTime 当天 00:00:00，否则回退为「昨天 00:00:00」。
     */
    private String resolveOrderTimeFrom() {
        LocalDateTime startTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getStartTime();
        LocalDateTime begin = startTime != null
                ? startTime.toLocalDate().atStartOfDay()
                : LocalDate.now().minusDays(1).atStartOfDay();
        return ZonedDateTime.of(begin, ZoneOffset.ofHours(8)).format(ORDER_TIME_FORMATTER);
    }

    /**
     * 「订单结束时间」：dmp 任务有 endTime 则取 endTime 当天 00:00:00，否则回退为「今天 00:00:00」。
     */
    private String resolveOrderTimeTo() {
        LocalDateTime endTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getEndTime();
        LocalDateTime end = endTime != null
                ? endTime.toLocalDate().atStartOfDay()
                : LocalDate.now().atStartOfDay();
        return ZonedDateTime.of(end, ZoneOffset.ofHours(8)).format(ORDER_TIME_FORMATTER);
    }
}
