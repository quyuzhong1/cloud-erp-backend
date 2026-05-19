package com.erp.server.dmp.inout.handler.input.task.init.api.mercadolocal;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
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


@Service
@Slf4j
@Scope("prototype")
public class MercadoLocalOrderApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private MercadoLocalSdkClientService mercadoLocalSdkClientService;
    @Resource
    private MercadoLocalRateLimitHelper rateLimitHelper;

    private static final String BIZ_TYPE = MercadoLocalRateLimitHelper.BIZ_ORDER_SEARCH;


    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();


        MercadoShopInfoDTO shopInfoDTO = mercadoLocalSdkClientService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("美客多店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }
        String userId = String.valueOf(shopInfoDTO.getUserId());

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

        while (nexflag) {
            int offset = pageSize * pageNo;

            StringBuilder sb = new StringBuilder();
            sb.append(url);
            sb.append(path);
            sb.append("?seller=");
            sb.append(shopInfoDTO.getUserId());
            sb.append("&limit=");
            sb.append(pageSize);
            sb.append("&offset=");
            sb.append(offset);
            sb.append("&order.date_last_updated.from=");
            sb.append(this.dateToStr(dmpInputApiInitRequest.getStartTime()));
            sb.append("&order.date_last_updated.to=");
            sb.append(this.dateToStr(dmpInputApiInitRequest.getEndTime()));
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

            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(orderDTO.getResults()));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

        }
        return dmpInputTaskInitDTOList;
    }

    private String dateToStr(LocalDateTime dateTime) {

        OffsetDateTime utcTime = dateTime
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String formatted = utcTime.format(formatter);

        return formatted.replace("+00:00", "-00");
    }
}
