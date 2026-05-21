package com.erp.server.wms.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.server.wms.service.adapter.PackageForecastPlatformAdapter;
import com.erp.server.wms.service.adapter.PackageForecastPlatformAdapterFactory;
import com.erp.server.wms.service.PackageForecastService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jack
 * @ClassName SyncPackageForecastStatusJob
 * @description: 组包详情状态更新
 * @date 2024年02月20日
 * @version: 1.0
 */
@Component
@Slf4j
public class SyncPackageForecastStatusJob {
    @Resource
    private PackageForecastService packageForecastService;

    @Resource
    private PackageForecastPlatformAdapterFactory packageForecastPlatformAdapterFactory;

    /**
     * 同步组包订单详情
     */
    @XxlJob(value = "syncPackageForecastStatusJob")
    public void SyncPackageForecastStatusJob() throws Exception {
        XxlJobHelper.log("syncPackageForecastStatusJob start : {}", LocalDateTime.now());
        DateTime dateTime = DateUtil.offsetMonth(DateUtil.date(), -3);
        //根据订单查询组包明细  默认查询 3月内的组包数据
        List<PackageForecastEntity> awaitingPickupList = packageForecastService.getAliExpressHandoverList(dateTime);
        if (CollectionUtils.isNotEmpty(awaitingPickupList)){
            awaitingPickupList.forEach(packageForecastEntity -> {
                packageForecastService.queryAliExpressInfo(packageForecastEntity);
                XxlJobHelper.log("syncPackageForecastStatusJob awaitingPickupList update : {}", packageForecastEntity.getHandoverNo());
            });
        }
        for (PackageForecastPlatformAdapter adapter : packageForecastPlatformAdapterFactory.listAdapters()) {
            List<PackageForecastEntity> trackingList = adapter.listSyncTrackingStatus(dateTime);
            if (CollectionUtils.isNotEmpty(trackingList)) {
                trackingList.forEach(packageForecastEntity -> {
                    adapter.syncTrackingStatus(packageForecastEntity);
                    XxlJobHelper.log("syncPackageForecastStatusJob platformTrackingList update : {}", packageForecastEntity.getTransportNo());
                });
            }
        }
        XxlJobHelper.log("syncPackageForecastStatusJob end : {}", LocalDateTime.now());
    }
}
