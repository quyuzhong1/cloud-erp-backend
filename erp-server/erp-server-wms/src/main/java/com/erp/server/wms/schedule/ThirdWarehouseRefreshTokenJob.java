package com.erp.server.wms.schedule;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.RefreshShopTokenDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.tms.entity.CfgLogisticsAuthFieldEntity;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.ThirdWarehouseService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ThirdWarehouseRefreshTokenJob {

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    @Resource
    private LogisticsAuthFeign logisticsAuthFeign;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 刷新三方仓token
     */
    @XxlJob("refreshThirdWarehouseToken")
    public ReturnT<String> refreshThirdWarehouseToken() {
        XxlJobHelper.log("[刷新三方仓token] 任务开始--------------------------------------->");
        List<OverseasProviderEntity> overseasProviderEntityList = overseasProviderService.list();
        //refreshToken过期的，将状态更新为未授权
        List<OverseasProviderEntity> refreshTokenExpireList = overseasProviderEntityList.stream()
                .filter(overseasProviderEntity -> {
                    if (overseasProviderEntity.getAuthStatus().equals(AuthStatusEnum.ALREADY.getCode())) {
                        Map<String, Object> authMap = overseasProviderEntity.getAuthJson();
                        if (authMap == null || authMap.isEmpty() || !authMap.containsKey("refreshExpireIn")) {
                            return false;
                        }
                        String expireTimeStr = (String) authMap.get("refreshExpireIn");
                        LocalDateTime expireTime = LocalDateTime.parse(
                                expireTimeStr,
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        );
                        //如果当前时间+12小时大于过期时间，则需要刷新
                        LocalDateTime now = LocalDateTime.now();
                        return now.isAfter(expireTime);
                    }
                    return false;
                })
                .collect(Collectors.toList());
        refreshTokenExpireList.forEach(overseasProviderEntity -> {
            overseasProviderEntity.setAuthStatus(AuthStatusEnum.NOT.getCode());
            log.warn("[刷新三方仓token] refreshToken过期: authId={}, Code={}，状态更新为未授权",
                    overseasProviderEntity.getId(),
                    overseasProviderEntity.getCode()
            );
            XxlJobHelper.log("[刷新三方仓token] refreshToken过期: authId={}, Code={}，状态更新为未授权",
                    overseasProviderEntity.getId(),
                    overseasProviderEntity.getCode()
            );
        });
        if(CollectionUtils.isNotEmpty(refreshTokenExpireList)){
            overseasProviderService.updateBatchById(refreshTokenExpireList);
        }
        overseasProviderEntityList = overseasProviderService.list();
        overseasProviderEntityList = overseasProviderEntityList.stream()
                .filter(overseasProviderEntity -> {
                    Map<String,Object> authMap = overseasProviderEntity.getAuthJson();
                    if (authMap == null || authMap.isEmpty() || !authMap.containsKey("expireIn")) {
                        return false;
                    }
                    String expireTimeStr = (String) authMap.get("expireIn");
                    LocalDateTime expireTime = LocalDateTime.parse(
                            expireTimeStr,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    );
                    //如果当前时间+12小时大于过期时间，则需要刷新
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime refreshTime = now.plusHours(12);
                    if (refreshTime.isAfter(expireTime)) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        List<OverseasProviderEntity> updateList = new ArrayList<>();
        List<LogisticsAuthFieldEntity> updateLogistic = new ArrayList<>();
        for (OverseasProviderEntity overseasProviderEntity : overseasProviderEntityList) {
            ThirdWarehouseService thirdWarehouseService = thirdWarehouseRegistry.getHandler(overseasProviderEntity.getCode());
            Map<String,Object> authMap = overseasProviderEntity.getAuthJson();
            ApiResult<String> result = thirdWarehouseService.refreshToken(overseasProviderEntity.getId(),authMap);
            if(result.isSuccess()) {
                XxlJobHelper.log("[刷新三方仓token] 刷新成功: shopId={}, PlatformCode={}", overseasProviderEntity.getId(), overseasProviderEntity.getCode());
                overseasProviderEntity.setAuthJson(authMap);
                updateList.add(overseasProviderEntity);
                //同步刷新物流商token
                List<LogisticsAuthEntity> logisticsAuthEntities = FeignQuery.create(LogisticsAuthEntity.class)
                        .eq(LogisticsAuthEntity::getLogisticsPlatform, overseasProviderEntity.getCode())
                        .list();
                if(CollectionUtils.isEmpty(logisticsAuthEntities)){
                    continue;
                }
                List<String> logisticAuthIds = logisticsAuthEntities.stream()
                        .map(LogisticsAuthEntity::getId)
                        .collect(Collectors.toList());
                List<LogisticsAuthFieldEntity> logisticsAuthFieldEntities = FeignQuery.create(LogisticsAuthFieldEntity.class)
                        .in(LogisticsAuthFieldEntity::getLogisticsAuthId, logisticAuthIds)
                        .list();
                if(CollectionUtils.isEmpty(logisticsAuthFieldEntities)){
                    continue;
                }
                for (LogisticsAuthFieldEntity logisticsAuthFieldEntity : logisticsAuthFieldEntities) {
                    if(authMap.containsKey(logisticsAuthFieldEntity.getFieldCode())){
                        logisticsAuthFieldEntity.setFieldValue(authMap.get(logisticsAuthFieldEntity.getFieldCode()).toString());
                        updateLogistic.add(logisticsAuthFieldEntity);
                    }
                }
            }else{
                XxlJobHelper.log("[刷新三方仓token] 刷新失败: shopId={}, PlatformCode={}， error={}",
                        overseasProviderEntity.getId(),
                        overseasProviderEntity.getCode(),
                        result.getMsg()
                );
            }
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            overseasProviderService.updateBatchById(updateList);
        }
        if(CollectionUtils.isNotEmpty(updateLogistic)){
            logisticsAuthFeign.updateLogisticAuthFile(updateLogistic);
        }
        //将三方仓的token封装到redis
        List<OverseasProviderEntity> alreadyAuthList = overseasProviderService.listByAuthStatus(AuthStatusEnum.ALREADY.getCode());
        for (OverseasProviderEntity overseasProviderEntity : alreadyAuthList) {
            String tokenKey = CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, overseasProviderEntity.getCode(), overseasProviderEntity.getId());
            Map<String,Object> map = overseasProviderEntity.getAuthJson();
            map.put("ownerCode",overseasProviderEntity.getOwnerCode());
            redisUtil.set(tokenKey, map, 86400);
        }

        XxlJobHelper.log("[刷新三方仓token] 任务结束--------------------------------------->");
        return ReturnT.SUCCESS;
    }

}
