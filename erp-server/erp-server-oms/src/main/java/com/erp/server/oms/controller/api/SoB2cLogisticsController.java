package com.erp.server.oms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cLogisticsService;
import com.common.core.controller.vo.ApiResult;


/**
 * B2C销售订单物流信息表
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@RestController
@RequestMapping("/soB2cLogistics")
public class SoB2cLogisticsController extends BaseController {

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;




}
