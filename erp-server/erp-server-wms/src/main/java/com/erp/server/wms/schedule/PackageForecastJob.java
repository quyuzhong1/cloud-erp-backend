package com.erp.server.wms.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.erp.server.wms.service.PackageForecastService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    @Resource
    private PackageForecastDetailService packageForecastDetailService;

    /**
     * 同步组包订单详情
     */
    @XxlJob(value = "syncPackageForecastInfo")
    public void SyncPackageForecastInfo() throws Exception {
        XxlJobHelper.log("syncPackageForecastInfo start : {}", LocalDateTime.now());
        DateTime dateTime = DateUtil.offsetMonth(DateUtil.date(), -3);
        //根据订单查询组包明细  默认查询 3月内的组包数据
        List<String> handoverStatusList = new ArrayList<>();
        String jobParam = XxlJobHelper.getJobParam();
        String handoverStatusStr = StrUtils.null2EmptyWithTrim(jobParam);
        if (CharSequenceUtil.isNotBlank(handoverStatusStr)){
            handoverStatusList = Arrays.stream(handoverStatusStr.split(",")).distinct().collect(Collectors.toList());
        }
        handoverStatusList.add(CharSequenceUtil.EMPTY);
        List<PackageForecastEntity> orders = packageForecastService.lambdaQuery()
                .ne(PackageForecastEntity::getHandoverNo, CharSequenceUtil.EMPTY)
                .in(PackageForecastEntity::getHandoverStatus,handoverStatusList)
                .gt(PackageForecastEntity::getBillDate, dateTime)
                .list();
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
