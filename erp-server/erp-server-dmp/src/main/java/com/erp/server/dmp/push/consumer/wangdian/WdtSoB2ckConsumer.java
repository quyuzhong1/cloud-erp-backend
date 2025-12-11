package com.erp.server.dmp.push.consumer.wangdian;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sdk.wangdian.sdk.api.sales.RawTradeAPI;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Request;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Response;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.python.google.common.util.concurrent.RateLimiter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 旺店通B2C销售订单消费(推送到旺店通)
 *
 * @author jack
 * @date 2025-12-10
 */
@Component
@Slf4j
public class WdtSoB2ckConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private WangDianClientService wangDianClientService;
    @Resource
    private CommonService commonService;
    @Resource
    private RedissonClient redissonClient;

    // 在类中添加 Gson 实例
    private static final Gson gson = new Gson();

    private static final String LOCK = "wdt:push:soB2c:";

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPushTaskService.updateStatus(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpPushTaskService.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {

        }
        RateLimiter limiter = RateLimiter.create(1, 1, TimeUnit.SECONDS);
        if (!limiter.tryAcquire()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("限流失败：{}", e.getMessage(), e);
            }
        }
        String requestStr = JSONUtil.toJsonStr(ext);
        // 处理参数中存在null字符串的数据
        requestStr = requestStr.replace("null", "");
        log.info("requestStr: {}", requestStr);
        PushSelf2Request request = JSON.parseObject(requestStr, PushSelf2Request.class);
        //request转json并且打印出来
        log.info("request: {}", request);
        try {
            RLock lock = redissonClient.getLock(LOCK + request.getBusinessCode());
            try {
                boolean locked = lock.tryLock(10, TimeUnit.SECONDS);
                if (locked) {
                    RawTradeAPI api = wangDianClientService.get(RawTradeAPI.class);
//                    Map<String, Object> rawTradeMap = JSON.parseObject(JSON.toJSONString(request.getRawTradeList()), new TypeReference<Map<String, Object>>() {});
//                    List<Map<String, Object>> rawTradeOrderMap = JSON.parseObject(JSON.toJSONString(request.getRawTradeOrderList()), new TypeReference<List<Map<String, Object>>>() {});
                    Map<String, Object> rawTradeMap = gson.fromJson(gson.toJson(request.getRawTradeList().get(0)), new TypeToken<Map<String, Object>>(){}.getType());

                    List<Map<String, Object>> rawTradeOrderMapList = new  ArrayList<>();
                    for (PushSelf2Request.RawTradeOrder order : request.getRawTradeOrderList()) {
                        Map<String, Object> rawTradeOrderMap = gson.fromJson(gson.toJson(order), new TypeToken<Map<String, Object>>(){}.getType());
                        rawTradeOrderMapList.add(rawTradeOrderMap);
                    }

                    PushSelf2Response result = api.pushSelf2(request.getShopNo() , Collections.singletonList(rawTradeMap), rawTradeOrderMapList );
                    Integer status = result.getStatus();
                    PushSelf2Response.ErrorData data = result.getData();
                    if(Objects.nonNull(data)){
                        Integer chgCount = data.getChgCount();
                        Integer newCount = data.getNewCount();
                        List<PushSelf2Response.Error> errorList = data.getErrorList();
                        String errorMsg ="";
                        if(CollUtil.isNotEmpty(errorList)){
                            errorMsg = Optional.ofNullable(errorList).orElse(new ArrayList<>()).stream()
                                    .map(error -> String.format("【拆分单号:%s，错误原因：%s】", error.getNo(), error.getError()))
                                    .collect(Collectors.joining(","));
                        }
                        throw new ServiceException(ApiError.ERROR_WDT_SALES_RAW_TRADE_PUSHSELF2,status,newCount,chgCount,errorMsg);
                    }


//                    String msg = Optional.ofNullable(result.getErrorList()).orElse(new ArrayList<>()).stream()
//                            .map(errorList -> String.format("【拆分单号:%s，错误原因：%s】", errorList.getNo(), errorList.getError()))
//                            .collect(Collectors.joining(","));
//                    if (StringUtils.isNotBlank(msg)) {
//                       throw new ServiceException(ApiError.ERROR_WDT_SALES_RAW_TRADE_PUSHSELF2);
//                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException(ApiError.ERROR_1026);

            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("推送旺店通失败:{}", e.getMessage(), e);
            throw e;
        }
        return ApiResult.success();
    }
}
