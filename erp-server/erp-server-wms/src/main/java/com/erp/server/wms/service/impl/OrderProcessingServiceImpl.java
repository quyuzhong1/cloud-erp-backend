package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;

@Slf4j
@Service
public class OrderProcessingServiceImpl implements OrderProcessingService {

    @Resource
    private FirstMileProcessingService firstMileProcessingService;

    @Resource
    private SoB2cProcessingService sob2cProcessingService;

    @Resource
    private SoB2bProcessingService soB2bProcessingService;

    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;

    @Override
    public void autoOrderProcessing(LocalDate startDate) {
        //查询最新的关账时间
       LocalDate minClosedDate = inventoryClosedRecordService.getMinClosedDate();
       if (ObjectUtil.isNull(startDate)) {
           startDate = minClosedDate.minusMonths(12L);
       }
        //b2c销售订单更新订单跟踪
        sob2cProcessingService.autoUpdateSoB2cProcessing(startDate);
        log.warn("b2c销售订单更新订单跟踪更新成功");
        //b2b销售订单更新订单跟踪
        soB2bProcessingService.autoUpdateSoB2bProcessing(startDate);
        log.warn("b2b销售订单更新订单跟踪更新成功");
        //头程订单更新订单跟踪
        firstMileProcessingService.autoUpdateFirstMileProcessing(startDate);
        log.warn("头程订单更新订单跟踪更新成功");
    }
}
