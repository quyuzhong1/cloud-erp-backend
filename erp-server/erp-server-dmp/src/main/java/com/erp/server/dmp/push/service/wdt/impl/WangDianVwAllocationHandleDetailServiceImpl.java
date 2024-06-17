package com.erp.server.dmp.push.service.wdt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.wdt.WangDianVwAllocationHandleDetailService;
import com.sdk.wangdian.sdk.api.Result;
import com.sdk.wangdian.sdk.api.virtualWarehouse.VwAllocationHandleDetailAPI;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwAllocationHandelDetailPushDTO;
import com.sdk.wangdian.server.WangDianClientService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 分货单处理明细
 */
@Service
public class WangDianVwAllocationHandleDetailServiceImpl implements WangDianVwAllocationHandleDetailService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private WangDianClientService wangDianClientService;
    @Resource
    private CommonService commonService;

    @Resource
    private RedissonClient redissonClient;

    private static final String LOCK = "wdt:push:product:spu:";


    @Override
    public void executeConsumer(VwAllocationHandelDetailPushDTO pushDTOS) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        RLock lock = redissonClient.getLock(LOCK + pushDTOS.getVirtualWarehouseNo());
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                VwAllocationHandleDetailAPI api = wangDianClientService.get(VwAllocationHandleDetailAPI.class);
                Map<String, Object> map = JSON.parseObject(JSON.toJSONString(pushDTOS), new TypeReference<Map<String, Object>>() {
                });
                Map<String, Object> request = commonService.makeApiFieldMap(map, platformEntity.getId(), ApiModuleTypeEnum.WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL.getCode());
                Result result = api.batchPush(Collections.singletonList(request));
                String msg = Optional.ofNullable(result.getErrorList()).orElse(new ArrayList<>()).stream()
                        .map(errorList -> String.format("【spu:%s，错误原因：%s】", errorList.getNo(), errorList.getError()))
                        .collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(msg)){
                    throw new ServiceException(msg);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ApiError.ERROR_1026);
        } finally {
            lock.unlock();
        }
    }
}
