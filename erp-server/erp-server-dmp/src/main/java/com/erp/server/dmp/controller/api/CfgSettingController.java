package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.CfgSettingService;
import com.common.core.controller.vo.ApiResult;


/**
 * <p>
 * 服务配置表
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
@RestController
@RequestMapping("/cfgSetting")
public class CfgSettingController extends BaseController {

    @Autowired
    private CfgSettingService cfgSettingService;



}
