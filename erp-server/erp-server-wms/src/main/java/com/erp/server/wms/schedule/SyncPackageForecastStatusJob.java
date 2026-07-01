package com.erp.server.wms.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.server.wms.service.adapter.PackageForecastPlatformAdapter;
import com.erp.server.wms.service.adapter.PackageForecastPlatformAdapterFactory;
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
    private PackageForecastPlatformAdapterFactory packageForecastPlatformAdapterFactory;

    /**
     * 同步组包订单详情
     */
    @XxlJob(value = "syncPackageForecastStatusJob")
    public void SyncPackageForecastStatusJob() throws Exception {
        XxlJobHelper.log("syncPackageForecastStatusJob start : {}", LocalDateTime.now());
        DateTime dateTime = DateUtil.offsetMonth(DateUtil.date(), -3);
        // 默认查询 3 月内的组包数据；各平台统一走适配器，避免速卖通重复同步。
        int totalCount = 0;
        int totalFailCount = 0;
        for (PackageForecastPlatformAdapter adapter : packageForecastPlatformAdapterFactory.listAdapters()) {
            List<PackageForecastEntity> trackingList = adapter.listSyncTrackingStatus(dateTime);
            if (CollectionUtils.isNotEmpty(trackingList)) {
                totalCount += trackingList.size();
                final int[] platformFailCount = {0};
                XxlJobHelper.log("syncPackageForecastStatusJob platform: {}, count: {}", adapter.platform(), trackingList.size());
                adapter.syncTrackingStatus(trackingList, (packageForecastEntity, e) -> {
                    platformFailCount[0]++;
                    log.error("syncPackageForecastStatusJob platform sync failed, id: {}, transportNo: {}",
                            packageForecastEntity.getId(), packageForecastEntity.getTransportNo(), e);
                    XxlJobHelper.log("syncPackageForecastStatusJob platform sync failed, id: {}, error: {}",
                            packageForecastEntity.getId(), e.getMessage());
                });
                totalFailCount += platformFailCount[0];
            }
        }
        XxlJobHelper.log("syncPackageForecastStatusJob end : {}, total: {}, failed: {}",
                LocalDateTime.now(), totalCount, totalFailCount);
    }
}
