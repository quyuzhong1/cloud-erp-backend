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
import java.util.List;

/**
 * AIYA（爱亚）2C 出库单状态轮询 InitHandler，对齐 {@code WegoOutboundInitHandler} 结构，
 * 复用 {@link AbstractAiyaInitHandler} 的授权解析。
 * <p>
 * 按方案文档 6.3.3「4、爱亚出库单查询」：以 [date-1]~[date] 作为「发运时间」窗口
 * （{@code shippingTimeFrom}/{@code shippingTimeTo}），并按服务商下每个已启用仓库分页拉取
 * （{@code warehouseCode} 文档必填）。翻页终止：优先看响应 {@code total}，否则用
 * 「本页条数 &lt; pageSize」。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaOutboundInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "2C出库单";

    /** 发运时间格式：文档响应示例为 {@code yyyy-MM-dd HH:mm:ss}，请求侧同格式 */
    private static final DateTimeFormatter SHIPPING_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

        String shippingTimeFrom = resolveShippingTimeFrom();
        String shippingTimeTo = resolveShippingTimeTo();

        List<Object> allResult = new ArrayList<>();
        for (OverseasProviderWarehouseEntity warehouse : warehouseList) {
            String warehouseCode = warehouse.getPlatformWarehouseCode();
            if (StringUtils.isBlank(warehouseCode)) {
                log.warn("[AIYA出库] 服务商[id={}] 仓库[id={}] platformWarehouseCode 为空，跳过",
                        auth.getAuthId(), warehouse.getId());
                continue;
            }
            allResult.addAll(fetchOutboundByWarehouse(auth, warehouseCode, shippingTimeFrom, shippingTimeTo));
        }

        if (allResult.isEmpty()) {
            log.info("[AIYA出库] 服务商[id={}] 发运时间[{} ~ {}] 未拉到任何出库单",
                    auth.getAuthId(), shippingTimeFrom, shippingTimeTo);
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allResult));
        log.info("[AIYA出库] 服务商[id={}] 发运时间[{} ~ {}] 共拉取={}条",
                auth.getAuthId(), shippingTimeFrom, shippingTimeTo, result.size());
        return Collections.singletonList(buildInitDTO(result, auth.getAuthId()));
    }

    /**
     * 按仓库 + 发运时间窗口分页拉取出库单。
     */
    private List<AiyaOutboundResp.OutboundOrderDTO> fetchOutboundByWarehouse(
            AiyaAuth auth, String warehouseCode, String shippingTimeFrom, String shippingTimeTo) {
        List<AiyaOutboundResp.OutboundOrderDTO> orderList = new ArrayList<>();
        int pageNum = 1;
        while (pageNum <= MAX_PAGE_LIMIT) {
            AiyaOutboundQueryDTO.QueryReqDTO req = AiyaOutboundQueryDTO.QueryReqDTO.builder()
                    .accessToken(auth.getPartnerId())
                    .secret(auth.getPartnerKey())
                    .customerCode(auth.getCustomerCode())
                    .warehouseCode(warehouseCode)
                    .shippingTimeFrom(shippingTimeFrom)
                    .shippingTimeTo(shippingTimeTo)
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
     * 「发运开始时间」：dmp 任务有 startTime 则取 startTime 当天 00:00:00，否则回退为「昨天 00:00:00」。
     */
    private String resolveShippingTimeFrom() {
        LocalDateTime startTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getStartTime();
        LocalDateTime begin = startTime != null
                ? startTime.toLocalDate().atStartOfDay()
                : LocalDate.now().minusDays(1).atStartOfDay();
        return begin.format(SHIPPING_TIME_FORMATTER);
    }

    /**
     * 「发运结束时间」：dmp 任务有 endTime 则取 endTime 当天 00:00:00，否则回退为「今天 00:00:00」。
     */
    private String resolveShippingTimeTo() {
        LocalDateTime endTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getEndTime();
        LocalDateTime end = endTime != null
                ? endTime.toLocalDate().atStartOfDay()
                : LocalDate.now().atStartOfDay();
        return end.format(SHIPPING_TIME_FORMATTER);
    }
}
