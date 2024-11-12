package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.server.mrp.service.CalcSalesInfoDimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 销量试算表
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@RestController
@LogSystemModule("销量试算表")
@RequestMapping("/calcSalesInfoDim")
public class CalcSalesInfoDimController extends BaseController {

    @Resource
    private CalcSalesInfoDimService calcSalesInfoDimService;


}
