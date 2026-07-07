package com.erp.server.dmp.inout.handler.input.task.init.api.wego;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.wms.wego.dto.response.WegoReturnOrderResp;
import com.sdk.wms.wego.service.WegoOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WEGO 退货入库单 DMP 输入 init 任务处理器。
 * <p>
 * 链路说明：
 * <ol>
 *   <li>按 {@link #dmpInputTaskEntity#getNextLevelId()} 取对应已授权的 WEGO {@code overseas_provider}；</li>
 *   <li>按任务 startTime/endTime（缺省回退为 [date-1] ~ [date]）作为「到仓日期」调用
 *       {@code returnorder.queryPage} 分页拉取退货订单；</li>
 *   <li>仅保留状态为「已处理」（status=6）的退货单，避免未处理数据进入 DMP 流水；</li>
 *   <li>每条记录追加 {@code authId} 和 {@code sourcePlatform} 字段，供下游 handler 使用。</li>
 * </ol>
 * <p>
 * 注意：WEGO 退货订单只有「待入库」和「已入库」概念，无「处理中」批次，
 * 因此无需批次推送逻辑，直接按「已处理」状态过滤即可。
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoReturnInstockInitHandler extends DmpInputInitHandler {

    /** WEGO 退货订单「已处理」状态值 */
    private static final int STATUS_FINISHED = 6;

    /** 每页最大拉取条数（WEGO 最大 100） */
    private static final int PAGE_SIZE = 100;

    /** 最大翻页保护，避免死循环 */
    private static final int MAX_PAGE_LIMIT = 200;

    private static final String AUTH_KEY_APP_TOKEN = "appToken";
    private static final String AUTH_KEY_APP_SECRET = "appSecret";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest,
                                                  DmpInputTaskResponse dmpResponse) {
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.WEGO.getCode())
                .list();
        if (CollUtil.isEmpty(providerList)) {
            throw new ServiceException("WEGO 退货入库：授权信息不存在");
        }
        OverseasProviderEntity provider = providerList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (provider == null) {
            throw new ServiceException("WEGO 退货入库：对应授权ID不存在, nextLevelId="
                    + dmpInputTaskEntity.getNextLevelId());
        }

        Map<String, Object> authJson = provider.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException("WEGO 退货入库：服务商[" + provider.getId() + "] auth_json 为空");
        }
        String appToken = toStr(authJson.get(AUTH_KEY_APP_TOKEN));
        String appSecret = toStr(authJson.get(AUTH_KEY_APP_SECRET));
        if (StringUtils.isAnyBlank(appToken, appSecret)) {
            throw new ServiceException("WEGO 退货入库：服务商[" + provider.getId() + "] appToken/appSecret 缺失");
        }

        // 到仓日期范围：优先取 dmp 任务注入的时间窗口，缺省回退为 [date-1] ~ [date]
        String arrivalDateBegin = resolveArrivalDateBegin();
        String arrivalDateEnd = resolveArrivalDateEnd();
        String authId = provider.getId();

        List<WegoReturnOrderResp.ReturnOrderDTO> allOrders =
                fetchReturnOrderPages(appToken, appSecret, arrivalDateBegin, arrivalDateEnd, authId);

        if (allOrders.isEmpty()) {
            log.info("[WEGO退货入库] 服务商[id={}] 到仓日期[{} ~ {}] 未拉到已处理退货单",
                    authId, arrivalDateBegin, arrivalDateEnd);
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allOrders));
        result.forEach(item -> {
            JSONObject obj = (JSONObject) item;
            obj.put("authId", authId);
            obj.put("sourcePlatform", DmpBasicSystemCodeEnum.WEGO.getCode());
        });

        DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
        dto.setMsg(result.toJSONString());
        return Collections.singletonList(dto);
    }

    /**
     * 分页拉取退货单，仅保留状态为「已处理」（status=6）的记录。
     */
    private List<WegoReturnOrderResp.ReturnOrderDTO> fetchReturnOrderPages(String appToken,
                                                                            String appSecret,
                                                                            String arrivalDateBegin,
                                                                            String arrivalDateEnd,
                                                                            String authId) {
        List<WegoReturnOrderResp.ReturnOrderDTO> result = new ArrayList<>();
        int pageNum = 1;
        Integer totalPages = null;

        while (pageNum <= MAX_PAGE_LIMIT) {
            WegoReturnOrderResp resp;
            try {
                resp = wegoOpenApiService.queryReturnOrderPage(
                        appToken, appSecret, arrivalDateBegin, arrivalDateEnd, pageNum, PAGE_SIZE);
            } catch (Exception e) {
                log.error("[WEGO退货入库] 服务商[id={}] 到仓日期[{} ~ {}] 调用异常, pageNum={}",
                        authId, arrivalDateBegin, arrivalDateEnd, pageNum, e);
                throw new ServiceException("WEGO 退货入库分页查询调用异常, pageNum=" + pageNum + ": " + e.getMessage(), e);
            }

            if (resp == null) {
                log.error("[WEGO退货入库] 服务商[id={}] 接口响应为空, pageNum={}", authId, pageNum);
                throw new ServiceException("WEGO 退货入库分页查询接口响应为空, pageNum=" + pageNum);
            }
            if (!Boolean.TRUE.equals(resp.getSuccess())) {
                log.error("[WEGO退货入库] 服务商[id={}] 接口返回失败: errorCode={}, errorMsg={}",
                        authId, resp.getErrorCode(), resp.getErrorMsg());
                throw new ServiceException("WEGO 退货入库分页查询接口失败: errorCode=" + resp.getErrorCode()
                        + ", errorMsg=" + resp.getErrorMsg());
            }
            WegoReturnOrderResp.PageResultDTO page = resp.getResult();
            if (page == null) {
                throw new ServiceException("WEGO 退货入库分页查询接口 result 为空, pageNum=" + pageNum);
            }
            if (totalPages == null) {
                totalPages = page.getPages();
            }
            List<WegoReturnOrderResp.ReturnOrderDTO> list = page.getList();
            if (CollUtil.isNotEmpty(list)) {
                // 只保留已处理状态
                list.stream()
                        .filter(o -> Objects.equals(STATUS_FINISHED, o.getStatus()))
                        .forEach(result::add);
            }
            if (Boolean.TRUE.equals(page.getEmptyFlag())
                    || CollUtil.isEmpty(list)
                    || (totalPages != null && pageNum >= totalPages)) {
                break;
            }
            pageNum++;
        }

        if (pageNum > MAX_PAGE_LIMIT) {
            log.error("[WEGO退货入库] 服务商[id={}] 已达最大翻页上限({})，存在未拉取数据，任务中止", authId, MAX_PAGE_LIMIT);
            throw new ServiceException("WEGO退货入库：已达最大翻页上限(" + MAX_PAGE_LIMIT
                    + ")，已拉取=" + result.size() + "条，数据不完整，任务中止");
        }
        log.info("[WEGO退货入库] 服务商[id={}] 到仓日期[{} ~ {}] 共拉到已处理退货单={}条, 翻页={}",
                authId, arrivalDateBegin, arrivalDateEnd, result.size(), pageNum);
        return result;
    }

    private String resolveArrivalDateBegin() {
        LocalDateTime startTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getStartTime();
        LocalDate begin = startTime != null ? startTime.toLocalDate() : LocalDate.now().minusDays(1);
        return begin.format(DATE_FORMATTER);
    }

    private String resolveArrivalDateEnd() {
        LocalDateTime endTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getEndTime();
        LocalDate end = endTime != null ? endTime.toLocalDate() : LocalDate.now();
        return end.format(DATE_FORMATTER);
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}
