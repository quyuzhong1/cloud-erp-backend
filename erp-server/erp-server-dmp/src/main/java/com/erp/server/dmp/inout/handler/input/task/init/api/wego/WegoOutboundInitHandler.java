package com.erp.server.dmp.inout.handler.input.task.init.api.wego;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WegoOutboundQueryPageDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.wms.wego.dto.response.WegoOutboundResp;
import com.sdk.wms.wego.service.WegoOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DMP 输入 init 任务处理器：WEGO 2C 出库单状态轮询。
 * <p>
 * 按 dmp 任务的 startTime/endTime（缺省时回退为 [date-1] ~ [date]）作为「订单日期」范围，
 * 调用 2c.order.queryPage orderDateBegin/orderDateEnd 模式拉取订单的最新状态。
 * <p>
 * 之所以按订单日期(orderDate)而非完结日期(finishDate)拉取：已取消单(orderStatus=15)在被提前拦截/取消时
 * WEGO 不一定回填 finishDate，按 finishDate 拉取会漏掉这类单，导致销售订单与三方仓发货单状态无法联动。
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoOutboundInitHandler extends DmpInputInitHandler {

    /**
     * WEGO 授权 JSON（overseas_provider.auth_json）中的 accessToken 字段 key。
     * 与 WegoHandlerServiceImpl / WegoWarehouseBaseDataJob / WegoInboundInitHandler 保持一致。
     */
    private static final String AUTH_KEY_APP_TOKEN = "appToken";

    /**
     * WEGO 授权 JSON（overseas_provider.auth_json）中的 secret 字段 key。
     */
    private static final String AUTH_KEY_APP_SECRET = "appSecret";

    private static final int PAGE_SIZE = WegoOutboundQueryPageDTO.MAX_PAGE_SIZE;

    /** 最大翻页保护，避免接口异常导致死循环 */
    private static final int MAX_PAGE_LIMIT = 1000;

    /** orderDateBegin/orderDateEnd 要求的日期时间格式（精确到秒） */
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.WEGO.getCode())
                .list();
        if (CollUtil.isEmpty(providerList)) {
            throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_AUTH_INFO_NOT_FOUND);
        }
        OverseasProviderEntity provider = providerList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst().orElse(null);
        if (provider == null) {
            throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_AUTH_ID_NOT_FOUND, dmpInputTaskEntity.getNextLevelId());
        }

        // 从数据库 overseas_provider.auth_json 读取 appToken / appSecret
        Map<String, Object> authJson = provider.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_AUTH_JSON_EMPTY, provider.getId());
        }
        String appToken = toStr(authJson.get(AUTH_KEY_APP_TOKEN));
        String appSecret = toStr(authJson.get(AUTH_KEY_APP_SECRET));
        if (StringUtils.isAnyBlank(appToken, appSecret)) {
            throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_TOKEN_SECRET_MISSING, provider.getId());
        }
        String authId = provider.getId();

        // 订单日期范围：优先取 dmp 任务注入的时间窗口，缺省时回退为 [date-1 00:00:00] ~ [date 00:00:00]
        // 按订单日期(orderDate)而非完结日期(finishDate)拉取，避免漏掉无 finishDate 的已取消单
        String orderDateBegin = resolveOrderDateBegin();
        String orderDateEnd   = resolveOrderDateEnd();

        List<WegoOutboundResp.OutboundOrderDTO> allResult =
                fetchOutboundPages(appToken, appSecret, orderDateBegin, orderDateEnd, authId);
        if (allResult.isEmpty()) {
            log.info("[WEGO出库] 服务商[id={}] 订单日期[{} ~ {}] 未拉到任何出库单", authId, orderDateBegin, orderDateEnd);
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allResult));
        result.forEach(item -> {
            JSONObject obj = (JSONObject) item;
            obj.put("authId", authId);
            obj.put("sourcePlatform", DmpBasicSystemCodeEnum.WEGO.getCode());
        });
        log.info("[WEGO出库] 服务商[id={}] 订单日期[{} ~ {}] 共拉取={}条", authId, orderDateBegin, orderDateEnd, result.size());
        DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
        dto.setMsg(result.toJSONString());
        return Collections.singletonList(dto);
    }

    /**
     * 按订单日期范围分页拉取出库单；根据响应 pages 字段循环翻页。
     */
    private List<WegoOutboundResp.OutboundOrderDTO> fetchOutboundPages(
            String appToken, String appSecret,
            String orderDateBegin, String orderDateEnd, String authId) {
        List<WegoOutboundResp.OutboundOrderDTO> orderList = new ArrayList<>();
        int pageNum = 1;
        Integer totalPages = null;
        while (pageNum <= MAX_PAGE_LIMIT) {
            WegoOutboundQueryPageDTO.QueryReqDTO req = new WegoOutboundQueryPageDTO.QueryReqDTO();
            req.setAccessToken(appToken);
            req.setSecret(appSecret);
            req.setOrderDateBegin(orderDateBegin);
            req.setOrderDateEnd(orderDateEnd);
            req.setPageNum(pageNum);
            req.setPageSize(PAGE_SIZE);
            WegoOutboundResp resp;
            try {
                resp = wegoOpenApiService.query2cOrderPage(req);
            } catch (Exception e) {
                log.error("[WEGO出库] 服务商[id={}] query2cOrderPage 异常, pageNum={}", authId, pageNum, e);
                throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_PAGE_QUERY_ERROR, pageNum - 1);
            }
            if (resp == null) {
                log.error("[WEGO出库] 服务商[id={}] query2cOrderPage 接口响应为空, pageNum={}", authId, pageNum);
                throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_RESPONSE_EMPTY, pageNum);
            }
            if (!Boolean.TRUE.equals(resp.getSuccess())) {
                log.error("[WEGO出库] 服务商[id={}] query2cOrderPage 接口返回失败: errorCode={}, errorMsg={}, pageNum={}",
                        authId, resp.getErrorCode(), resp.getErrorMsg(), pageNum);
                throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_RESPONSE_FAILED, resp.getErrorCode(), resp.getErrorMsg());
            }
            WegoOutboundResp.PageResultDTO pageResult = resp.getResult();
            if (pageResult == null) {
                log.error("[WEGO出库] 服务商[id={}] query2cOrderPage success=true 但 result 为空, pageNum={}", authId, pageNum);
                throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_RESULT_EMPTY, pageNum, pageNum - 1);
            }
            List<WegoOutboundResp.OutboundOrderDTO> list = pageResult.getList();
            if (CollUtil.isNotEmpty(list)) { orderList.addAll(list); }
            if (totalPages == null) { totalPages = pageResult.getPages(); }
            if (Boolean.TRUE.equals(pageResult.getEmptyFlag()) || CollUtil.isEmpty(list)
                    || (totalPages != null && pageNum >= totalPages)) { break; }
            pageNum++;
        }
        if (pageNum > MAX_PAGE_LIMIT) {
            log.error("[WEGO出库] 服务商[id={}] 已达最大翻页上限({})，存在未拉取数据，任务中止", authId, MAX_PAGE_LIMIT);
            throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_PAGE_LIMIT_EXCEEDED, MAX_PAGE_LIMIT, orderList.size());
        }
        log.info("[WEGO出库] 服务商[id={}] 订单日期[{} ~ {}] 拉取={}条 已翻页={}",
                authId, orderDateBegin, orderDateEnd, orderList.size(), pageNum);
        return orderList;
    }

    /**
     * 计算「订单开始日期时间」：dmp 任务有 startTime 则取 startTime 当天 00:00:00，否则回退为「昨天 00:00:00」。
     */
    private String resolveOrderDateBegin() {
        LocalDateTime startTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getStartTime();
        LocalDateTime begin = startTime != null
                ? startTime.toLocalDate().atStartOfDay()
                : LocalDate.now().minusDays(1).atStartOfDay();
        return begin.format(DATETIME_FORMATTER);
    }

    /**
     * 计算「订单结束日期时间」：dmp 任务有 endTime 则取 endTime 当天 00:00:00，否则回退为「今天 00:00:00」。
     */
    private String resolveOrderDateEnd() {
        LocalDateTime endTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getEndTime();
        LocalDateTime end = endTime != null
                ? endTime.toLocalDate().atStartOfDay()
                : LocalDate.now().atStartOfDay();
        return end.format(DATETIME_FORMATTER);
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}