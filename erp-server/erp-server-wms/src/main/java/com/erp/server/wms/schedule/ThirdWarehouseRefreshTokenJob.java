package com.erp.server.wms.schedule;

import cn.hutool.core.exceptions.ExceptionUtil;
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
        if (CollectionUtils.isEmpty(overseasProviderEntityList)){
            XxlJobHelper.log("[刷新三方仓token] 任务结束: 无需要刷新token的仓库--------------------------------------->");
            return ReturnT.SUCCESS;
        }
        for (OverseasProviderEntity overseasProviderEntity : overseasProviderEntityList) {
            try {
                overseasProviderService.refreshToken(overseasProviderEntity);
            }catch (Exception e){
                String errorMsg = ExceptionUtil.getMessage(e);
                XxlJobHelper.log("[刷新三方仓token] 异常: authId={}, Code={}，异常信息={}",
                        overseasProviderEntity.getId(),
                        overseasProviderEntity.getCode(),
                        errorMsg
                );

            }
        }
        XxlJobHelper.log("[刷新三方仓token] 任务结束--------------------------------------->");
        return ReturnT.SUCCESS;
    }

}
