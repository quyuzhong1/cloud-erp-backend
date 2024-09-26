package com.erp.server.mrp.controller.api;


import com.erp.server.mrp.service.CfgDataArchivingService;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;

/**
 * <p>
 * 归档配置 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-09-26
 */
@RestController
@RequestMapping("/cfg-data-archiving")
public class CfgDataArchivingController extends BaseController {

    @Resource
    private CfgDataArchivingService cfgDataArchivingService;
}
