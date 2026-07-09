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
import com.erp.model.wms.dto.WegoInOrderQueryPageDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.wms.wego.dto.response.WegoInboundResp;
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
 * dmp 输入 init 任务基础处理器：WEGO 海外仓入库单 api 拉取实现。
 * <p>
 * 与 {@code JiFengInboundInitHandler} 对齐的链路：
 * <ol>
 *   <li>按 {@link DmpInputInitHandler#dmpInputTaskEntity} 的 {@code nextLevelId} 取对应 {@code overseas_provider} 授权；</li>
 *   <li>按 dmp 任务的 startTime/endTime（缺省时回退为 [date-1] ~ [date]）作为「上架日期」范围调用
 *       {@code inorder.queryPage} 分页查询入库单；</li>
 *   <li>WEGO 服务端单页最大 {@value WegoInOrderQueryPageDTO#MAX_PAGE_SIZE}，按响应
 *       {@link WegoInboundResp.PageResultDTO#getPages()} 字段循环翻页拉取剩余分页数据。</li>
 * </ol>
 * <p>
 * 数据格式与 {@code WegoInBoundDmpHandler} / {@code WegoInboundRocketMQTaskHandler} 协议保持一致：
 * 每条记录补充 {@code authId} / {@code sourcePlatform} 字段。
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoInboundInitHandler extends DmpInputInitHandler {

    private static final String AUTH_KEY_APP_TOKEN = "appToken";
    private static final String AUTH_KEY_APP_SECRET = "appSecret";

    /**
     * WEGO inorder.queryPage 单页最大条数，与 {@link WegoInOrderQueryPageDTO#MAX_PAGE_SIZE} 保持一致。
     */
    private static final int DEFAULT_PAGE_SIZE = WegoInOrderQueryPageDTO.MAX_PAGE_SIZE;

    /**
     * 最大翻页保护，避免接口异常导致死循环。
     */
    private static final int MAX_PAGE_LIMIT = 1000;

    /**
     * WEGO 入库单接口要求的日期格式（yyyy-MM-dd）。
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.WEGO.getCode())
                .list();
        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            log.warn("[WEGO库存] 无已授权的WEGO服务商配置，跳过");
            return Collections.emptyList();
        }
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (null == overseasProviderEntity) {
            throw new ServiceException(ApiError.WH_WEGO_INBOUND_AUTH_ID_NOT_FOUND,
                    DmpBasicSystemCodeEnum.WEGO.getCode(), dmpInputTaskEntity.getNextLevelId());
        }

        Map<String, Object> authJson = overseasProviderEntity.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException(ApiError.WH_WEGO_INBOUND_AUTH_JSON_EMPTY, overseasProviderEntity.getId());
        }
        String appToken = toStr(authJson.get(AUTH_KEY_APP_TOKEN));
        String appSecret = toStr(authJson.get(AUTH_KEY_APP_SECRET));
        if (StringUtils.isAnyBlank(appToken, appSecret)) {
            throw new ServiceException(ApiError.WH_WEGO_INBOUND_TOKEN_SECRET_MISSING, overseasProviderEntity.getId());
        }

        // 上架日期范围：优先取 dmp 任务的时间窗口（定时器/补单均会注入），缺省时回退为 [date-1] ~ [date]
        String upDateBegin = resolveUpDateBegin();
        String upDateEnd = resolveUpDateEnd();

        String authId = overseasProviderEntity.getId();
        List<WegoInboundResp.InorderDTO> allOrderList = fetchInorderPages(appToken, appSecret, upDateBegin, upDateEnd, authId);

        if (allOrderList.isEmpty()) {
            log.info("[WEGO入库] 服务商[id={}] 上架日期[{} ~ {}] 未拉到任何入库单", authId, upDateBegin, upDateEnd);
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allOrderList));
        result.forEach(item -> {
            JSONObject obj = (JSONObject) item;
            obj.put("authId", authId);
            obj.put("sourcePlatform", DmpBasicSystemCodeEnum.WEGO.getCode());
        });

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(result.toJSONString());
        return Collections.singletonList(dmpInputTaskInitDTO);
    }

    /**
     * 按上架日期范围分页拉取入库单数据；第一次调用后根据响应 {@code pages} 字段循环拉取剩余分页。
     */
    private List<WegoInboundResp.InorderDTO> fetchInorderPages(String appToken, String appSecret,
                                                               String upDateBegin, String upDateEnd, String authId) {
        List<WegoInboundResp.InorderDTO> orderList = new ArrayList<>();
        int pageNum = 1;
        Integer totalPages = null;

        while (pageNum <= MAX_PAGE_LIMIT) {
            WegoInOrderQueryPageDTO.QueryReqDTO reqDTO = new WegoInOrderQueryPageDTO.QueryReqDTO();
            reqDTO.setAccessToken(appToken);
            reqDTO.setSecret(appSecret);
            reqDTO.setUpDateBegin(upDateBegin);
            reqDTO.setUpDateEnd(upDateEnd);
            reqDTO.setPageNum(pageNum);
            reqDTO.setPageSize(DEFAULT_PAGE_SIZE);

            WegoInboundResp resp;
            try {
                resp = wegoOpenApiService.queryInorderPage(reqDTO);
            } catch (Exception e) {
                log.error("[WEGO入库] 服务商[id={}] 上架日期[{} ~ {}] 调用异常，pageNum={}",
                        authId, upDateBegin, upDateEnd, pageNum, e);
                throw new ServiceException(ApiError.WH_WEGO_INBOUND_PAGE_QUERY_ERROR, pageNum - 1);
            }

            WegoInboundResp.PageResultDTO pageResult = extractPageResult(resp, authId);
            if (pageResult == null) {
                throw new ServiceException(ApiError.WH_WEGO_INBOUND_PAGE_PARSE_FAILED, pageNum, pageNum - 1);
            }

            List<WegoInboundResp.InorderDTO> list = pageResult.getList();
            if (CollUtil.isNotEmpty(list)) {
                orderList.addAll(list);
            }

            // 仅在首次响应时记录 totalPages，后续循环以同一总页数为退出条件
            if (totalPages == null) {
                totalPages = pageResult.getPages();
            }
            Boolean emptyFlag = pageResult.getEmptyFlag();
            if (Boolean.TRUE.equals(emptyFlag)
                    || CollUtil.isEmpty(list)
                    || (Objects.nonNull(totalPages) && pageNum >= totalPages)) {
                break;
            }
            pageNum++;
        }

        if (pageNum > MAX_PAGE_LIMIT) {
            log.error("[WEGO入库] 服务商[id={}] 已达最大翻页上限({})，存在未拉取数据，任务中止", authId, MAX_PAGE_LIMIT);
            throw new ServiceException(ApiError.WH_WEGO_INBOUND_PAGE_LIMIT_EXCEEDED, MAX_PAGE_LIMIT, orderList.size());
        }
        log.info("[WEGO入库] 服务商[id={}] 上架日期[{} ~ {}] 共拉取入库单={}条，已翻页={}",
                authId, upDateBegin, upDateEnd, orderList.size(), pageNum);
        return orderList;
    }

    /**
     * 校验 WEGO 接口响应：success=true 且 result 非空才返回分页对象，否则抛出业务异常。
     *
     * @return 分页对象，非空
     */
    private WegoInboundResp.PageResultDTO extractPageResult(WegoInboundResp resp, String authId) {
        if (resp == null) {
            log.error("[WEGO入库] 服务商[id={}] 接口响应为空", authId);
            throw new ServiceException(ApiError.WH_WEGO_INBOUND_RESPONSE_EMPTY);
        }
        if (!Boolean.TRUE.equals(resp.getSuccess())) {
            log.error("[WEGO入库] 服务商[id={}] 接口返回失败: errorCode={}, errorMsg={}",
                    authId, resp.getErrorCode(), resp.getErrorMsg());
            throw new ServiceException(ApiError.WH_WEGO_INBOUND_RESPONSE_FAILED, resp.getErrorCode(), resp.getErrorMsg());
        }
        WegoInboundResp.PageResultDTO result = resp.getResult();
        if (result == null) {
            log.error("[WEGO入库] 服务商[id={}] 接口 success=true 但 result 为空", authId);
            throw new ServiceException(ApiError.WH_WEGO_INBOUND_RESULT_EMPTY);
        }
        return result;
    }

    /**
     * 计算「上架开始日期」：dmp 任务有 startTime 则取 startTime 当天，否则回退为「今天 - 1 天」。
     */
    private String resolveUpDateBegin() {
        LocalDateTime startTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getStartTime();
        LocalDate begin = startTime != null ? startTime.toLocalDate() : LocalDate.now().minusDays(1);
        return begin.format(DATE_FORMATTER);
    }

    /**
     * 计算「上架结束日期」：dmp 任务有 endTime 则取 endTime 当天，否则回退为「今天」。
     */
    private String resolveUpDateEnd() {
        LocalDateTime endTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getEndTime();
        LocalDate end = endTime != null ? endTime.toLocalDate() : LocalDate.now();
        return end.format(DATE_FORMATTER);
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}
