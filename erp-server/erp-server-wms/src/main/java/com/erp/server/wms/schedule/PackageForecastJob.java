package com.erp.server.wms.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.HandoverStatusEnum;
import com.erp.server.wms.service.PackageForecastService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zdy
 * @ClassName PackageForecastJob
 * @description: 组包详情状态更新
 * @date 2024年02月20日
 * @version: 1.0
 */
@Component
@Slf4j
public class PackageForecastJob {
    @Resource
    private PackageForecastService packageForecastService;

    //通过线程安全的计数器来控制执行的次数
    private final AtomicInteger executionCount = new AtomicInteger(0);
    // 最大执行次数= 主任务每间隔10分钟一次，按半天执行一次低频代码
    private final int maxExecutionCount = 72;

    /**
     * 同步组包订单详情
     */
    @XxlJob(value = "syncPackageForecastInfo")
    public void SyncPackageForecastInfo() throws Exception {
        XxlJobHelper.log("syncPackageForecastInfo start : {}", LocalDateTime.now());
        DateTime dateTime = DateUtil.offsetMonth(DateUtil.date(), -3);
        //根据订单查询组包明细  默认查询 3月内的组包数据
        List<PackageForecastEntity> orders = packageForecastService.getAliExpressHandoverList(dateTime);
        //每12小时执行一次
        List<PackageForecastEntity> awaitingPickupList = new ArrayList<>();
        if (executionCount.incrementAndGet() >= maxExecutionCount) {
            // 还原计数器
            executionCount.decrementAndGet();
            //查询AWAITING_PICKUP状态的组包数，3个月内的
            awaitingPickupList = packageForecastService.lambdaQuery().ne(PackageForecastEntity::getHandoverNo, "")
                    .eq(PackageForecastEntity::getHandoverStatus, HandoverStatusEnum.AWAITING_PICKUP.getCode())
                    .gt(PackageForecastEntity::getBillDate, dateTime).list();
            XxlJobHelper.log("syncPackageForecastInfo get awaitingPickupList size : {}", awaitingPickupList.size());

        }
        if (CollectionUtils.isEmpty(orders) && CollectionUtils.isEmpty(awaitingPickupList)){
            XxlJobHelper.log("syncPackageForecastInfo end : {}", LocalDateTime.now());
            return;
        }
        //查询需要查询的订单
        if(CollectionUtils.isNotEmpty(orders)){
            orders.forEach(packageForecastEntity -> {
                packageForecastService.queryAliExpressInfo(packageForecastEntity);
                XxlJobHelper.log("syncPackageForecastInfo update : {}", packageForecastEntity.getHandoverNo());
            });
        }
        if(CollectionUtils.isNotEmpty(awaitingPickupList)){
            awaitingPickupList.forEach(packageForecastEntity -> {
                packageForecastService.queryAliExpressInfo(packageForecastEntity);
                XxlJobHelper.log("syncPackageForecastInfo awaitingPickupList update : {}", packageForecastEntity.getHandoverNo());
            });
        }
        XxlJobHelper.log("syncPackageForecastInfo end : {}", LocalDateTime.now());
    }
}
