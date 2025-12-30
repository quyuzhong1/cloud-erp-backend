package com.erp.server.dmp.push.consumer.wangdian;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
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
import com.sdk.wangdian.sdk.api.wms.stockout.StockoutAPI;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.SalesStockoutRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.SalesStockoutResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.SalesWeighingResponse;
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
                    log.warn("推送旺店通B2B发货单开始:{}", request.getThirdCode());
                    StockoutAPI stockoutAPI = wangDianClientService.get(StockoutAPI.class);
                    SalesStockoutRequest salesStockoutRequest = new SalesStockoutRequest();
                    salesStockoutRequest.setSrcOrderNo(request.getThirdCode());
                    Pager pager = new Pager();
                    pager.setPageNo(0);
                    pager.setPageSize(100);
                    pager.setCalcTotal(true);
                    SalesStockoutResponse sales = stockoutAPI.querySales(salesStockoutRequest,pager);
                    if(CollectionUtils.isEmpty(sales.getOrderList())){
                        return ApiResult.error(0, "未找到对应的旺店通销售订单");
                    }
                    String outCode = sales.getOrderList().get(0).getOrderNo();

                    Map<String, Object> weightingReq = new HashMap<>();
                    weightingReq.put("order_no", outCode);
                    weightingReq.put("weight", 0);

                    SalesWeighingResponse response = stockoutAPI.salesWeighing(weightingReq);
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
