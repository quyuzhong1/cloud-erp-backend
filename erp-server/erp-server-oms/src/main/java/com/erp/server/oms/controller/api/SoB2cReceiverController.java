package com.erp.server.oms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cReceiverService;
import com.common.core.controller.vo.ApiResult;


/**
 * B2C销售订单买家信息表
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@RestController
@RequestMapping("/soB2cReceiver")
public class SoB2cReceiverController extends BaseController {

    @Resource
    private SoB2cReceiverService soB2cReceiverService;



}
