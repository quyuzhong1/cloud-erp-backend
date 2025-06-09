package com.erp.server.oms.controller.api;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cRefService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * B2C销售订单合并拆分关联表
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@RestController
@RequestMapping("/soB2cRef")
public class SoB2cRefController extends BaseController {

    @Resource
    private SoB2cRefService soB2cRefService;



}
