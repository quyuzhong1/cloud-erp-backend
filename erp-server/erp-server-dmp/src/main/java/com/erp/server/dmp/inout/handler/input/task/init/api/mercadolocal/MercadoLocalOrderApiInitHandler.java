package com.erp.server.dmp.inout.handler.input.task.init.api.mercadolocal;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.constant.DmpInputConstant;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.mercadolocal.constant.MercadoConstant;
import com.sdk.oms.mercadolocal.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.order.OrderDataDTO;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@Scope("prototype")
public class MercadoLocalOrderApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private MercadoLocalSdkClientService mercadoLocalSdkClientService;
    @Resource
    private MercadoLocalRateLimitHelper rateLimitHelper;

    private static final String BIZ_TYPE = MercadoLocalRateLimitHelper.BIZ_ORDER_SEARCH;

    /**
     * 美客多 orders/search 要求完整 ISO8601（含秒）。OffsetDateTime#toString 在秒为 0 时会省略 :00，
     * 例如 2026-06-25T17:46-04:00，导致 API 返回 invalid_date_format。
     */
    private static final DateTimeFormatter MERCADO_ORDER_SEARCH_OFFSET =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");


    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
    	List<DmpInputTaskInitDTO> dealOrderIdQuery = dealOrderIdQuery(dmpInputApiInitRequest);
    	if(dealOrderIdQuery != null) {
    		return dealOrderIdQuery;
    	}
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();


        MercadoShopInfoDTO shopInfoDTO = mercadoLocalSdkClientService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("美客多店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }
        String userId = String.valueOf(shopInfoDTO.getUserId());
        String inputTaskId = dmpInputApiInitRequest.getInputTaskId();

        // 入口检查限流退避标记。命中则 fail-fast 抛异常，避免在退避期内继续打 API 加剧限流。
        // 注意：本 handler 实现的是 DmpInputApiInitHandler 接口，没有 dmpResponse，无法走"软退"路径，
        // 只能依赖任务调度器在 errorCount 未到上限时按 nextExecTime 自然重试。
        if (rateLimitHelper.isLimited(userId, BIZ_TYPE)) {
            throw new ServiceException(StrUtil.format(
                    "美客多本土站-订单查询：店铺userId={} 处于限流退避中，本次跳过等待退避结束后重试", userId));
        }

        String url = MercadoConstant.URL;
        String path = dmpInputApiInitRequest.getApiType();

        //每次最多获取50条
        int pageSize = 50;
        //当前页数
        int pageNo = 0;

        boolean nexflag = true;

        String platformOrderCreateTime = "";
        String taskExtendJson = dmpInputApiInitRequest.getTaskExtendJson();
        if(StringUtils.isNotBlank(taskExtendJson)) {
        	JSONObject parseObject = JSON.parseObject(taskExtendJson);
            if(parseObject != null) {
            	platformOrderCreateTime = parseObject.getString(DmpInputConstant.PLATFORM_ORDER_CREATE_TIME);
            }
        }
        while (nexflag) {
            int offset = pageSize * pageNo;
            String pageCacheId = "offset:" + offset;

            if (StringUtils.isNotBlank(inputTaskId)) {
                String cached = rateLimitHelper.getResultCache(inputTaskId, userId, BIZ_TYPE, pageCacheId);
                if (StringUtils.isNotBlank(cached)) {
                    JSONArray cachedResults = JSON.parseArray(cached);
                    if (CollectionUtils.isEmpty(cachedResults)) {
                        nexflag = false;
                        break;
                    }
                    DmpInputTaskInitDTO cachedDto = new DmpInputTaskInitDTO();
                    cachedDto.setMsg(cached);
                    dmpInputTaskInitDTOList.add(cachedDto);
                    pageNo++;
                    if (cachedResults.size() < pageSize) {
                        nexflag = false;
                    }
                    continue;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(url);
            sb.append(path);
            sb.append("?seller=");
            sb.append(shopInfoDTO.getUserId());
            sb.append("&limit=");
            sb.append(pageSize);
            sb.append("&offset=");
            sb.append(offset);
            if(StringUtils.isNotBlank(platformOrderCreateTime)) {
                OffsetDateTime orderCreated = OffsetDateTime.parse(platformOrderCreateTime);
            	sb.append("&order.date_created.from=");
                sb.append(formatMercadoOrderSearchDateTime(orderCreated.plusSeconds(-5)));
                sb.append("&order.date_created.to=");
                sb.append(formatMercadoOrderSearchDateTime(orderCreated.plusSeconds(5)));
            }else {
            	sb.append("&order.date_last_updated.from=");
                sb.append(this.dateToStr(dmpInputApiInitRequest.getStartTime()));
                sb.append("&order.date_last_updated.to=");
                sb.append(this.dateToStr(dmpInputApiInitRequest.getEndTime()));
            }
            sb.append("&order.status=");
            sb.append("cancelled,paid,invalid");
            //入参
            HashMap<String, Object> params = new HashMap<>(2);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

            // 单次请求，去掉内嵌 sleep+retry 死循环：429/503/timeout 都走限流退避
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(),
                    JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);

            // 限流 / 网关临时不可用 / Read timed out → 写退避标记后 fail-fast，
            // 已经成功拉到的前面分页数据本次会被一并丢弃，下次调度从 offset=0 重新拉以保证数据完整。
            if (rateLimitHelper.isRateLimitedCode(apiResult.getCode())
                    || (apiResult.getMsg() != null && apiResult.getMsg().equalsIgnoreCase("Read timed out"))) {
                rateLimitHelper.markLimited(userId, BIZ_TYPE, MercadoLocalRateLimitHelper.DEFAULT_BACKOFF_SECONDS);
                log.warn("【美客多本土站-订单查询】触发限流/超时，写入退避标记。userId={}, code={}, msg={}, url={}",
                        userId, apiResult.getCode(), apiResult.getMsg(), sb.toString());
                throw new ServiceException(StrUtil.format(
                        "美客多本土站-订单查询触发限流/超时，已写入退避标记，等待重试。userId={}, code={}",
                        userId, apiResult.getCode()));
            }

            if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
                log.error("调用url={},入参params={}, 美客多marketplace/orders/search数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if (ObjectUtil.isEmpty(apiResult.getData())) {
                break;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            OrderDataDTO orderDTO;
            try {
                orderDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDataDTO.class);
            } catch (JsonProcessingException e) {
                log.error("美客多orders/search接口数据解析错误，数据={}", apiResult.getData(), e);
                throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if (CollectionUtils.isEmpty(orderDTO.getResults())) {
                nexflag = false;
                break;
            }
            pageNo++;

            String pageResultJson = JSONArray.toJSONString(orderDTO.getResults());
            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(pageResultJson);
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

            if (StringUtils.isNotBlank(inputTaskId)) {
                rateLimitHelper.setResultCache(inputTaskId, userId, BIZ_TYPE, pageCacheId, pageResultJson,
                        MercadoLocalRateLimitHelper.CACHE_SECONDS_TASK_LIFE);
            }

        }
        return dmpInputTaskInitDTOList;
    }

    private static String formatMercadoOrderSearchDateTime(OffsetDateTime dateTime) {
        return dateTime.format(MERCADO_ORDER_SEARCH_OFFSET);
    }

    /**
     * date_last_updated 区间参数：固定 pattern 含秒/毫秒，非 OffsetDateTime#toString，不会出现秒位省略问题。
     */
    private String dateToStr(LocalDateTime dateTime) {

        OffsetDateTime utcTime = dateTime
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String formatted = utcTime.format(formatter);

        return formatted.replace("+00:00", "-00");
    }

    private List<DmpInputTaskInitDTO> dealOrderIdQuery(DmpInputApiInitRequest dmpInputApiInitRequest) {
    	String extendJson = dmpInputApiInitRequest.getTaskExtendJson();
    	if(StringUtils.isBlank(extendJson)) {
            return null;
        }
        JSONObject parseObject = JSON.parseObject(extendJson);
        if(null == parseObject) {
            return null;
        }
        JSONArray jsonArray = parseObject.getJSONArray(DmpInputConstant.ORDER_ID_LIST);
        if (CollectionUtils.isEmpty(jsonArray)){
            return null;
        }
     	List<String> orderIdList = jsonArray.stream()
            .map(Object::toString)
            .collect(Collectors.toList());
     	parseObject.remove(DmpInputConstant.ORDER_ID_LIST);

     	String nextLevelId = dmpInputApiInitRequest.getNextLevelId();

        MercadoShopInfoDTO shopInfoDTO = mercadoLocalSdkClientService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("美客多店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }
        String userId = String.valueOf(shopInfoDTO.getUserId());
        String inputTaskId = dmpInputApiInitRequest.getInputTaskId();


        //入参
        HashMap<String, Object> params = new HashMap<>(2);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        List<String> failedOrderIds = new ArrayList<>();
        for(String orderId : orderIdList) {
            if (StringUtils.isNotBlank(inputTaskId)) {
                String cached = rateLimitHelper.getResultCache(inputTaskId, userId,
                        MercadoLocalRateLimitHelper.BIZ_ORDER_GET, orderId);
                if (StringUtils.isNotBlank(cached)) {
                    DmpInputTaskInitDTO cachedDto = new DmpInputTaskInitDTO();
                    cachedDto.setMsg(cached);
                    dmpInputTaskInitDTOList.add(cachedDto);
                    continue;
                }
            }

        	StringBuffer sb = new StringBuffer();
            sb.append(MercadoConstant.URL);
            sb.append("/orders/");
            sb.append(orderId);
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
                log.warn("{}查询订单失败，返回 responseMap={}", orderId, JSONUtil.toJsonStr(apiResult));
                failedOrderIds.add(orderId);
                continue;
            }
            if (ObjectUtil.isEmpty(apiResult.getData())) {
                log.warn("{}未查询到数据", orderId);
                failedOrderIds.add(orderId);
                continue;
            }
            String jsonStr = JSONUtil.toJsonStr(apiResult.getData());
            JSONObject resultJson = JSON.parseObject(jsonStr);
            if (resultJson == null) {
                log.warn("{}未查询到有效订单数据，返回报文：{}", orderId, jsonStr);
                failedOrderIds.add(orderId);
                continue;
            }
            // orderIdList 补拉：GET /orders/{id} 与 search results[] 元素为同一 Order 资源，包装为单元素 JSONArray 供下游复用
            String resultMsg = JSONArray.toJSONString(Collections.singletonList(resultJson));
            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(resultMsg);
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

            if (StringUtils.isNotBlank(inputTaskId)) {
                rateLimitHelper.setResultCache(inputTaskId, userId, MercadoLocalRateLimitHelper.BIZ_ORDER_GET,
                        orderId, resultMsg, MercadoLocalRateLimitHelper.CACHE_SECONDS_TASK_LIFE);
            }
        }
        if (!failedOrderIds.isEmpty()) {
            if (dmpInputTaskInitDTOList.isEmpty()) {
                throw new ServiceException(StrUtil.format(
                        "美客多本土站-orderIdList补拉全部失败，失败订单：{}", String.join(",", failedOrderIds)));
            }
            log.warn("美客多本土站-orderIdList补拉部分失败，失败订单：{}", String.join(",", failedOrderIds));
        }
        // orderIdList 模式始终返回 List（含空列表），避免 null 被误判为「非补拉模式」而落入分页检索
     	return dmpInputTaskInitDTOList;
    }
}
