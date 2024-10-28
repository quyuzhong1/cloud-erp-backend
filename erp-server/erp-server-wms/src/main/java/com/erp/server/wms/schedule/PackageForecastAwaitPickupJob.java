package com.erp.server.wms.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.HandoverStatusEnum;
import com.erp.server.wms.service.PackageForecastDetailService;
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
 * @ClassName PackageForecastAwaitPickupJob
 * @description: 组包详情状态更新
 * @date 2024-10-28
 * @version: 1.0
 */
@Component
@Slf4j
public class PackageForecastAwaitPickupJob {
    @Resource
    private PackageForecastService packageForecastService;
    @Resource
    private PackageForecastDetailService packageForecastDetailService;

    /**
     * 同步组包订单详情
     */
    @XxlJob(value = "syncPackageForecastAwaitPickupInfo")
    public void SyncPackageForecastInfo() throws Exception {
        XxlJobHelper.log("syncPackageForecastInfo start : {}", LocalDateTime.now());
        DateTime dateTime = DateUtil.offsetMonth(DateUtil.date(), -3);
        //根据订单查询组包明细  默认查询 3月内的组包数据
        List<PackageForecastEntity> orders = packageForecastService.lambdaQuery().ne(PackageForecastEntity::getHandoverNo, "")
                .eq(PackageForecastEntity::getHandoverStatus, HandoverStatusEnum.AWAITING_PICKUP.getCode())
                .gt(PackageForecastEntity::getBillDate, dateTime).list();
        if (CollectionUtils.isEmpty(orders)){
            XxlJobHelper.log("syncPackageForecastInfo end : {}", LocalDateTime.now());
            return;
        }
        //查询需要查询的订单
        orders.forEach(packageForecastEntity -> {
            packageForecastService.queryAliExpressInfo(packageForecastEntity);
            XxlJobHelper.log("syncPackageForecastInfo update : {}", packageForecastEntity.getHandoverNo());
        });
        XxlJobHelper.log("syncPackageForecastInfo end : {}", LocalDateTime.now());
    }
}
