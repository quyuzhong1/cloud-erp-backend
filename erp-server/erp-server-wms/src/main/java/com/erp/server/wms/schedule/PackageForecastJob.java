package com.erp.server.wms.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.common.business.enums.LogisticsPlatformEnum;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.erp.server.wms.service.PackageForecastService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
        List<PackageForecastEntity> orders = packageForecastService.getAliExpressHandoverList(dateTime);
        if (CollectionUtils.isEmpty(orders)){
            XxlJobHelper.log("syncPackageForecastInfo end : {}", LocalDateTime.now());
            return;
        }
        //查询需要查询的订单
        PackageForecastDTO.AlExpressHandoverBaseDTO alExpressHandoverBase;
        try {
            alExpressHandoverBase = packageForecastService.getAlExpressHandoverBase(LogisticsPlatformEnum.ALI_EXPRESS.getCode());
        }catch (Exception e){
            XxlJobHelper.log("syncPackageForecastInfo error : {}", e.getMessage());
            throw new Exception(e);
        }
        orders.forEach(packageForecastEntity -> {
            packageForecastService.queryAliExpressInfo(packageForecastEntity, alExpressHandoverBase);
            XxlJobHelper.log("syncPackageForecastInfo update : {}", packageForecastEntity.getHandoverNo());
        });
        XxlJobHelper.log("syncPackageForecastInfo end : {}", LocalDateTime.now());
    }
}
