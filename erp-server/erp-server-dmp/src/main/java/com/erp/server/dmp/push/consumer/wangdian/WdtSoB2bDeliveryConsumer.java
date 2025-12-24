package com.erp.server.dmp.push.consumer.wangdian;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sdk.wangdian.sdk.Client;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.sales.RawTradeAPI;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Request;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Response;
import com.sdk.wangdian.server.WangDianClientService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
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
public class WdtSoB2bDeliveryConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

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

    private static final String LOCK = "wdt:push:soB2bDelivery:";

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
        SoInfoEntity request = JSON.parseObject(requestStr, SoInfoEntity.class);
        try {
            RLock lock = redissonClient.getLock(LOCK + request.getCode());
            try {
                boolean locked = lock.tryLock(10, TimeUnit.SECONDS);
                if (locked) {
                    Client client = wangDianClientService.getClient();
                    JSONObject parseObject = new JSONObject();
                    parseObject.put("src_order_no",request.getThirdCode());
                    Pager pager = new Pager();
                    int pageSize = 200;
                    pager.setPageSize(pageSize);
                    pager.setCalcTotal(true);
                    pager.setPageNo(0);

                    String execute = client.execute("wms.stockout.Sales.queryWithDetail", JSON.toJSONString(Collections.singletonList(parseObject)), pager);
                    JSONObject jsonObject = JSON.parseObject(execute);
                    if(Objects.isNull(jsonObject)){
                        throw new ServiceException("旺店通获取出库明细为空，传参：{},结果{}",JSON.toJSONString(Collections.singletonList(parseObject)),execute);
                    }
                    JSONObject data = jsonObject.getJSONObject("data");
                    if(Objects.isNull(data)){
                        throw new ServiceException("旺店通获取出库明细data为空，传参：{},结果{}",JSON.toJSONString(Collections.singletonList(parseObject)),execute);
                    }
                    JSONArray jsonArray = data.getJSONArray("order");
                    if(Objects.isNull(jsonArray) || jsonArray.isEmpty()){
                        throw new ServiceException("旺店通获取出库明细order为空，传参：{},结果{}",JSON.toJSONString(Collections.singletonList(parseObject)),execute);
                    }
                    String outCode = jsonArray.getJSONObject(0).getString("order_no");
                    if(StringUtils.isBlank(outCode)){
                        throw new ServiceException("旺店通获取出库单号为空，传参：{},结果{}",JSON.toJSONString(Collections.singletonList(parseObject)),execute);
                    }
                    JSONObject deliveryParam = new JSONObject();
                    deliveryParam.put("order_no",outCode);
                    deliveryParam.put("weight",0);

                    String deliveryResult = client.execute("wms.stockout.Sales.salesWeighing", JSON.toJSONString(deliveryParam), null);
                    JSONObject resultJson = JSON.parseObject(deliveryResult);
                    if(resultJson.getInteger("status")!=0){
                        log.error("推送旺店通发货失败:{}", resultJson.getString("message"));
                        return ApiResult.error(0, "推送旺店通失败:" + resultJson.getString("message"));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException(ApiError.ERROR_1026);

            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("推送旺店通失败:{}", e.getMessage(), e);
            return ApiResult.error(0, "推送旺店通失败:" + e.getMessage());
        }
        return ApiResult.success();
    }
}
